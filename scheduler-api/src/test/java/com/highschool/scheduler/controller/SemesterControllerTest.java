package com.highschool.scheduler.controller;

import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.service.SemesterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SemesterController.class)
class SemesterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SemesterService semesterService;

    @Test
    void testGetActiveSemester() throws Exception {
        Semester sem = new Semester();
        sem.setId(1L);
        sem.setName("Fall");

        when(semesterService.getActiveSemester()).thenReturn(sem);

        mockMvc.perform(get("/api/semesters/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fall"));
    }
}
