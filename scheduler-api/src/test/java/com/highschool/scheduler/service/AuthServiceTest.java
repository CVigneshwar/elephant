package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Student;
import com.highschool.scheduler.model.Teacher;
import com.highschool.scheduler.repository.StudentRepository;
import com.highschool.scheduler.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private StudentRepository studentRepo;
    private TeacherRepository teacherRepo;
    private AuthService authService;

    @BeforeEach
    void setup() {
        studentRepo = Mockito.mock(StudentRepository.class);
        teacherRepo = Mockito.mock(TeacherRepository.class);
        authService = new AuthService(studentRepo, teacherRepo);
    }

    private Student student(String email) {
        Student s = new Student();
        s.setId(10L);
        s.setFirstName("Karen");
        s.setLastName("Allen");
        s.setEmail(email);
        return s;
    }

    private Teacher teacher(String email) {
        Teacher t = new Teacher();
        t.setId(55L);
        t.setFirstName("Paul");
        t.setLastName("Phillips");
        t.setEmail(email);
        return t;
    }

    @Test
    void testLoginAsStudent() {
        when(studentRepo.findByEmailIgnoreCase("student@test.com")).thenReturn(Optional.of(student("student@test.com")));

        Map<String, Object> result = authService.loginUser("student@test.com");

        assertThat(result.get("role")).isEqualTo("STUDENT");
        assertThat(result.get("name")).isEqualTo("Karen Allen");
    }

    @Test
    void testLoginAsTeacher() {
        when(teacherRepo.findByEmailIgnoreCase("teacher@test.com")).thenReturn(Optional.of(teacher("teacher@test.com")));

        Map<String, Object> result = authService.loginUser("teacher@test.com");

        assertThat(result.get("role")).isEqualTo("TEACHER");
        assertThat(result.get("name")).isEqualTo("Paul Phillips");
    }

    @Test
    void testUnknownUserThrowsException() {
        when(studentRepo.findByEmailIgnoreCase("unknown@test.com")).thenReturn(Optional.empty());
        when(teacherRepo.findByEmailIgnoreCase("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginUser("unknown@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No user found");
    }
}
