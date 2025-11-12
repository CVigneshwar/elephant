package com.highschool.scheduler.service;


import com.highschool.scheduler.model.Semester;
import com.highschool.scheduler.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public Semester getActiveSemester() {
        return semesterRepository.findByIsActiveTrue().orElseThrow(() -> new IllegalStateException("No active semester found"));
    }
}
