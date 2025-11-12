1. Introduction

This project solves both challenges from the assignment:

Challenge 1: Master Schedule Generator

A backend engine that automatically creates the school’s weekly timetable while respecting teacher constraints, room constraints, and time restrictions.

Challenge 2: Student Course Planning

A student portal that allows students to view available sections, select eligible course sections, avoid conflicts, track academic progress, and view their personal weekly schedule.

The system is built as a full-stack web application using Spring Boot (Java) + Angular with a preloaded SQLite database.

2. High-Level Architecture
Backend (Spring Boot)

Layered architecture: Controllers → Services → Repositories → DTO Models

JPA + SQLite (lightweight in-memory file database)

Custom constraints + DB triggers

Services implement scheduling, conflict detection, prerequisite validation, and progress tracking

REST API exposed for frontend consumption

Frontend (Angular, Standalone Components)

Student portal and teacher portal

Weekly calendar visualization

Browse course sections

Enrollment popup & conflict validation

Progress dashboard

Utilization dashboard (bonus)

Dev Tools

Dockerfile + DevContainer scaffolding prepared

Useful for reviewers to run in a consistent environment

(Local verification not possible due to missing virtualization support)

3. Data Model & Enhancements
Core Provided Tables:

students, teachers, classrooms

courses, course history

specializations, room types

semesters

Tables Added:

student_section_enrollments

student_id

course_section_id

semester_id

enrolled_date

(FK constraints enabled)

Database-level Triggers Provided:

prerequisite enforcement

prevent duplicate “passed” course

prerequisite semester ordering

Enhancements Added:

course_type (CORE / ELECTIVE)

enrolled_date support for accurate scheduling by date

capacity checks based on enrollment date

active semester–based queries

4. Challenge 1: Master Schedule Generator
4.1 Constraints Implemented
Constraint	Handled?	How
School hours (9–17)	✅	slot list definition
Lunch break	✅	12:00 excluded
Max 2-hour consecutive session	✅	duration capping
Min 1 hour	✅	default session length
Teacher daily limit (4 hrs)	✅	daily load tracking
Teacher specialization	✅	teacher filtered by specialization
Room specialization	✅	room filtered by roomType
Room capacity (10)	✅	course types + capacity logic
No double-booking	✅	teacher + room busy map
Demand satisfaction	👍 Balanced	hours-per-week + eligible students
Even load distribution	👍 Improved	day load + time slot load maps
4.2 Scheduling Strategy (Concise)

Find eligible students for each course

Estimate number of sections needed

Pick teacher + room by specialization

Distribute weekly hours across days

For each hour:

choose least-loaded day

choose least-loaded time slot

validate teacher daily limit

avoid >2-hour consecutive

place section

mark teacher + room busy

update day/time load maps

This creates a weekly schedule that is globally balanced across Mon–Fri and all time slots.

5. Challenge 2: Student Course Planning
5.1 Eligible Course Section Logic

A student sees only sections where:

Eligibility Rule	Implemented?
Grade range matches	✅
Prerequisite passed	✅
Has not already passed this course	✅
Course not already enrolled this semester	✅
Seats available on selected date	✅
No conflict with (day + time + date)	✅
5.2 Enrollment Flow

Student opens "Browse Courses"

Clicks Enroll → popup opens

System loads eligible dates based on capacity

Student chooses a date

Backend validates:

conflict check using exact date

prerequisite

max 5 courses per semester

room capacity by date

Enrollment saved with enrolled_date

5.3 Student Schedule View

The weekly calendar shows only those course sections scheduled on the exact enrolled date, not repeated every week.

5.4 Progress Tracking

GPA (pass = 4 points, fail = 0)

Credits earned

Credits remaining

Core vs elective courses

Upcoming planned courses

Max course limit detection

6. UI/UX Notes
Teacher Portal

Generate Schedule

View Weekly Timetable

View Utilization Dashboard

Student Portal

Browse Courses

Enroll (popup date-selector)

My Schedule (date-based weekly calendar)

Progress Dashboard

Academic History

Weekly Calendar

Flexible width

Horizontal + vertical stacking

Shows capacity text inside event box

Navigation based on semester date range

7. Key System Assumptions (Documented)

Weekly schedule is repeated across semester.

Course hours_per_week represent total weekly instructional hours.

Sessions are either 1 hour or 2 hours max.

Capacity is per session per date.

Students enroll for specific dates—not the entire semester block.

Teachers cannot exceed 4 hours daily or 2 consecutive hours.

Only the active semester is considered.

8. Limitations & Future Improvements (Optional but Useful)

Could use genetic algorithms or integer programming for complex optimization

UI calendar can support drag-and-drop

Add teacher availability preferences

Add automated conflict resolution suggestions for students

Multi-threaded schedule generator for large schools

9. DevContainer & Docker Summary (Concise)

DevContainer gives reproducible environment:

Java 17 + Maven

Node 18 + Angular

SQLite preinstalled

Same environment for reviewers

Dockerfile supports running backend & frontend containers.
(Not tested locally because virtualization support is disabled.)

10. How to Run
Backend
cd scheduler
mvn spring-boot:run

Frontend
cd scheduler-ui
npm install
ng serve --open

Database

Already included 
Spring Boot loads it automatically.

11. Closing Summary

This project delivers:

✔ Fully automated master schedule generator
✔ Balanced resource allocation (teachers, rooms, timeslots)
✔ Student course planning with conflict, capacity & prerequisite validation
✔ Weekly calendar visualizations for teacher and student
✔ Academic progress tracking
✔ Extra features: utilization dashboards, DevContainer, Dockerfile

The solution is modular, maintainable, and ready for extension during the technical interview.