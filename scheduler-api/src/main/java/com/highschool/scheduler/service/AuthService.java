package com.highschool.scheduler.service;

import com.highschool.scheduler.repository.StudentRepository;
import com.highschool.scheduler.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String NO_USER_FOUND_WITH_THAT_EMAIL = "No user found with the email - ";
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    public Map<String, Object> loginUser(String email) {

        return studentRepository.findByEmailIgnoreCase(email)
                .<Map<String, Object>>map(s -> Map.of(
                        "id", s.getId(),
                        "name", s.getFirstName() + " " + s.getLastName(),
                        "email", s.getEmail(),
                        "role", "STUDENT"
                ))
                .or(() -> teacherRepository.findByEmailIgnoreCase(email)
                        .map(t -> Map.of(
                                "id", t.getId(),
                                "name", t.getFirstName() + " " + t.getLastName(),
                                "email", t.getEmail(),
                                "role", "TEACHER"
                        )))
                .orElseThrow(() -> new IllegalArgumentException(NO_USER_FOUND_WITH_THAT_EMAIL + email));
    }
}
