package com.highschool.scheduler.repository;

import com.highschool.scheduler.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findBySemesterOrder(int semesterOrder);

}

