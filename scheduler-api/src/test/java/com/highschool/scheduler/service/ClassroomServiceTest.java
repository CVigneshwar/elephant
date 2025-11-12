package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.repository.ClassroomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ClassroomServiceTest {

    private ClassroomRepository classroomRepo;
    private ClassroomService classroomService;

    @BeforeEach
    void setup() {
        classroomRepo = Mockito.mock(ClassroomRepository.class);
        classroomService = new ClassroomService(classroomRepo);
    }

    @Test
    void testGetAllClassrooms() {
        // Arrange
        Classroom c1 = new Classroom();
        c1.setId(1L);
        c1.setName("Room-101");

        Classroom c2 = new Classroom();
        c2.setId(2L);
        c2.setName("Lab-1");

        when(classroomRepo.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<Classroom> result = classroomService.getAllClassrooms();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Room-101");
        assertThat(result.get(1).getName()).isEqualTo("Lab-1");
    }

    @Test
    void testEmptyClassroomList() {
        when(classroomRepo.findAll()).thenReturn(List.of());

        List<Classroom> result = classroomService.getAllClassrooms();

        assertThat(result).isEmpty();
    }
}
