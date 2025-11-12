package com.highschool.scheduler.dto;


public record EnrollmentDTO(
        String dayOfWeek,
        String startTime,
        String endTime,
        String courseName,
        String teacherName,
        String roomName,
        String enrolledDate
) {
}
