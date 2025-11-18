package com.highschool.scheduler.service.util;

import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.model.Student;
import com.highschool.scheduler.model.StudentCourseHistory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CourseEligibilityCalculator {
    private final List<Student> students;
    private final Map<Long, List<StudentCourseHistory>> historyByStudent;

    public CourseEligibilityCalculator(List<Student> students, List<StudentCourseHistory> histories) {
        this.students = students;
        this.historyByStudent = histories.stream()
                .collect(Collectors.groupingBy(h -> h.getStudent().getId()));
    }

    public int eligibleStudentsCount(Course course) {
        return (int) students.stream()
                .filter(s -> withinGradeRange(s, course))
                .filter(s -> hasPrerequisitePassed(s, course))
                .filter(s -> notAlreadyPassed(s, course))
                .count();
    }

    private boolean withinGradeRange(Student s, Course c) {
        return s.getGradeLevel() >= c.getGradeLevelMin()
                && s.getGradeLevel() <= c.getGradeLevelMax();
    }

    private boolean hasPrerequisitePassed(Student s, Course c) {
        if (c.getPrerequisite() == null) return true;
        return historyByStudent.getOrDefault(s.getId(), List.of()).stream()
                .anyMatch(h -> Objects.equals(h.getCourse().getId(), c.getPrerequisite().getId())
                        && "passed".equalsIgnoreCase(h.getStatus()));
    }

    private boolean notAlreadyPassed(Student s, Course c) {
        return historyByStudent.getOrDefault(s.getId(), List.of()).stream()
                .noneMatch(h -> Objects.equals(h.getCourse().getId(), c.getId())
                        && "passed".equalsIgnoreCase(h.getStatus()));
    }
}
