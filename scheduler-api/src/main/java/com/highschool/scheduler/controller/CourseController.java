package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<Course> getAllCourses() {
        log.debug("Fetching all courses");
        return courseService.findAll();
    }
}
