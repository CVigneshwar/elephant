package com.highschool.scheduler.controller;

import com.highschool.scheduler.dto.ScheduleEventDTO;
import com.highschool.scheduler.service.ScheduleGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private static final String RESET_SCHEDULE_SUCCESS_MESSAGE =
            "All course sections and related enrollments deleted successfully.";

    private final ScheduleGeneratorService scheduleGeneratorService;

    @PostMapping("/generate")
    public List<ScheduleEventDTO> generateSchedule() {
        log.info("Generating master schedule for active semester...");
        List<ScheduleEventDTO> result = scheduleGeneratorService.generateForActiveSemester();
        log.info("Generated {} schedule events.", result.size());
        return result;
    }

    @GetMapping
    public List<ScheduleEventDTO> getSchedule() {
        log.debug("Fetching full master schedule...");
        return scheduleGeneratorService.getSchedule();
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetSchedule() {
        log.warn("Resetting master schedule (active semester only)...");
        scheduleGeneratorService.resetSchedule();
        log.info("Master schedule reset completed.");
        return ResponseEntity.ok(RESET_SCHEDULE_SUCCESS_MESSAGE);
    }
}
