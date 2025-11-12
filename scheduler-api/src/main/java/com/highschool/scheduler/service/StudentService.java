package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Student;
import com.highschool.scheduler.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> findAll() {
        return studentRepository.findAll();
    }


    public Student findById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new IllegalStateException("No student found"));
    }
}
