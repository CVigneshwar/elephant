package com.highschool.scheduler.repository;

import com.highschool.scheduler.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    @Modifying
    @Query("""
                DELETE FROM CourseSection cs
                WHERE cs.semester.id = (
                    SELECT s.id FROM Semester s WHERE s.isActive = true
                )
            """)
    void deleteAllByActiveSemester();

}

