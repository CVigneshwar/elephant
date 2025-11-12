package com.highschool.scheduler.dto;

public record AcademicHistoryDTO(
        String semesterName,
        String courseName,
        String courseType,
        int credits,
        String status
) {
}