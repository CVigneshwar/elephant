package com.highschool.scheduler.repository;

import com.highschool.scheduler.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmailIgnoreCase(String email);
}
