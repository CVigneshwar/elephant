// com.highschool.scheduler.repository.StudentSectionEnrollmentRepository
package com.highschool.scheduler.repository;

import com.highschool.scheduler.model.StudentSectionEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudentSectionEnrollmentRepository extends JpaRepository<StudentSectionEnrollment, Long> {
    int countByStudentIdAndSemesterId(Long studentId, Long semesterId);

    List<StudentSectionEnrollment> findByStudentIdAndSemesterId(Long studentId, Long semesterId);

    @Query(
            value = """
                      SELECT * 
                      FROM student_section_enrollments 
                      WHERE student_id = :studentId 
                        AND course_section_id = :sectionId 
                      LIMIT 1
                    """,
            nativeQuery = true)
    StudentSectionEnrollment findByStudentAndSection(@Param("studentId") Long studentId,
                                                     @Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(e) FROM StudentSectionEnrollment e " +
            "WHERE e.courseSection.id = :sectionId AND e.enrolledDate = :date")
    long countByCourseSectionIdAndEnrolledDate(@Param("sectionId") Long sectionId, @Param("date") LocalDate date);


    List<StudentSectionEnrollment> findByStudentId(Long studentId);


    @Modifying
    @Query("""
                DELETE FROM StudentSectionEnrollment e
                WHERE e.courseSection.semester.id = (
                    SELECT s.id FROM Semester s WHERE s.isActive = true
                )
            """)
    void deleteAllByActiveSemester();

    @Query("""
                SELECT COUNT(e)
                FROM StudentSectionEnrollment e
                WHERE e.courseSection.id = :sectionId
                  AND e.enrolledDate = :date
            """)
    long countBySectionAndDate(@Param("sectionId") Long sectionId,
                               @Param("date") LocalDate date);


}
