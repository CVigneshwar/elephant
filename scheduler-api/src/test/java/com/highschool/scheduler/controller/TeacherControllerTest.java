package com.highschool.scheduler.controller;

import com.highschool.scheduler.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TeacherService teacherService;

    @Test
    void testGetAllTeachers() throws Exception {
        when(teacherService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());
    }
}
