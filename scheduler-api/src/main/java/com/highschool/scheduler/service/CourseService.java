package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepo;

    public List<Course> findAll() {
        return this.courseRepo.findAll();
    }
}
