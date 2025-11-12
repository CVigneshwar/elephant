package com.highschool.scheduler.controller;

import com.highschool.scheduler.dto.ScheduleEventDTO;
import com.highschool.scheduler.service.ScheduleGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // IMPORTANT: @MockBean not @Mock
    @MockBean
    private ScheduleGeneratorService scheduleGeneratorService;

    @Test
    void testGenerateSchedule() throws Exception {

        ScheduleEventDTO dto = new ScheduleEventDTO(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "MAT101",
                "Algebra I",
                "John Smith",
                "Room-101",
                null
        );

        when(scheduleGeneratorService.generateForActiveSemester())
                .thenReturn(List.of(dto));

        mockMvc.perform(post("/api/schedule/generate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("MAT101"));
    }
}