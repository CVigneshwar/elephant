package com.highschool.scheduler.service.util;

import com.highschool.scheduler.model.Teacher;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tracks and manages teaching loads, schedules, and room usage
 * for teachers during schedule generation.
 * Provides utilities to check teacher and room availability,
 * teaching hours, and enforce consecutive hour constraints.
 */
public final class TeacherLoadTracker {
    private final Map<Long, Map<DayOfWeek, Set<LocalTime>>> teacherSlots = new HashMap<>();
    private final Map<Long, Map<DayOfWeek, Integer>> teacherDaily = new HashMap<>();
    private final Map<Long, Integer> teacherWeekly = new HashMap<>();
    private final Set<String> roomBusy = new HashSet<>();

    private static final int MAX_CONSECUTIVE_HOURS = 2;

    /**
     * Checks if a teacher is busy (already scheduled) at the given day and time slot.
     *
     * @param teacherId the teacher's unique ID
     * @param day the day of week
     * @param slot the start time slot
     * @return true if the teacher is busy at this slot, false otherwise
     */
    public boolean isTeacherBusy(Long teacherId, DayOfWeek day, LocalTime slot) {
        return teacherSlots.getOrDefault(teacherId, Map.of())
                .getOrDefault(day, Set.of()).contains(slot);
    }

    /**
     * Checks if a room is busy (already scheduled) at the given day and time slot.
     *
     * @param roomId the room's unique ID
     * @param day the day of week
     * @param slot the start time slot
     * @return true if the room is busy at this slot, false otherwise
     */
    public boolean isRoomBusy(Long roomId, DayOfWeek day, LocalTime slot) {
        return roomBusy.contains(key(roomId, day, slot));
    }

    /**
     * Gets the total number of hours assigned to a teacher for the week.
     *
     * @param teacherId the teacher's unique ID
     * @return the number of scheduled hours for the teacher this week
     */
    public int weeklyHours(Long teacherId) {
        return teacherWeekly.getOrDefault(teacherId, 0);
    }

    /**
     * Gets the number of hours a teacher is assigned on a particular day.
     *
     * @param teacherId the teacher's unique ID
     * @param day the day of week
     * @return the number of scheduled hours for the teacher on that day
     */
    public int teacherDailyHours(Long teacherId, DayOfWeek day) {
        return teacherDaily.getOrDefault(teacherId, Map.of())
                .getOrDefault(day, 0);
    }

    /**
     * Marks a teacher and room as scheduled/occupied at the given day and time slot.
     * Increments the relevant teaching hour counters.
     *
     * @param teacherId the teacher's unique ID
     * @param roomId the room's unique ID
     * @param day the day of week
     * @param slot the start time slot
     */
    public void markPlaced(Long teacherId, Long roomId, DayOfWeek day, LocalTime slot) {
        teacherSlots.computeIfAbsent(teacherId, k -> new EnumMap<>(DayOfWeek.class))
                .computeIfAbsent(day, k -> new HashSet<>()).add(slot);

        teacherDaily.computeIfAbsent(teacherId, k -> new EnumMap<>(DayOfWeek.class))
                .merge(day, 1, Integer::sum);

        teacherWeekly.merge(teacherId, 1, Integer::sum);
        roomBusy.add(key(roomId, day, slot));
    }

    /**
     * Checks if scheduling a teacher for a block of consecutive hours would exceed the allowed maximum.
     *
     * @param teacherId the teacher's unique ID
     * @param day the day of week
     * @param proposedStart the proposed starting time
     * @param duration the number of consecutive hours to check
     * @return true if this assignment would exceed the maximum consecutive hours, false otherwise
     */
    public boolean wouldExceedConsecutiveHours(Long teacherId, DayOfWeek day, LocalTime proposedStart, int duration) {
        Set<LocalTime> occupied = teacherSlots
                .getOrDefault(teacherId, Map.of())
                .getOrDefault(day, Set.of());

        // Build list of hours this session would occupy
        List<LocalTime> newSlots = new ArrayList<>();
        for (int i = 0; i < duration; i++) newSlots.add(proposedStart.plusHours(i));

        // Combine occupied + new slots
        List<Integer> hours = occupied.stream()
                .map(LocalTime::getHour)
                .collect(Collectors.toCollection(ArrayList::new));
        newSlots.forEach(s -> hours.add(s.getHour()));

        Collections.sort(hours);

        // Count consecutive hours
        int maxConsecutive = 1, current = 1;
        for (int i = 1; i < hours.size(); i++) {
            if (hours.get(i) == hours.get(i - 1) + 1) {
                current++;
                maxConsecutive = Math.max(maxConsecutive, current);
            } else {
                current = 1;
            }
        }
        return maxConsecutive > MAX_CONSECUTIVE_HOURS;
    }

    /**
     * Builds a unique key string for use in room schedule tracking.
     *
     * @param id the room or teacher ID
     * @param dayOfWeek the day of week
     * @param timeSlot the time slot
     * @return a unique string representing the resource, day, and time
     */
    private static String key(Long id, DayOfWeek dayOfWeek, LocalTime timeSlot) {
        return id + "|" + dayOfWeek + "|" + timeSlot;
    }
}
