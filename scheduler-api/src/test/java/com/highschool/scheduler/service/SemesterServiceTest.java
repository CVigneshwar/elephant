package com.highschool.scheduler.service;

import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

class SemesterServiceTest {

    private SemesterRepository semesterRepo;
    private SemesterService semesterService;

    @BeforeEach
    void setup() {
        semesterRepo = Mockito.mock(SemesterRepository.class);
        semesterService = new SemesterService(semesterRepo);
    }

    @Test
    void testGetActiveSemester() {
        // Arrange
        Semester sem = new Semester();
        sem.setId(10L);
        sem.setName("Fall");
        sem.setYear(2024);

        when(semesterRepo.findByIsActiveTrue()).thenReturn(Optional.of(sem));

        // Act
        Semester result = semesterService.getActiveSemester();

        // Assert
        assertThat(result.getName()).isEqualTo("Fall");
        assertThat(result.getYear()).isEqualTo(2024);
    }

    @Test
    void testNoActiveSemesterThrowsException() {
        when(semesterRepo.findByIsActiveTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> semesterService.getActiveSemester())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active semester found");
    }
}
