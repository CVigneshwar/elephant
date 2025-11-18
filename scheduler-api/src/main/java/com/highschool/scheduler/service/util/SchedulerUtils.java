package com.highschool.scheduler.service.util;

import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.model.Teacher;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for scheduling-related selection logic,
 * such as picking least-used rooms or least-loaded teachers.
 */
public final class SchedulerUtils {

    /**
     * Selects and returns a classroom from the list of available rooms
     * that has been used the least number of times in the current schedule.
     * If there is a tie, one of the least-used rooms is chosen at random.
     *
     * @param availableRooms      List of available {@link Classroom} objects.
     * @param scheduledSections   List of already scheduled {@link CourseSection} objects.
     * @return a {@link Classroom} that is least used, or {@code null} if the list is empty.
     */
    public static Classroom pickLeastUsedRoom(List<Classroom> availableRooms, List<CourseSection> scheduledSections) {
        int minUsage = availableRooms.stream()
                .mapToInt(r -> countRoomUsage(r, scheduledSections))
                .min()
                .orElse(0);

        List<Classroom> leastUsed = availableRooms.stream()
                .filter(r -> countRoomUsage(r, scheduledSections) == minUsage)
                .collect(Collectors.toList());

        Collections.shuffle(leastUsed);
        return leastUsed.isEmpty() ? null : leastUsed.get(0);
    }

    /**
     * Selects and returns a teacher from the list of available teachers
     * who has the lowest total weekly assigned hours (as tracked by {@link TeacherLoadTracker}).
     * If there is a tie, one of the least-loaded teachers is chosen at random.
     *
     * @param load               The {@link TeacherLoadTracker} instance for querying teacher loads.
     * @param availableTeachers  List of available {@link Teacher} objects.
     * @return a {@link Teacher} with the least load, or {@code null} if the list is empty.
     */
    public static Teacher pickLeastLoadedTeacher(TeacherLoadTracker load, List<Teacher> availableTeachers) {
        int minLoad = availableTeachers.stream()
                .mapToInt(t -> load.weeklyHours(t.getId()))
                .min()
                .orElse(0);

        List<Teacher> leastLoaded = availableTeachers.stream()
                .filter(t -> load.weeklyHours(t.getId()) == minLoad)
                .collect(Collectors.toList());

        Collections.shuffle(leastLoaded);
        return leastLoaded.isEmpty() ? null : leastLoaded.get(0);
    }

    /**
     * Counts how many times a given classroom appears in the list of scheduled sections.
     *
     * @param room      The {@link Classroom} to check.
     * @param sections  List of {@link CourseSection} objects to search.
     * @return The number of times {@code room} is assigned in {@code sections}.
     */
    private static int countRoomUsage(Classroom room, List<CourseSection> sections) {
        return (int) sections.stream().filter(s -> s.getClassroom().equals(room)).count();
    }
}
