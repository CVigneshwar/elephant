package com.highschool.scheduler.controller;

import com.highschool.scheduler.service.ClassroomService;
import com.highschool.scheduler.model.Classroom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassroomController.class)
class ClassroomControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClassroomService classroomService;

    @Test
    void testGetAllClassrooms() throws Exception {
        when(classroomService.getAllClassrooms()).thenReturn(List.of());

        mockMvc.perform(get("/api/classrooms"))
                .andExpect(status().isOk());
    }
}
