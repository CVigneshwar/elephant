package com.highschool.scheduler.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleEventDTO(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String courseCode,
        String courseName,
        String teacherName,
        String roomName,
        LocalDate enrolledDate

) {
}
