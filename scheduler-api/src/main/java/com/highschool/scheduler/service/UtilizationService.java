package com.highschool.scheduler.service;

import com.highschool.scheduler.dto.UtilizationDTO;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.repository.CourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilizationService {

    private final CourseSectionRepository sectionRepo;

    private static final int TEACHER_MAX_DAILY = 4;
    private static final int ROOM_MAX_DAILY = 8;
    private static final int DAYS = 5;

    public UtilizationDTO calculate() {
        List<CourseSection> sections = sectionRepo.findAll();
        if (sections.isEmpty()) {
            return emptyResponse();
        }

        // ️Aggregate raw values
        Map<Long, Double> teacherHours = aggregateTeacherHours(sections);
        Map<Long, Double> roomHours = aggregateRoomHours(sections);
        Map<DayOfWeek, Double> dayHours = aggregateDayHours(sections);
        Map<LocalTime, Double> slotHours = aggregateSlotHours(sections);

        // Convert to unified DTO usage lists
        List<UtilizationDTO.ResourceUsage> teacherUsage = buildTeacherUsage(sections, teacherHours);
        List<UtilizationDTO.ResourceUsage> roomUsage = buildRoomUsage(sections, roomHours);
        List<UtilizationDTO.DayUsage> dayUsage = buildDayUsage(dayHours);
        List<UtilizationDTO.TimeSlotUsage> slotUsage = buildSlotUsage(slotHours);

        // summary
        UtilizationDTO.SummaryStats summary = buildSummary(teacherUsage, roomUsage, dayUsage, slotUsage);

        return new UtilizationDTO(summary, teacherUsage, roomUsage, dayUsage, slotUsage);
    }

    private Map<Long, Double> aggregateTeacherHours(List<CourseSection> sections) {
        Map<Long, Double> map = new HashMap<>();
        for (CourseSection s : sections) {
            map.merge(s.getTeacher().getId(), duration(s), Double::sum);
        }
        return map;
    }

    private Map<Long, Double> aggregateRoomHours(List<CourseSection> sections) {
        Map<Long, Double> map = new HashMap<>();
        for (CourseSection s : sections) {
            map.merge(s.getClassroom().getId(), duration(s), Double::sum);
        }
        return map;
    }

    private Map<DayOfWeek, Double> aggregateDayHours(List<CourseSection> sections) {
        Map<DayOfWeek, Double> map = new EnumMap<>(DayOfWeek.class);
        for (CourseSection s : sections) {
            map.merge(s.getDayOfWeek(), duration(s), Double::sum);
        }
        return map;
    }

    private Map<LocalTime, Double> aggregateSlotHours(List<CourseSection> sections) {
        Map<LocalTime, Double> map = new LinkedHashMap<>();
        for (CourseSection s : sections) {
            map.merge(s.getStartTime(), duration(s), Double::sum);
        }
        return map;
    }

    private double duration(CourseSection s) {
        return Duration.between(s.getStartTime(), s.getEndTime()).toHours();
    }

    private List<UtilizationDTO.ResourceUsage> buildTeacherUsage(
            List<CourseSection> sections, Map<Long, Double> hours) {

        double max = TEACHER_MAX_DAILY * DAYS;

        return hours.entrySet().stream()
                .map(e -> {
                    var teacher = sections.stream()
                            .filter(s -> s.getTeacher().getId().equals(e.getKey()))
                            .findFirst().get().getTeacher();
                    return new UtilizationDTO.ResourceUsage(
                            teacher.getId(),
                            teacher.getFirstName() + " " + teacher.getLastName(),
                            e.getValue(),
                            max,
                            percent(e.getValue(), max)
                    );
                })
                .sorted(Comparator.comparing(UtilizationDTO.ResourceUsage::percent).reversed())
                .collect(Collectors.toList());
    }

    private List<UtilizationDTO.ResourceUsage> buildRoomUsage(
            List<CourseSection> sections, Map<Long, Double> hours) {

        double max = ROOM_MAX_DAILY * DAYS;

        return hours.entrySet().stream()
                .map(e -> {
                    var room = sections.stream()
                            .filter(s -> s.getClassroom().getId().equals(e.getKey()))
                            .findFirst().get().getClassroom();
                    return new UtilizationDTO.ResourceUsage(
                            room.getId(),
                            room.getName(),
                            e.getValue(),
                            max,
                            percent(e.getValue(), max)
                    );
                })
                .sorted(Comparator.comparing(UtilizationDTO.ResourceUsage::percent).reversed())
                .toList();
    }

    private List<UtilizationDTO.DayUsage> buildDayUsage(Map<DayOfWeek, Double> hours) {
        double max = ROOM_MAX_DAILY * DAYS;
        return hours.entrySet().stream()
                .map(e -> new UtilizationDTO.DayUsage(e.getKey(), e.getValue(), percent(e.getValue(), max)))
                .sorted(Comparator.comparing(d -> d.day().getValue()))
                .toList();
    }

    private List<UtilizationDTO.TimeSlotUsage> buildSlotUsage(Map<LocalTime, Double> hours) {
        double max = ROOM_MAX_DAILY * DAYS;
        return hours.entrySet().stream()
                .map(e -> new UtilizationDTO.TimeSlotUsage(e.getKey(), e.getValue(), percent(e.getValue(), max)))
                .sorted(Comparator.comparing(UtilizationDTO.TimeSlotUsage::slot))
                .toList();
    }

    // ---------------------------------------------------------
    // 🔹 Summary builder
    // ---------------------------------------------------------

    private UtilizationDTO.SummaryStats buildSummary(
            List<UtilizationDTO.ResourceUsage> teacher,
            List<UtilizationDTO.ResourceUsage> rooms,
            List<UtilizationDTO.DayUsage> days,
            List<UtilizationDTO.TimeSlotUsage> slots) {

        return new UtilizationDTO.SummaryStats(
                avg(teacher), avg(rooms), avg(days), avg(slots),
                maxLabel(days), minLabel(days),
                maxLabel(slots), minLabel(slots)
        );
    }

    private double avg(List<? extends UtilizationDTO.Usage> list) {
        return list.stream().mapToDouble(UtilizationDTO.Usage::percent).average().orElse(0);
    }

    private String maxLabel(List<? extends UtilizationDTO.Usage> list) {
        return list.stream().max(Comparator.comparing(UtilizationDTO.Usage::percent))
                .map(UtilizationDTO.Usage::label).orElse("-");
    }

    private String minLabel(List<? extends UtilizationDTO.Usage> list) {
        return list.stream().min(Comparator.comparing(UtilizationDTO.Usage::percent))
                .map(UtilizationDTO.Usage::label).orElse("-");
    }

    private double percent(double used, double max) {
        return (used / max) * 100;
    }

    // ---------------------------------------------------------

    private UtilizationDTO emptyResponse() {
        return new UtilizationDTO(
                new UtilizationDTO.SummaryStats(0,0,0,0,"-","-","-","-"),
                List.of(), List.of(), List.of(), List.of()
        );
    }
}