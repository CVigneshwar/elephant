package com.highschool.scheduler.dto;

public record EligibleSectionDTO(
        Long id,
        Long courseId,
        String courseName,
        String teacherName,
        String roomName,
        String dayOfWeek,
        String startTime,
        String endTime,
        int capacity,
        int enrolled,
        String courseType
) {}
