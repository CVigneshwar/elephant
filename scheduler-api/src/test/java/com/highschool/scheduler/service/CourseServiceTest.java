package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CourseServiceTest {

    private CourseRepository courseRepo;
    private CourseService courseService;

    @BeforeEach
    void setup() {
        courseRepo = Mockito.mock(CourseRepository.class);
        courseService = new CourseService(courseRepo);
    }

    @Test
    void testFindAllCourses() {
        // Arrange
        Course c1 = new Course();
        c1.setId(1L);
        c1.setName("Math I");

        Course c2 = new Course();
        c2.setId(2L);
        c2.setName("Physics I");

        when(courseRepo.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<Course> result = courseService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Math I");
        assertThat(result.get(1).getName()).isEqualTo("Physics I");
    }

    @Test
    void testEmptyCourseList() {
        when(courseRepo.findAll()).thenReturn(List.of());

        List<Course> result = courseService.findAll();

        assertThat(result).isEmpty();
    }
}
