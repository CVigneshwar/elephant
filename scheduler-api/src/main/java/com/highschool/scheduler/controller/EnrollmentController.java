package com.highschool.scheduler.controller;

import com.highschool.scheduler.dto.*;
import com.highschool.scheduler.model.StudentSectionEnrollment;
import com.highschool.scheduler.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/{studentId}/schedule")
    public List<ScheduleEventDTO> schedule(@PathVariable Long studentId) {
        log.debug("Fetching schedule for student {}", studentId);
        return enrollmentService.getSchedule(studentId);
    }

    @GetMapping("/{studentId}/progress")
    public Map<String, Object> progress(@PathVariable Long studentId) {
        log.debug("Fetching progress for student {}", studentId);
        return enrollmentService.getProgress(studentId);
    }

    @GetMapping("/{studentId}/eligible-sections")
    public List<EligibleSectionDTO> getEligibleSections(@PathVariable Long studentId) {
        log.debug("Fetching eligible sections for student {}", studentId);
        return enrollmentService.getEligibleSections(studentId);
    }

    @PostMapping("/{studentId}/validate-conflict")
    public ValidationResponse validateConflict(@RequestBody EnrollmentRequestDTO enrollmentRequest) {

        log.debug("Validating conflict for student {} on section {}", enrollmentRequest.studentId(), enrollmentRequest.sectionId());
        return enrollmentService.validateConflict(enrollmentRequest.studentId(), enrollmentRequest.sectionId(), enrollmentRequest.enrolledDate());
    }

    @PostMapping("/{studentId}/validate-prereq")
    public ValidationResponse validatePrereq(
            @PathVariable Long studentId,
            @RequestBody Map<String, Long> body) {

        Long courseId = body.get("courseId");
        log.debug("Validating prerequisite for student {} on course {}", studentId, courseId);
        return enrollmentService.validatePrerequisite(studentId, courseId);
    }

    @GetMapping("/course-sections/{sectionId}/eligible-dates")
    public List<LocalDate> getEligibleDatesForCourseSection(@PathVariable Long sectionId) {
        log.debug("Fetching eligible dates for course section {}", sectionId);
        return enrollmentService.getEligibleDatesForSection(sectionId);
    }

    @PostMapping("/{studentId}/enroll")
    public StudentSectionEnrollment enroll(@RequestBody EnrollmentRequestDTO req) {
        log.info("Enrolling student {} to section {} on date {}",
                req.studentId(), req.sectionId(), req.enrolledDate());
        return enrollmentService.enroll(req.studentId(), req.sectionId(), req.enrolledDate());
    }

    @GetMapping("/{id}/history")
    public List<AcademicHistoryDTO> getAcademicHistory(@PathVariable Long id) {
        log.debug("Fetching academic history for student {}", id);
        return enrollmentService.getAcademicHistory(id);
    }

    @GetMapping("/{id}/enrollments/current")
    public List<EnrollmentDTO> getCurrentEnrollments(@PathVariable Long id) {
        log.debug("Fetching current enrollments for student {}", id);
        return enrollmentService.getCurrentEnrollments(id);
    }
}
