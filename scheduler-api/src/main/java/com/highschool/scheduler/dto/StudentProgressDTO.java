package com.highschool.scheduler.dto;

public record StudentProgressDTO(
        double gpa,
        int creditsEarned,
        int creditsRequired,
        int creditsRemaining,
        int plannedThisSemester,
        boolean maxCoursesReached,
        long coreCoursesCompleted,
        long electiveCoursesCompleted,
        double completionPercentage
) {
}
