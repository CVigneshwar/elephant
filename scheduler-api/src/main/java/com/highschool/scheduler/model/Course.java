package com.highschool.scheduler.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "credits")
    private int credits;

    @Column(name = "hours_per_week")
    private int hoursPerWeek;

    // Prerequisite (self-join)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prerequisite_id")
    private Course prerequisite;

    // Linked specialization
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @Column(name = "semester_order")
    private int semesterOrder; // 1 = Fall, 2 = Spring

    @Column(name = "grade_level_min")
    private int gradeLevelMin;

    @Column(name = "grade_level_max")
    private int gradeLevelMax;

    @Column(name = "course_type")
    private String courseType;

}
