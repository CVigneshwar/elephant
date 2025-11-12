package com.highschool.scheduler.dto;

public record SectionDTO(
        Long id, String dayOfWeek, String startTime, String endTime,
        Long courseId, String courseCode, String courseName, Integer credits,
        String teacherName, String roomName
) {
}

