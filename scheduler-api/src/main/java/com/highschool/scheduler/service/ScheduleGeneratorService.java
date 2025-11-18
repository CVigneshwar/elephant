package com.highschool.scheduler.service;

import com.highschool.scheduler.dto.ScheduleEventDTO;
import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.model.Teacher;
import com.highschool.scheduler.repository.ClassroomRepository;
import com.highschool.scheduler.repository.CourseRepository;
import com.highschool.scheduler.repository.CourseSectionRepository;
import com.highschool.scheduler.repository.SemesterRepository;
import com.highschool.scheduler.repository.StudentCourseHistoryRepository;
import com.highschool.scheduler.repository.StudentRepository;
import com.highschool.scheduler.repository.StudentSectionEnrollmentRepository;
import com.highschool.scheduler.repository.TeacherRepository;
import com.highschool.scheduler.service.util.CourseEligibilityCalculator;
import com.highschool.scheduler.service.util.SchedulerUtils;
import com.highschool.scheduler.service.util.TeacherLoadTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ScheduleGeneratorService {

    private static final int ROOM_CAPACITY = 10;
    private static final int TEACHER_MAX_DAILY_HOURS = 4;

    private static final DayOfWeek[] DAYS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    };

    private static final List<LocalTime> SLOTS = List.of(
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(13, 0), // skip lunch
            LocalTime.of(14, 0),
            LocalTime.of(15, 0),
            LocalTime.of(16, 0)
    );

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SemesterRepository semesterRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseHistoryRepository studentCourseHistoryRepository;
    private final StudentSectionEnrollmentRepository studentSectionEnrollmentRepository;

    /**
     * Constructs the ScheduleGeneratorService with required repositories.
     */
    public ScheduleGeneratorService(CourseRepository courseRepo,
                                    TeacherRepository teacherRepo,
                                    ClassroomRepository classroomRepo,
                                    SemesterRepository semesterRepo,
                                    CourseSectionRepository sectionRepo,
                                    StudentRepository studentRepo,
                                    StudentCourseHistoryRepository historyRepo,
                                    StudentSectionEnrollmentRepository studentEnrollmentRepo) {
        this.courseRepository = courseRepo;
        this.teacherRepository = teacherRepo;
        this.classroomRepository = classroomRepo;
        this.semesterRepository = semesterRepo;
        this.courseSectionRepository = sectionRepo;
        this.studentRepository = studentRepo;
        this.studentCourseHistoryRepository = historyRepo;
        this.studentSectionEnrollmentRepository = studentEnrollmentRepo;
    }

    /**
     * Generates a balanced schedule for the active semester.
     * Deletes previous sections, creates new ones, and returns events for the UI.
     * @return List of scheduled events (DTOs).
     */
    @Transactional
    public List<ScheduleEventDTO> generateForActiveSemester() {
        Semester semester = semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active semester found"));

        // Remove existing course sections for active semester
        courseSectionRepository.deleteAllByActiveSemester();

        // Fetch only courses belonging to active semester order
        List<Course> courses = courseRepository.findBySemesterOrder(semester.getOrderInYear());
        if (courses.isEmpty()) return List.of();

        List<Teacher> teachers = teacherRepository.findAll();
        List<Classroom> rooms = classroomRepository.findAll();
        CourseEligibilityCalculator courseEligibilityCalculator = new CourseEligibilityCalculator(studentRepository.findAll(), studentCourseHistoryRepository.findAll());
        TeacherLoadTracker load = new TeacherLoadTracker();

        Map<Long, List<Teacher>> teachersBySpec = teachers.stream()
                .filter(t -> t.getSpecialization() != null)
                .collect(Collectors.groupingBy(t -> t.getSpecialization().getId()));

        Map<Long, List<Classroom>> roomsByType = rooms.stream()
                .filter(r -> r.getRoomType() != null)
                .collect(Collectors.groupingBy(r -> r.getRoomType().getId()));


        Map<String, Integer> globalSlotLoad = initializeGlobalSlotLoad();


        List<CourseSection> courseSections = generateCourseSections(courses, courseEligibilityCalculator, teachersBySpec, roomsByType, load, semester, globalSlotLoad);
        List<CourseSection> savedCourseSections = courseSectionRepository.saveAll(courseSections);
        return savedCourseSections.stream().map(this::modelToDto).toList();
    }

    /**
     * Clears all course sections and enrollments for the active semester.
     */
    @Transactional
    public void resetSchedule() {
        studentSectionEnrollmentRepository.deleteAllByActiveSemester();
        courseSectionRepository.deleteAllByActiveSemester();
        System.out.println(" Cleared course sections and enrollments for active semester only.");
    }

    /**
     * Clears all course sections and enrollments for the active semester.
     */
    public List<ScheduleEventDTO> getSchedule() {
        return courseSectionRepository.findAll().stream().map(this::modelToDto).collect(Collectors.toList());
    }

    /**
     * Generates course sections for the given courses and semester.
     * @param courses List of courses to schedule.
     * @param demand CourseEligibilityCalculator for demand estimation.
     * @param teachersBySpec Map of specialization to available teachers.
     * @param roomsByType Map of room type to available classrooms.
     * @param load TeacherLoadTracker for load balancing.
     * @param semester The semester entity.
     * @param globalSlotLoad Map tracking slot usage for balancing.
     * @return List of scheduled CourseSection entities (not saved yet).
     */
    private List<CourseSection> generateCourseSections(List<Course> courses, CourseEligibilityCalculator demand, Map<Long, List<Teacher>> teachersBySpec, Map<Long, List<Classroom>> roomsByType, TeacherLoadTracker load, Semester semester, Map<String, Integer> globalSlotLoad) {
        int weeksInSemester = calculateWeeksInSemester(semester);
        List<CourseSection> result = new ArrayList<>();
        courses.forEach(course -> {
            int eligibleStudentsCount = demand.eligibleStudentsCount(course);
            int sectionsNeeded = Math.max(1,
                    (int) Math.ceil((double) eligibleStudentsCount / (ROOM_CAPACITY * weeksInSemester)));

            if (course.getHoursPerWeek() < sectionsNeeded) {
                throw new IllegalStateException("hoursPerWeek needs to be increased for course - " + course.getName());
            }

            assignBalancedCourseSectionSchedule(course,   load, result,
                    teachersBySpec, roomsByType, globalSlotLoad, semester);
        });
        return result;
    }

    /**
     * Schedules course sections in a balanced way across timeslots, teachers, and rooms.
     * @param course The course to schedule.
     * @param load The teacher load tracker.
     * @param result A list to collect created CourseSections.
     * @param teachersBySpec Map of specialization to teachers.
     * @param roomsByType Map of room type to classrooms.
     * @param globalSlotLoad Map tracking slot usage.
     * @param semester The semester entity.
     */
    private void assignBalancedCourseSectionSchedule(
            Course course,
            TeacherLoadTracker load,
            List<CourseSection> result,
            Map<Long, List<Teacher>> teachersBySpec,
            Map<Long, List<Classroom>> roomsByType,
            Map<String, Integer> globalSlotLoad,
            Semester semester
    ) {
        int remainingHours = course.getHoursPerWeek();

        while (remainingHours > 0) {
            int minUsage = globalSlotLoad.values().stream().min(Integer::compareTo).orElse(0);

            List<String> bestSlotKeys = globalSlotLoad.entrySet().stream()
                    .filter(e -> e.getValue() == minUsage)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            Collections.shuffle(bestSlotKeys); // Randomize among equally-used slots, else if best teacher and room alloation gets occpuied, it might end in an endless loop

            boolean placed = false;
            for (String slotKey : bestSlotKeys) {
                DayOfWeek day = DayOfWeek.valueOf(slotKey.split("\\|")[0]);
                LocalTime slot = LocalTime.parse(slotKey.split("\\|")[1]);

                //  Find least-loaded available teachers for this slot
                List<Teacher> teacherPool = teachersBySpec.get(course.getSpecialization().getId());
                if (teacherPool == null || teacherPool.isEmpty()) continue;
                List<Teacher> availableTeachers = teacherPool.stream()
                        .filter(t -> load.teacherDailyHours(t.getId(), day) < TEACHER_MAX_DAILY_HOURS
                                && !load.isTeacherBusy(t.getId(), day, slot)
                                && !load.wouldExceedConsecutiveHours(t.getId(), day, slot, 1))
                        .toList();
                if (availableTeachers.isEmpty()) continue;

                Teacher altTeacher = SchedulerUtils.pickLeastLoadedTeacher(load, availableTeachers);

                //  Find least-used available rooms for this slot
                List<Classroom> roomPool = roomsByType.get(course.getSpecialization().getRoomType().getId());
                if (roomPool == null || roomPool.isEmpty()) continue;
                List<Classroom> availableRooms = roomPool.stream()
                        .filter(r -> !load.isRoomBusy(r.getId(), day, slot))
                        .toList();
                if (availableRooms.isEmpty()) continue;

                Classroom altRoom = SchedulerUtils.pickLeastUsedRoom(availableRooms, result);

                // 4. Can we do a 2-hour block?
                boolean canTryTwoHours =
                        remainingHours >= 2
                                && slot.getHour() != 11
                                && slot.getHour() < 16
                                && !load.isTeacherBusy(altTeacher.getId(), day, slot.plusHours(1))
                                && !load.isRoomBusy(altRoom.getId(), day, slot.plusHours(1))
                                && !load.wouldExceedConsecutiveHours(altTeacher.getId(), day, slot, 2);

                int duration = canTryTwoHours ? 2 : 1;
                if (!canTryTwoHours &&
                        load.wouldExceedConsecutiveHours(altTeacher.getId(), day, slot, 1)) continue;

                createCourseSection(course, result, semester, altTeacher, altRoom, day, slot, duration);

                for (int h = 0; h < duration; h++) {
                    load.markPlaced(altTeacher.getId(), altRoom.getId(), day, slot.plusHours(h));
                }
                globalSlotLoad.merge(slotKey, duration, Integer::sum);
                remainingHours -= duration;
                placed = true;
                break;
            }
            if (!placed) {
                System.err.println("Could not schedule all hours for course: " + course.getName());
                break;
            }
        }
    }

    /**
     * Creates and adds a CourseSection instance to the result list.
     */
    private static void createCourseSection(Course course, List<CourseSection> result, Semester semester, Teacher altTeacher, Classroom altRoom, DayOfWeek day, LocalTime slot, int duration) {
        CourseSection sec = new CourseSection();
        sec.setCourse(course);
        sec.setTeacher(altTeacher);
        sec.setClassroom(altRoom);
        sec.setSemester(semester);
        sec.setDayOfWeek(day);
        sec.setStartTime(slot);
        sec.setEndTime(slot.plusHours(duration));
        result.add(sec);
    }


    /**
     * Initializes the global slot load map (day, time -> usage count).
     */
    private static Map<String, Integer> initializeGlobalSlotLoad() {
        Map<String, Integer> globalSlotLoad = new HashMap<>();
        for (DayOfWeek d : DAYS) {
            for (LocalTime t : SLOTS) {
                globalSlotLoad.put(d + "|" + t, 0);
            }
        }
        return globalSlotLoad;
    }

    /**
     * Calculates the number of weeks in a semester.
     * @param semester The semester entity.
     * @return The number of weeks in the semester.
     */
    private static int calculateWeeksInSemester(Semester semester) {
        if (semester.getStartDate() == null || semester.getEndDate() == null)
            throw new IllegalArgumentException("Semester start and end dates must be set");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                semester.getStartDate(), semester.getEndDate());
        return (int) Math.ceil(daysBetween / 7.0);
    }

    /**
     * Converts a CourseSection entity to its DTO representation.
     * @param section The CourseSection entity.
     * @return The corresponding ScheduleEventDTO.
     */
    private ScheduleEventDTO modelToDto(CourseSection section) {
        return new ScheduleEventDTO(
                section.getId(),
                section.getDayOfWeek(),
                section.getStartTime(),
                section.getEndTime(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName(),
                section.getClassroom().getName(),
                null
        );
    }




}
