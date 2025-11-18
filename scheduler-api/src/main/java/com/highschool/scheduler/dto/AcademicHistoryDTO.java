package com.highschool.scheduler.dto;

public record AcademicHistoryDTO(
        String semesterName,
        String courseName,
        String courseType,
        double credits,
        String status
) {
}