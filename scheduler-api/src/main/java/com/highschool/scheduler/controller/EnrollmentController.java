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

/**
 * REST controller for managing student enrollments, schedules, and eligibility.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Retrieves the current schedule for a given student.
     *
     * @param studentId the ID of the student
     * @return a list of {@link ScheduleEventDTO} representing the student's schedule
     */
    @GetMapping("/{studentId}/schedule")
    public List<ScheduleEventDTO> schedule(@PathVariable Long studentId) {
        log.debug("Fetching schedule for student {}", studentId);
        return enrollmentService.getSchedule(studentId);
    }

    /**
     * Retrieves the academic progress information for a given student.
     *
     * @param studentId the ID of the student
     * @return a map containing progress data for the student
     */
    @GetMapping("/{studentId}/progress")
    public Map<String, Object> progress(@PathVariable Long studentId) {
        log.debug("Fetching progress for student {}", studentId);
        return enrollmentService.getProgress(studentId);
    }

    /**
     * Retrieves all eligible course sections for a given student.
     *
     * @param studentId the ID of the student
     * @return a list of {@link EligibleSectionDTO} the student can enroll in
     */
    @GetMapping("/{studentId}/eligible-sections")
    public List<EligibleSectionDTO> getEligibleSections(@PathVariable Long studentId) {
        log.debug("Fetching eligible sections for student {}", studentId);
        return enrollmentService.getEligibleSections(studentId);
    }

    /**
     * Validates if enrolling a student in a course section would result in a scheduling conflict.
     *
     * @param enrollmentRequest the enrollment request DTO containing student and section info
     * @return a {@link ValidationResponse} indicating if a conflict exists
     */
    @PostMapping("/{studentId}/validate-conflict")
    public ValidationResponse validateConflict(@RequestBody EnrollmentRequestDTO enrollmentRequest) {

        log.debug("Validating conflict for student {} on section {}", enrollmentRequest.studentId(), enrollmentRequest.sectionId());
        return enrollmentService.validateConflict(enrollmentRequest.studentId(), enrollmentRequest.sectionId(), enrollmentRequest.enrolledDate());
    }

    /**
     * Validates if a student has met the prerequisites for a course.
     *
     * @param studentId the ID of the student
     * @param body a map containing the courseId key
     * @return a {@link ValidationResponse} indicating if prerequisites are satisfied
     */
    @PostMapping("/{studentId}/validate-prereq")
    public ValidationResponse validatePrereq(
            @PathVariable Long studentId,
            @RequestBody Map<String, Long> body) {

        Long courseId = body.get("courseId");
        log.debug("Validating prerequisite for student {} on course {}", studentId, courseId);
        return enrollmentService.validatePrerequisite(studentId, courseId);
    }

    /**
     * Retrieves the eligible enrollment dates for a specific course section.
     *
     * @param sectionId the ID of the course section
     * @return a list of {@link LocalDate} objects representing eligible dates
     */
    @GetMapping("/course-sections/{sectionId}/eligible-dates")
    public List<LocalDate> getEligibleDatesForCourseSection(@PathVariable Long sectionId) {
        log.debug("Fetching eligible dates for course section {}", sectionId);
        return enrollmentService.getEligibleDatesForSection(sectionId);
    }

    /**
     * Enrolls a student in a course section on a specific date.
     *
     * @param req the enrollment request DTO
     * @return the created {@link StudentSectionEnrollment} entity
     */
    @PostMapping("/{studentId}/enroll")
    public StudentSectionEnrollment enroll(@RequestBody EnrollmentRequestDTO req) {
        log.info("Enrolling student {} to section {} on date {}",
                req.studentId(), req.sectionId(), req.enrolledDate());
        return enrollmentService.enroll(req.studentId(), req.sectionId(), req.enrolledDate());
    }

    /**
     * Retrieves the complete academic history for a student.
     *
     * @param id the ID of the student
     * @return a list of {@link AcademicHistoryDTO} representing the student's history
     */
    @GetMapping("/{id}/history")
    public List<AcademicHistoryDTO> getAcademicHistory(@PathVariable Long id) {
        log.debug("Fetching academic history for student {}", id);
        return enrollmentService.getAcademicHistory(id);
    }

    /**
     * Retrieves the current enrollments for a student.
     *
     * @param id the ID of the student
     * @return a list of {@link EnrollmentDTO} representing the student's current enrollments
     */
    @GetMapping("/{id}/enrollments/current")
    public List<EnrollmentDTO> getCurrentEnrollments(@PathVariable Long id) {
        log.debug("Fetching current enrollments for student {}", id);
        return enrollmentService.getCurrentEnrollments(id);
    }
}