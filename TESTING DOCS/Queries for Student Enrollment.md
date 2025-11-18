# Student Enrollment Verification Queries


---

## 1. Courses for which a student id will be eligible for


```sql
SELECT c.*
FROM courses c
-- Only courses for the active semester
JOIN semesters sem ON sem.order_in_year = c.semester_order AND sem.is_active = 1
-- Get the student
JOIN students s ON s.id = 201
WHERE
  -- 1. Student's grade is in course's allowed range
  s.grade_level BETWEEN c.grade_level_min AND c.grade_level_max

  -- 2. Not already passed this course
  AND NOT EXISTS (
    SELECT 1
    FROM student_course_history sch
    WHERE sch.student_id = s.id
      AND sch.course_id = c.id
      AND LOWER(sch.status) = 'passed'
  )

  -- 3. Not already enrolled in this course for the active semester
  AND NOT EXISTS (
    SELECT 1
    FROM student_section_enrollments se
    JOIN course_sections cs ON se.course_section_id = cs.id
    WHERE se.student_id = s.id
      AND se.semester_id = sem.id
      AND cs.course_id = c.id
  )

  -- 4. Prerequisite check: if course has a prerequisite, student must have passed it
  AND (
    c.prerequisite_id IS NULL
    OR EXISTS (
      SELECT 1
      FROM student_course_history sch2
      WHERE sch2.student_id = s.id
        AND sch2.course_id = c.prerequisite_id
        AND LOWER(sch2.status) = 'passed'
    )
  )
ORDER BY c.name;

```

## 2. Verify more than 10 student enrollments is not possible


Insert these in DB and then try to enroll. The  date which already has 10 enrollment will not appear for the student 
After inserting, try enrolling michael.ramirez11@student.maplewood.edu to Team Sports on Monday 10-11

```sql
INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (201, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (202, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (203, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (204, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (205, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (206, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (207, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (208, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (209, 2685, 7, '2024-08-26');

INSERT INTO student_section_enrollments (student_id, course_section_id, semester_id, enrolled_date)
VALUES (210, 2685, 7, '2024-08-26');

```