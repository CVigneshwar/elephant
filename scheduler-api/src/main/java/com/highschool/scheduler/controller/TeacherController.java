package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Teacher;
import com.highschool.scheduler.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public List<Teacher> getAllTeachers() {
        log.debug("Fetching all teachers");
        return teacherService.findAll();
    }
}
