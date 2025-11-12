package com.highschool.scheduler.repository;

import com.highschool.scheduler.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}

