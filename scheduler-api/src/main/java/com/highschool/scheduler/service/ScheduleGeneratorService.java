package com.highschool.scheduler.service;

import com.highschool.scheduler.dto.ScheduleEventDTO;
import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.model.Student;
import com.highschool.scheduler.model.StudentCourseHistory;
import com.highschool.scheduler.model.Teacher;
import com.highschool.scheduler.repository.ClassroomRepository;
import com.highschool.scheduler.repository.CourseRepository;
import com.highschool.scheduler.repository.CourseSectionRepository;
import com.highschool.scheduler.repository.SemesterRepository;
import com.highschool.scheduler.repository.StudentCourseHistoryRepository;
import com.highschool.scheduler.repository.StudentRepository;
import com.highschool.scheduler.repository.StudentSectionEnrollmentRepository;
import com.highschool.scheduler.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScheduleGeneratorService {

    private static final int ROOM_CAPACITY = 10;
    private static final int TEACHER_MAX_DAILY_HOURS = 4;
    private static final int MAX_CONSECUTIVE_HOURS = 2;

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

    private final CourseRepository courseRepo;
    private final TeacherRepository teacherRepo;
    private final ClassroomRepository classroomRepo;
    private final SemesterRepository semesterRepo;
    private final CourseSectionRepository sectionRepo;
    private final StudentRepository studentRepo;
    private final StudentCourseHistoryRepository historyRepo;
    private final StudentSectionEnrollmentRepository studentEnrollmentRepo;

    public ScheduleGeneratorService(CourseRepository courseRepo,
                                    TeacherRepository teacherRepo,
                                    ClassroomRepository classroomRepo,
                                    SemesterRepository semesterRepo,
                                    CourseSectionRepository sectionRepo,
                                    StudentRepository studentRepo,
                                    StudentCourseHistoryRepository historyRepo,
                                    StudentSectionEnrollmentRepository studentEnrollmentRepo) {
        this.courseRepo = courseRepo;
        this.teacherRepo = teacherRepo;
        this.classroomRepo = classroomRepo;
        this.semesterRepo = semesterRepo;
        this.sectionRepo = sectionRepo;
        this.studentRepo = studentRepo;
        this.historyRepo = historyRepo;
        this.studentEnrollmentRepo = studentEnrollmentRepo;
    }

    @Transactional
    public List<ScheduleEventDTO> generateForActiveSemester() {
        Semester semester = semesterRepo.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active semester found"));

        // Remove existing course sections for active semester
        List<CourseSection> existing = sectionRepo.findAll().stream()
                .filter(s -> Objects.equals(s.getSemester().getId(), semester.getId()))
                .toList();
        if (!existing.isEmpty()) sectionRepo.deleteAllInBatch(existing);

        // Fetch only courses belonging to active semester order
        List<Course> courses = courseRepo.findBySemesterOrder(semester.getOrderInYear());
        if (courses.isEmpty()) return List.of();

        List<Teacher> teachers = teacherRepo.findAll();
        List<Classroom> rooms = classroomRepo.findAll();
        DemandEstimator demand = new DemandEstimator(studentRepo.findAll(), historyRepo.findAll());
        TeacherLoadTracker load = new TeacherLoadTracker();

        Map<Long, List<Teacher>> teachersBySpec = teachers.stream()
                .filter(t -> t.getSpecialization() != null)
                .collect(Collectors.groupingBy(t -> t.getSpecialization().getId()));

        Map<Long, List<Classroom>> roomsByType = rooms.stream()
                .filter(r -> r.getRoomType() != null)
                .collect(Collectors.groupingBy(r -> r.getRoomType().getId()));

        // 🧠 New map for (day + time) level balancing
        Map<String, Integer> globalSlotLoad = new HashMap<>();
        for (DayOfWeek d : DAYS) {
            for (LocalTime t : SLOTS) {
                globalSlotLoad.put(d + "|" + t, 0);
            }
        }

        List<CourseSection> result = new ArrayList<>();
        AtomicInteger courseCounter = new AtomicInteger(0);
        int weeksInSemester = calculateWeeksInSemester(semester);

        courses.forEach(course -> {
            int eligible = demand.eligibleCount(course);
            int sectionsNeeded = Math.max(1,
                    (int) Math.ceil((double) eligible / (ROOM_CAPACITY * weeksInSemester)));

            if (course.getHoursPerWeek() < sectionsNeeded) {
                throw new IllegalStateException("hoursPerWeek needs to be increased for course - " + course.getName());
            }

            Long specId = course.getSpecialization() != null ? course.getSpecialization().getId() : null;
            if (specId == null) return;

            List<Teacher> pool = teachersBySpec.getOrDefault(specId, List.of());
            List<Classroom> roomPool = roomsByType.getOrDefault(
                    course.getSpecialization().getRoomType() != null ?
                            course.getSpecialization().getRoomType().getId() : -1L,
                    List.of());
            if (pool.isEmpty() || roomPool.isEmpty()) return;

            pool.sort(Comparator.comparingInt(t -> load.weeklyHours(t.getId())));
            Teacher teacher = pool.get(0);
            Classroom room = roomPool.get(0);

            scheduleWeeklyHoursBalanced(course, teacher, room, semester,
                    course.getHoursPerWeek(), load, result,
                    teachersBySpec, roomsByType, globalSlotLoad);
        });

        List<CourseSection> courseSections = sectionRepo.saveAll(result);
        return courseSections.stream().map(this::modelToDto).toList();
    }

    private void scheduleWeeklyHoursBalanced(Course course,
                                             Teacher teacher,
                                             Classroom room,
                                             Semester semester,
                                             int weeklyHours,
                                             TeacherLoadTracker load,
                                             List<CourseSection> result,
                                             Map<Long, List<Teacher>> teachersBySpec,
                                             Map<Long, List<Classroom>> roomsByType,
                                             Map<String, Integer> globalSlotLoad) {

        int remainingHours = weeklyHours;

        while (remainingHours > 0) {
            // Pick least loaded (day, slot) combination globally
            String bestSlotKey = globalSlotLoad.entrySet().stream()
                    .min(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(DayOfWeek.MONDAY + "|09:00");

            DayOfWeek day = DayOfWeek.valueOf(bestSlotKey.split("\\|")[0]);
            LocalTime slot = LocalTime.parse(bestSlotKey.split("\\|")[1]);

            boolean placed = false;

            for (Teacher altTeacher : teachersBySpec.get(course.getSpecialization().getId())) {
                if (load.teacherDailyHours(altTeacher.getId(), day) >= TEACHER_MAX_DAILY_HOURS)
                    continue;

                for (Classroom altRoom : roomsByType.get(course.getSpecialization().getRoomType().getId())) {
                    if (remainingHours <= 0) break;

                    if (load.isTeacherBusy(altTeacher.getId(), day, slot)) continue;
                    if (load.isRoomBusy(altRoom.getId(), day, slot)) continue;
                    if (load.wouldExceedConsecutiveHours(altTeacher.getId(), day, slot, 1)) continue;

                    boolean canTryTwoHours =
                            remainingHours >= 2 &&
                                    slot.getHour() != 11 && // not starting at 11 (would cross lunch)
                                    slot.getHour() < 16 &&  // leaves room for +1 hour
                                    !load.isTeacherBusy(altTeacher.getId(), day, slot.plusHours(1)) &&
                                    !load.isRoomBusy(altRoom.getId(), day, slot.plusHours(1)) &&
                                    !load.wouldExceedConsecutiveHours(altTeacher.getId(), day, slot, 2);

                    int duration;
                    if (canTryTwoHours) {
                        duration = 2;
                    } else {
                        // If even a 1-hour block would exceed consecutive limit, skip this slot
                        if (load.wouldExceedConsecutiveHours(altTeacher.getId(), day, slot, 1)) continue;
                        duration = 1;
                    }


                    CourseSection sec = new CourseSection();
                    sec.setCourse(course);
                    sec.setTeacher(altTeacher);
                    sec.setClassroom(altRoom);
                    sec.setSemester(semester);
                    sec.setDayOfWeek(day);
                    sec.setStartTime(slot);
                    sec.setEndTime(slot.plusHours(duration));
                    result.add(sec);

                    for (int h = 0; h < duration; h++) {
                        load.markPlaced(altTeacher.getId(), altRoom.getId(), day, slot.plusHours(h));
                    }

                    globalSlotLoad.merge(day + "|" + slot, duration, Integer::sum);

                    remainingHours -= duration;
                    placed = true;
                    break;
                }
                if (placed) break;
            }

            if (!placed) {
                globalSlotLoad.merge(day + "|" + slot, 1, Integer::sum);
                if (globalSlotLoad.values().stream().allMatch(v -> v > 0)) {
                    System.err.println("⚠ Could not schedule all hours for course: " + course.getName());
                    break;
                }
            }
        }
    }



    private int calculateWeeksInSemester(Semester semester) {
        if (semester.getStartDate() == null || semester.getEndDate() == null)
            throw new IllegalArgumentException("Semester start and end dates must be set");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                semester.getStartDate(), semester.getEndDate());
        return (int) Math.ceil(daysBetween / 7.0);
    }

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

    @Transactional
    public void resetSchedule() {
        studentEnrollmentRepo.deleteAllByActiveSemester();
        sectionRepo.deleteAllByActiveSemester();
        System.out.println("🧹 Cleared course sections and enrollments for active semester only.");
    }

    public List<ScheduleEventDTO> getSchedule() {
        return sectionRepo.findAll().stream().map(this::modelToDto).collect(Collectors.toList());
    }
    // ---- Helper Classes ----

    private static class TeacherLoadTracker {
        private final Map<Long, Map<DayOfWeek, Set<LocalTime>>> teacherSlots = new HashMap<>();
        private final Map<Long, Map<DayOfWeek, Integer>> teacherDaily = new HashMap<>();
        private final Map<Long, Integer> teacherWeekly = new HashMap<>();
        private final Set<String> roomBusy = new HashSet<>();

        boolean isTeacherBusy(Long teacherId, DayOfWeek day, LocalTime slot) {
            return teacherSlots.getOrDefault(teacherId, Map.of())
                    .getOrDefault(day, Set.of()).contains(slot);
        }

        boolean isRoomBusy(Long roomId, DayOfWeek day, LocalTime slot) {
            return roomBusy.contains(key(roomId, day, slot));
        }

        int weeklyHours(Long teacherId) {
            return teacherWeekly.getOrDefault(teacherId, 0);
        }

        int teacherDailyHours(Long teacherId, DayOfWeek day) {
            return teacherDaily.getOrDefault(teacherId, Map.of())
                    .getOrDefault(day, 0);
        }

        void markPlaced(Long teacherId, Long roomId, DayOfWeek day, LocalTime slot) {
            teacherSlots.computeIfAbsent(teacherId, k -> new EnumMap<>(DayOfWeek.class))
                    .computeIfAbsent(day, k -> new HashSet<>()).add(slot);

            teacherDaily.computeIfAbsent(teacherId, k -> new EnumMap<>(DayOfWeek.class))
                    .merge(day, 1, Integer::sum);

            teacherWeekly.merge(teacherId, 1, Integer::sum);
            roomBusy.add(key(roomId, day, slot));
        }

        boolean wouldExceedConsecutiveHours(Long teacherId, DayOfWeek day, LocalTime proposedStart, int duration) {
            Set<LocalTime> occupied = teacherSlots
                    .getOrDefault(teacherId, Map.of())
                    .getOrDefault(day, Set.of());

            // Build list of hours this session would occupy
            List<LocalTime> newSlots = new ArrayList<>();
            for (int i = 0; i < duration; i++) newSlots.add(proposedStart.plusHours(i));

            // Combine occupied + new slots
            List<Integer> hours = occupied.stream()
                    .map(LocalTime::getHour)
                    .collect(Collectors.toCollection(ArrayList::new));
            newSlots.forEach(s -> hours.add(s.getHour()));

            Collections.sort(hours);

            // Count consecutive hours
            int maxConsecutive = 1, current = 1;
            for (int i = 1; i < hours.size(); i++) {
                if (hours.get(i) == hours.get(i - 1) + 1) {
                    current++;
                    maxConsecutive = Math.max(maxConsecutive, current);
                } else {
                    current = 1;
                }
            }
            return maxConsecutive > MAX_CONSECUTIVE_HOURS;
        }
        private static String key(Long id, DayOfWeek d, LocalTime t) {
            return id + "|" + d + "|" + t;
        }
    }

    private static class DemandEstimator {
        private final List<Student> students;
        private final Map<Long, List<StudentCourseHistory>> historyByStudent;

        DemandEstimator(List<Student> students, List<StudentCourseHistory> histories) {
            this.students = students;
            this.historyByStudent = histories.stream()
                    .collect(Collectors.groupingBy(h -> h.getStudent().getId()));
        }

        int eligibleCount(Course course) {
            return (int) students.stream()
                    .filter(s -> withinGradeRange(s, course))
                    .filter(s -> hasPrerequisitePassed(s, course))
                    .filter(s -> notAlreadyPassed(s, course))
                    .count();
        }

        private boolean withinGradeRange(Student s, Course c) {
            return s.getGradeLevel() >= c.getGradeLevelMin()
                    && s.getGradeLevel() <= c.getGradeLevelMax();
        }

        private boolean hasPrerequisitePassed(Student s, Course c) {
            if (c.getPrerequisite() == null) return true;
            return historyByStudent.getOrDefault(s.getId(), List.of()).stream()
                    .anyMatch(h -> Objects.equals(h.getCourse().getId(), c.getPrerequisite().getId())
                            && "passed".equalsIgnoreCase(h.getStatus()));
        }

        private boolean notAlreadyPassed(Student s, Course c) {
            return historyByStudent.getOrDefault(s.getId(), List.of()).stream()
                    .noneMatch(h -> Objects.equals(h.getCourse().getId(), c.getId())
                            && "passed".equalsIgnoreCase(h.getStatus()));
        }
    }
}
