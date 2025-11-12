package com.highschool.scheduler.service;

import com.highschool.scheduler.dto.UtilizationDTO;
import com.highschool.scheduler.model.Classroom;
import com.highschool.scheduler.model.Course;
import com.highschool.scheduler.model.CourseSection;
import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.model.Teacher;
import com.highschool.scheduler.repository.CourseSectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UtilizationServiceTest {

    private CourseSectionRepository sectionRepo;
    private UtilizationService service;

    @BeforeEach
    void setup() {
        sectionRepo = Mockito.mock(CourseSectionRepository.class);
        service = new UtilizationService(sectionRepo);
    }

    private CourseSection section(Long id, Long teacherId, Long roomId,
                                  DayOfWeek day, int start, int end) {

        Teacher t = new Teacher();
        t.setId(teacherId);
        t.setFirstName("T" + teacherId);
        t.setLastName("L" + teacherId);

        Classroom c = new Classroom();
        c.setId(roomId);
        c.setName("R" + roomId);

        Course course = new Course();
        course.setId(id);
        course.setName("Course" + id);

        Semester sem = new Semester();
        sem.setId(1L);

        CourseSection s = new CourseSection();
        s.setId(id);
        s.setTeacher(t);
        s.setClassroom(c);
        s.setCourse(course);
        s.setSemester(sem);
        s.setDayOfWeek(day);
        s.setStartTime(LocalTime.of(start, 0));
        s.setEndTime(LocalTime.of(end, 0));

        return s;
    }

    @Test
    void testUtilizationCalculation() {
        // Arrange
        List<CourseSection> sections = List.of(
                section(1L, 10L, 100L, DayOfWeek.MONDAY, 9, 11), // 2 hours
                section(2L, 10L, 100L, DayOfWeek.TUESDAY, 10, 11), // 1 hour
                section(3L, 20L, 200L, DayOfWeek.MONDAY, 13, 15) // 2 hours
        );

        when(sectionRepo.findAll()).thenReturn(sections);

        // Act
        UtilizationDTO result = service.calculate();

        // Assert
        assertThat(result.teacherUsage().size()).isEqualTo(2);
        assertThat(result.roomUsage().size()).isEqualTo(2);
        assertThat(result.dayUsage().size()).isEqualTo(2);
        assertThat(result.timeSlotUsage().size()).isEqualTo(3);

        // Teacher 10L: 3 hours
        var teacher10 = result.teacherUsage().stream()
                .filter(u -> u.id() == 10L)
                .findFirst()
                .orElseThrow();

        assertThat(teacher10.used()).isEqualTo(3);

        // Monday total hours = 4
        var monday = result.dayUsage().stream()
                .filter(d -> d.day() == DayOfWeek.MONDAY)
                .findFirst()
                .orElseThrow();

        assertThat(monday.used()).isEqualTo(4);
    }

    @Test
    void testEmptyResponse() {
        when(sectionRepo.findAll()).thenReturn(List.of());

        UtilizationDTO result = service.calculate();

        assertThat(result.teacherUsage()).isEmpty();
        assertThat(result.roomUsage()).isEmpty();
        assertThat(result.summary().avgTeacherUtil()).isZero();
    }
}

