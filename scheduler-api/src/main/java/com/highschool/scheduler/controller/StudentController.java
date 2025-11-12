package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Student;
import com.highschool.scheduler.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<Student> getAllStudents() {
        log.debug("Fetching all students...");
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        log.debug("Fetching student {}", id);
        return studentService.findById(id);
    }
}
