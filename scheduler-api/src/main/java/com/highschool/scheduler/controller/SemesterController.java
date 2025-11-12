package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.service.SemesterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping("/active")
    public Semester getActiveSemester() {
        log.info("Fetching active semester");
        return semesterService.getActiveSemester();
    }
}
