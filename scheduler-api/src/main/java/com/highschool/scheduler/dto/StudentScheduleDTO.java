package com.highschool.scheduler.dto;


import java.util.List;

public record StudentScheduleDTO(
        Long semesterId,
        List<SectionDTO> sections
) {
}
