package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping
    public List<Classroom> getAllClassrooms() {
        log.debug("Fetching all classrooms");
        return classroomService.getAllClassrooms();
    }
}
