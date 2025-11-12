package com.highschool.scheduler.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record UtilizationDTO(
        SummaryStats summary,
        List<ResourceUsage> teacherUsage,
        List<ResourceUsage> roomUsage,
        List<DayUsage> dayUsage,
        List<TimeSlotUsage> timeSlotUsage
) {
    public sealed interface Usage permits ResourceUsage, DayUsage, TimeSlotUsage {
        String label();
        double percent();
    }

    public record ResourceUsage(
            Long id,
            String name,
            double used,
            double max,
            double percent
    ) implements Usage {
        public String label() { return name; }
    }

    public record DayUsage(
            DayOfWeek day,
            double used,
            double percent
    ) implements Usage {
        public String label() { return day.name(); }
    }

    public record TimeSlotUsage(
            LocalTime slot,
            double used,
            double percent
    ) implements Usage {
        public String label() { return slot.toString(); }
    }

    public record SummaryStats(
            double avgTeacherUtil,
            double avgRoomUtil,
            double avgDayLoad,
            double avgSlotLoad,
            String mostLoadedDay,
            String leastLoadedDay,
            String mostLoadedSlot,
            String leastLoadedSlot
    ) {}
}

