package com.highschool.scheduler.controller;

import com.highschool.scheduler.dto.ValidationResponse;
import com.highschool.scheduler.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EnrollmentService enrollmentService;

    @Test
    void testGetSchedule() throws Exception {
        when(enrollmentService.getSchedule(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/students/1/schedule")).andExpect(status().isOk());
    }

    @Test
    void testValidateConflict() throws Exception {
        var response = new ValidationResponse(true, List.of());
        when(enrollmentService.validateConflict(eq(1L), eq(5L))).thenReturn(response);

        mockMvc.perform(post("/api/students/1/validate-conflict").contentType(MediaType.APPLICATION_JSON).content("{\"courseSectionId\":5}")).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void testValidatePrereq() throws Exception {
        var response = new ValidationResponse(true, List.of());
        when(enrollmentService.validatePrerequisite(eq(1L), eq(10L))).thenReturn(response);

        mockMvc.perform(post("/api/students/1/validate-prereq").contentType(MediaType.APPLICATION_JSON).content("{\"courseId\":10}")).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void testGetEligibleSections() throws Exception {
        when(enrollmentService.getEligibleSections(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/students/1/eligible-sections")).andExpect(status().isOk());
    }
}
