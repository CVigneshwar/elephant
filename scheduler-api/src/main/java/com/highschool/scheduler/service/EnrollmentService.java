// com.highschool.scheduler.service.EnrollmentService
package com.highschool.scheduler.service;

import com.highschool.scheduler.dto.AcademicHistoryDTO;
import com.highschool.scheduler.dto.EligibleSectionDTO;
import com.highschool.scheduler.dto.EnrollmentDTO;
import com.highschool.scheduler.dto.ScheduleEventDTO;
import com.highschool.scheduler.dto.ValidationResponse;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.model.StudentCourseHistory;
import com.highschool.scheduler.model.StudentSectionEnrollment;
import com.highschool.scheduler.repository.CourseRepository;
import com.highschool.scheduler.repository.CourseSectionRepository;
import com.highschool.scheduler.repository.SemesterRepository;
import com.highschool.scheduler.repository.StudentCourseHistoryRepository;
import com.highschool.scheduler.repository.StudentRepository;
import com.highschool.scheduler.repository.StudentSectionEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final int MAX_COURSES_PER_SEMESTER = 5;
    public static final String STUDENT_NOT_FOUND = "Student not found";
    public static final String SEMESTER_NOT_FOUND = "Semester not found";
    public static final String PREREQUISITE_NOT_COMPLETED = "Prerequisite not completed";
    public static final String COURSE_SECTION_NOT_FOUND = "Course section not found";
    public static final String STUDENT_NOT_FOUND1 = "Student not found";
    public static final String ALREADY_ENROLLED_IN_THIS_SECTION = "Already enrolled in this section";
    public static final String ROOM_IS_FULL_FOR_SECTION_DATE = "Room is full for this section & date";
    public static final String SECTION_NOT_FOUND = "Section not found";
    public static final String ENROLLMENT_TIME_CONFLICT = "Time conflict with %s (%s %s-%s)";

    private final StudentRepository studentRepo;
    private final CourseSectionRepository sectionRepo;
    private final SemesterRepository semesterRepo;
    private final StudentSectionEnrollmentRepository enrollRepo;
    private final StudentCourseHistoryRepository historyRepo;
    private final CourseRepository courseRepo;

    public List<ScheduleEventDTO> getSchedule(Long studentId) {
        return enrollRepo.findByStudentId(studentId).stream()
                .map(e -> {
                    CourseSection s = e.getCourseSection();
                    return new ScheduleEventDTO(
                            s.getId(),
                            s.getDayOfWeek(),
                            s.getStartTime(),
                            s.getEndTime(),
                            s.getCourse().getCode(),
                            s.getCourse().getName(),
                            s.getTeacher().getFirstName() + " " + s.getTeacher().getLastName(),
                            s.getClassroom().getName(),
                            e.getEnrolledDate()
                    );
                })
                .toList();

    }

    public List<EligibleSectionDTO> getEligibleSections(Long studentId) {

        var student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOT_FOUND));

        var semester = semesterRepo.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalArgumentException(SEMESTER_NOT_FOUND));

        var sections = sectionRepo.findAll().stream()
                .filter(s -> Objects.equals(s.getSemester().getId(), semester.getId()))
                .toList();

        var history = historyRepo.findAll().stream()
                .filter(h -> h.getStudent().getId().equals(studentId))
                .toList();

        var enrolledSections = enrollRepo.findByStudentIdAndSemesterId(studentId, semester.getId());
        var enrolledCourseIds = enrolledSections.stream()
                .map(e -> e.getCourseSection().getCourse().getId())
                .collect(Collectors.toSet());

        List<EligibleSectionDTO> eligible = new ArrayList<>();

        for (var sec : sections) {
            var c = sec.getCourse();

            boolean passed = history.stream()
                    .anyMatch(h -> h.getCourse().getId().equals(c.getId()) &&
                            "passed".equalsIgnoreCase(h.getStatus()));
            if (passed || enrolledCourseIds.contains(c.getId())) continue;

            if (student.getGradeLevel() < c.getGradeLevelMin() ||
                    student.getGradeLevel() > c.getGradeLevelMax())
                continue;

            if (c.getPrerequisite() != null) {
                boolean prereqPassed = history.stream()
                        .anyMatch(h -> h.getCourse().getId().equals(c.getPrerequisite().getId()) &&
                                "passed".equalsIgnoreCase(h.getStatus()));
                if (!prereqPassed) continue;
            }

            int enrolledCount = (int) enrollRepo.findAll().stream()
                    .filter(e -> e.getCourseSection().getId().equals(sec.getId()))
                    .count();

            eligible.add(new EligibleSectionDTO(
                    sec.getId(),
                    c.getId(),
                    c.getName(),
                    sec.getTeacher().getFirstName() + " " + sec.getTeacher().getLastName(),
                    sec.getClassroom().getName(),
                    sec.getDayOfWeek().name(),
                    sec.getStartTime().toString(),
                    sec.getEndTime().toString(),
                    10,
                    enrolledCount,
                    c.getCourseType()
            ));
        }

        final List<String> DAY_ORDER = List.of(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
        );

        return eligible.stream()
                .sorted((a, b) -> {
                    int d1 = DAY_ORDER.indexOf(a.dayOfWeek());
                    int d2 = DAY_ORDER.indexOf(b.dayOfWeek());
                    if (d1 != d2) return d1 - d2;
                    return a.startTime().compareTo(b.startTime());
                })
                .toList();

    }



    // ---- Validate: conflicts with current schedule
    public ValidationResponse validateConflict(Long studentId, Long sectionId) {
        var target = sectionRepo.findById(sectionId).orElseThrow();
        var targetSemesterId = target.getSemester().getId();

        // Fetch all enrollments for the same semester
        var mine = enrollRepo.findByStudentIdAndSemesterId(studentId, targetSemesterId);

        List<String> errors = new ArrayList<>();

        for (var e : mine) {
            //  Skip if enrolled on different date (no conflict)
            if (e.getEnrolledDate() == null || !Objects.equals(e.getEnrolledDate(), target.getSemester().getStartDate())) {
                // We'll compare only if both share the same enrolled date
                continue;
            }

            // Check if same date and overlapping time
            if (overlap(e.getCourseSection(), target)) {
                var c = e.getCourseSection().getCourse();
                errors.add(String.format(
                        ENROLLMENT_TIME_CONFLICT,
                        c.getName(),
                        e.getCourseSection().getDayOfWeek(),
                        e.getCourseSection().getStartTime(),
                        e.getCourseSection().getEndTime()
                ));
            }
        }

        return new ValidationResponse(errors.isEmpty(), errors);
    }

    // ---- Validate: prerequisite completion
    public ValidationResponse validatePrerequisite(Long studentId, Long courseId) {
        boolean ok = hasSatisfiedPrerequisite(studentId, courseId);
        return new ValidationResponse(ok, ok ? List.of() : List.of(PREREQUISITE_NOT_COMPLETED));
    }

    // ---- Enroll
    @Transactional
    public StudentSectionEnrollment enroll(Long studentId, Long sectionId, LocalDate enrolledDate) {
        var section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(COURSE_SECTION_NOT_FOUND));
        var student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(STUDENT_NOT_FOUND1));
        var semester = section.getSemester();

        // check existing enrollment for same section
        if (enrollRepo.findByStudentAndSection(studentId, sectionId) != null) {
            throw new IllegalStateException(ALREADY_ENROLLED_IN_THIS_SECTION);
        }

        // check room capacity
        long enrolledCount = enrollRepo.countByCourseSectionIdAndEnrolledDate(sectionId, enrolledDate);
        if (enrolledCount >= section.getClassroom().getCapacity()) {
            throw new IllegalStateException(ROOM_IS_FULL_FOR_SECTION_DATE + " " + enrolledDate);
        }

        // Check semester course limit
        int planned = enrollRepo.countByStudentIdAndSemesterId(studentId, semester.getId());
        if (planned >= MAX_COURSES_PER_SEMESTER) {
            throw new IllegalStateException("Maximum of " + MAX_COURSES_PER_SEMESTER + " courses per semester reached.");
        }

        // conflict detection (if you have one)
        var conflict = validateConflict(studentId, sectionId);
        if (!conflict.ok()) {
            throw new IllegalStateException(conflict.errors().get(0));
        }

        // ✅ create new enrollment
        var enrollment = new StudentSectionEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourseSection(section);
        enrollment.setSemester(semester);
        enrollment.setEnrolledDate(enrolledDate);

        return enrollRepo.save(enrollment);
    }

    public List<LocalDate> getEligibleDatesForSection(Long sectionId) {
        CourseSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException(SECTION_NOT_FOUND));

        Semester semester = section.getSemester();
        int capacity = section.getClassroom().getCapacity();

        LocalDate start = semester.getStartDate();
        LocalDate end = semester.getEndDate();
        DayOfWeek day = section.getDayOfWeek();

        // ✅ List all dates in semester matching section’s DayOfWeek
        List<LocalDate> allDates = start.datesUntil(end.plusDays(1))
                .filter(d -> d.getDayOfWeek().equals(day))
                .toList();

        // ✅ Filter based on available seats
        return allDates.stream()
                .filter(d -> enrollRepo.countBySectionAndDate(sectionId, d) < capacity)
                .toList();
    }

    // ---- Unenroll
    @Transactional
    public void unenroll(Long enrollmentId) {
        enrollRepo.deleteById(enrollmentId);
    }

    public List<AcademicHistoryDTO> getAcademicHistory(Long studentId) {
        var histories = historyRepo.findByStudentId(studentId);
        return histories.stream()
                .sorted(Comparator.comparing(h -> h.getSemester().getStartDate()))
                .map(h -> new AcademicHistoryDTO(
                        h.getSemester().getName(),
                        h.getCourse().getName(),
                        h.getCourse().getCourseType(),
                        h.getCourse().getCredits(),
                        h.getStatus()
                ))
                .toList();
    }

    public List<EnrollmentDTO> getCurrentEnrollments(Long studentId) {
        var semester = semesterRepo.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active semester found"));

        return enrollRepo.findByStudentIdAndSemesterId(studentId, semester.getId())
                .stream()
                .map(e -> {
                    var sec = e.getCourseSection();
                    return new EnrollmentDTO(
                            sec.getDayOfWeek().name(),
                            sec.getStartTime().toString(),
                            sec.getEndTime().toString(),
                            sec.getCourse().getName(),
                            sec.getTeacher().getFirstName() + " " + sec.getTeacher().getLastName(),
                            sec.getClassroom().getName(),
                            e.getEnrolledDate().toString()
                    );
                })
                .sorted(Comparator
                        .comparing(EnrollmentDTO::dayOfWeek)
                        .thenComparing(EnrollmentDTO::startTime))
                .toList();
    }


    // ---- Progress (GPA + credits)
    public Map<String, Object> getProgress(Long studentId) {
        var student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        var histories = historyRepo.findByStudentId(studentId);
        var enrollments = enrollRepo.findByStudentId(studentId);

        int totalCredits = histories.stream()
                .filter(h -> "passed".equalsIgnoreCase(h.getStatus()))
                .mapToInt(h -> h.getCourse().getCredits())
                .sum();

        double gpa = histories.stream()
                .filter(h -> "passed".equalsIgnoreCase(h.getStatus()) || "failed".equalsIgnoreCase(h.getStatus()))
                .mapToDouble(h -> "passed".equalsIgnoreCase(h.getStatus()) ? 4.0 : 0.0)
                .average().orElse(0.0);

        int creditsRequired = 30;
        int creditsRemaining = Math.max(0, creditsRequired - totalCredits);
        double completion = ((double) totalCredits / creditsRequired) * 100;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentId", student.getId());
        result.put("studentName", student.getFirstName() + " " + student.getLastName());
        result.put("email", student.getEmail());
        result.put("gradeLevel", student.getGradeLevel());
        result.put("gpa", gpa);
        result.put("creditsEarned", totalCredits);
        result.put("creditsRequired", creditsRequired);
        result.put("creditsRemaining", creditsRemaining);
        result.put("completionPercentage", completion);
        result.put("plannedThisSemester", enrollments.size());
        result.put("maxCoursesReached", enrollments.size() >= 5);

        return result;
    }

    public double calculateGpaFromPassFail(List<StudentCourseHistory> histories) {
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (var h : histories) {
            int credits = h.getCourse().getCredits();
            totalCredits += credits;

            if ("passed".equalsIgnoreCase(h.getStatus())) {
                totalPoints += 4.0 * credits; // treat pass as full grade
            } else if ("failed".equalsIgnoreCase(h.getStatus())) {
                totalPoints += 0.0;
            }
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }


    private double mapGradeToPoints(String grade) {
        return switch (grade.toUpperCase()) {
            case "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }


    // ----- helpers -----
    private boolean overlap(CourseSection a, CourseSection b) {
        if (a.getDayOfWeek() != b.getDayOfWeek()) return false;
        return !(!a.getEndTime().isAfter(b.getStartTime())
                || !b.getEndTime().isAfter(a.getStartTime()));
    }

    private boolean hasSatisfiedPrerequisite(Long studentId, Long courseId) {
        var course = courseRepo.findById(courseId).orElseThrow();
        var prereq = course.getPrerequisite();
        if (prereq == null) return true;

        return historyRepo.findAll().stream()
                .filter(h -> h.getStudent().getId().equals(studentId))
                .anyMatch(h -> h.getCourse().getId().equals(prereq.getId())
                        && "passed".equalsIgnoreCase(h.getStatus()));
    }









}
