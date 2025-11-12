package com.highschool.scheduler.dto;

import java.time.LocalDate;

public record EnrollmentRequestDTO(
        Long studentId,
        Long sectionId,
        LocalDate enrolledDate
) {
}