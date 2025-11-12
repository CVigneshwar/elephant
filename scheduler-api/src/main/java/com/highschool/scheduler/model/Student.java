package com.highschool.scheduler.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "students")
public class Student {

    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "grade_level")
    private int gradeLevel;


    @Column(name = "email")
    private String email;

    @Column(name = "enrollment_year")
    private int enrollmentYear;

    @Column(name = "expected_graduation_year")
    private int expectedGraduationYear;

}
