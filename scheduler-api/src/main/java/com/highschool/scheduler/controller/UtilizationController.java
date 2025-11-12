package com.highschool.scheduler.controller;

import com.highschool.scheduler.dto.UtilizationDTO;
import com.highschool.scheduler.service.UtilizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/utilization")
public class UtilizationController {

    private final UtilizationService utilizationService;

    @GetMapping
    public UtilizationDTO getUtilization() {
        log.info("Calculating utilization statistics...");
        return utilizationService.calculate();
    }
}
