# School Scheduling SQL Reporting Queries

These queries help analyze teacher load, room usage, slot distribution, daily load, and planned vs. assigned hours.

---

## 1. Teacher Load Allocation

Shows all 50 teachers, their section assignments for each specialization, and the mandatory specialization hours per week (for semester order 1).

```sql
SELECT
    s.id AS specialization_id,
    s.name AS specialization_name,
    t.id AS teacher_id,
    t.first_name || ' ' || t.last_name AS teacher_name,
    COUNT(cs.id) AS num_sections,
    COALESCE(SUM(
        (strftime('%H', cs.end_time) * 60 + strftime('%M', cs.end_time))
      - (strftime('%H', cs.start_time) * 60 + strftime('%M', cs.start_time))
    ) / 60.0, 0) AS teacher_total_hours,
    (
      SELECT SUM(hours_per_week)
      FROM courses c2
      WHERE c2.specialization_id = s.id AND c2.semester_order = 1
    ) AS mandatory_specialization_hours_per_week
FROM
    teachers t
JOIN
    specializations s ON t.specialization_id = s.id
LEFT JOIN course_sections cs ON cs.teacher_id = t.id
LEFT JOIN courses c ON cs.course_id = c.id AND c.semester_order = 1
GROUP BY
    s.id, s.name, t.id, t.first_name, t.last_name
ORDER BY
    s.name, t.last_name, t.first_name;
```

## 2. Room Load

Shows room utilization (for Mathematics room type), including rooms with zero allocations.

```sql
SELECT
    r.id AS room_id,
    r.name AS room_name,
    rt.name AS room_type_name,
    COUNT(cs.id) AS num_sections,
    COALESCE(SUM(
        (strftime('%H', cs.end_time) * 60 + strftime('%M', cs.end_time))
      - (strftime('%H', cs.start_time) * 60 + strftime('%M', cs.start_time))
    ) / 60.0, 0) AS total_hours
FROM
    classrooms r
JOIN
    room_types rt ON r.room_type_id = rt.id
LEFT JOIN
    course_sections cs ON cs.classroom_id = r.id
LEFT JOIN
    courses c ON cs.course_id = c.id
LEFT JOIN
    specializations s ON c.specialization_id = s.id
WHERE
    r.room_type_id = (
      SELECT room_type_id FROM specializations WHERE name = 'Mathematics'
    )
    AND (s.name = 'Mathematics' OR s.name IS NULL)
GROUP BY
    r.id, r.name, rt.name
ORDER BY
    r.name;
```

## 3. Time Slot Load
Shows the number of sections and total hours scheduled for each possible time slot, including empty slots.

```sql
WITH slots(day_of_week, start_time) AS (
  SELECT 'MONDAY',   '09:00' UNION ALL
  SELECT 'MONDAY',   '10:00' UNION ALL
  SELECT 'MONDAY',   '11:00' UNION ALL
  SELECT 'MONDAY',   '13:00' UNION ALL
  SELECT 'MONDAY',   '14:00' UNION ALL
  SELECT 'MONDAY',   '15:00' UNION ALL
  SELECT 'MONDAY',   '16:00' UNION ALL
  SELECT 'TUESDAY',  '09:00' UNION ALL
  SELECT 'TUESDAY',  '10:00' UNION ALL
  SELECT 'TUESDAY',  '11:00' UNION ALL
  SELECT 'TUESDAY',  '13:00' UNION ALL
  SELECT 'TUESDAY',  '14:00' UNION ALL
  SELECT 'TUESDAY',  '15:00' UNION ALL
  SELECT 'TUESDAY',  '16:00' UNION ALL
  SELECT 'WEDNESDAY','09:00' UNION ALL
  SELECT 'WEDNESDAY','10:00' UNION ALL
  SELECT 'WEDNESDAY','11:00' UNION ALL
  SELECT 'WEDNESDAY','13:00' UNION ALL
  SELECT 'WEDNESDAY','14:00' UNION ALL
  SELECT 'WEDNESDAY','15:00' UNION ALL
  SELECT 'WEDNESDAY','16:00' UNION ALL
  SELECT 'THURSDAY', '09:00' UNION ALL
  SELECT 'THURSDAY', '10:00' UNION ALL
  SELECT 'THURSDAY', '11:00' UNION ALL
  SELECT 'THURSDAY', '13:00' UNION ALL
  SELECT 'THURSDAY', '14:00' UNION ALL
  SELECT 'THURSDAY', '15:00' UNION ALL
  SELECT 'THURSDAY', '16:00' UNION ALL
  SELECT 'FRIDAY',   '09:00' UNION ALL
  SELECT 'FRIDAY',   '10:00' UNION ALL
  SELECT 'FRIDAY',   '11:00' UNION ALL
  SELECT 'FRIDAY',   '13:00' UNION ALL
  SELECT 'FRIDAY',   '14:00' UNION ALL
  SELECT 'FRIDAY',   '15:00' UNION ALL
  SELECT 'FRIDAY',   '16:00'
)
SELECT
  slots.day_of_week,
  slots.start_time,
  COUNT(cs.id) AS num_sections,
  COALESCE(SUM(
      (strftime('%H', cs.end_time) * 60 + strftime('%M', cs.end_time))
    - (strftime('%H', cs.start_time) * 60 + strftime('%M', cs.start_time))
  ) / 60.0, 0) AS total_hours
FROM
  slots
LEFT JOIN course_sections cs
  ON cs.day_of_week = slots.day_of_week
  AND cs.start_time = slots.start_time
GROUP BY
  slots.day_of_week, slots.start_time
ORDER BY
  CASE slots.day_of_week
    WHEN 'MONDAY' THEN 1
    WHEN 'TUESDAY' THEN 2
    WHEN 'WEDNESDAY' THEN 3
    WHEN 'THURSDAY' THEN 4
    WHEN 'FRIDAY' THEN 5
  END,
  slots.start_time;
```

## 4. Day of Week Load
Shows total sections and hours for each day (Monday to Friday).

```sql
SELECT
  day_of_week,
  COUNT(id) AS num_sections,
  COALESCE(SUM(
      (strftime('%H', end_time) * 60 + strftime('%M', end_time))
    - (strftime('%H', start_time) * 60 + strftime('%M', start_time))
  ) / 60.0, 0) AS total_hours
FROM
  course_sections
GROUP BY
  day_of_week
ORDER BY
  CASE day_of_week
    WHEN 'MONDAY' THEN 1
    WHEN 'TUESDAY' THEN 2
    WHEN 'WEDNESDAY' THEN 3
    WHEN 'THURSDAY' THEN 4
    WHEN 'FRIDAY' THEN 5
  END;
```

## 5. Planned Hours vs Assigned Hours
Compares planned hours per week (from the courses table) to the actual assigned hours (from course_sections) for all courses in semester_order 1.

```sql
SELECT
    c.id AS course_id,
    c.code AS course_code,
    c.name AS course_name,
    c.hours_per_week AS planned_hours_per_week,
    COALESCE(SUM(
        (strftime('%H', cs.end_time) * 60 + strftime('%M', cs.end_time))
      - (strftime('%H', cs.start_time) * 60 + strftime('%M', cs.start_time))
    ) / 60.0, 0) AS assigned_hours
FROM
    courses c
LEFT JOIN
    course_sections cs ON c.id = cs.course_id
WHERE
    c.semester_order = 1
GROUP BY
    c.id, c.code, c.name, c.hours_per_week
ORDER BY
    c.code;
```

