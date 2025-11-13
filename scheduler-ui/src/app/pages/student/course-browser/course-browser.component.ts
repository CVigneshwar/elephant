import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { UserContextService } from '../../../core/services/user-context.service';
import { CourseSection, ScheduleService } from '../../../core/services/schedule.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { EnrollDialogComponent } from '../enroll-dialogue/enroll-dialogue.component';


@Component({
  selector: 'app-course-browser',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatSnackBarModule, MatDialogModule],
  templateUrl: './course-browser.component.html',
  styleUrls: ['./course-browser.component.scss']
})
export class CourseBrowserComponent implements OnInit {
    displayedColumns = ['dayOfWeek', 'time', 'course', 'courseType', 'teacher', 'room', 'action'];
    sections: CourseSection[] = [];
  loading = true;
  studentId!: number;
  base = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private snack: MatSnackBar,
    private userCtx: UserContextService,
    private scheduleService: ScheduleService,
    private enrollmentService: EnrollmentService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    const user = this.userCtx.getUser();
    if (!user) return;
    this.studentId = user.id;

    this.http.get(`${this.base}/students/${this.studentId}/eligible-sections`)
      .subscribe((res: any) => {
        this.sections = res;
        this.loading = false;
      });
  }

  /** 🧠 Opens date picker dialog and performs enrollment */
  enroll(section: any) {
    const dialogRef = this.dialog.open(EnrollDialogComponent, {
      width: '400px',
      data: { sectionId: section.id, courseName: section.courseName }
    });
  
    dialogRef.afterClosed().subscribe((selectedDate: string | null) => {
      if (!selectedDate) {
        return; // user canceled
      }
  
      // ✅ Validate conflict
      this.http
        .post(`${this.base}/students/${this.studentId}/validate-conflict`, { courseSectionId: section.id })
        .subscribe({
          next: (valRes: any) => {
            if (!valRes.ok) {
              this.snack.open(valRes.errors?.[0] || 'Schedule conflict detected', 'Close', { duration: 3000 });
              return;
            }
  
            // ✅ Validate prerequisite
            this.http
              .post(`${this.base}/students/${this.studentId}/validate-prereq`, { courseId: section.courseId })
              .subscribe({
                next: (preRes: any) => {
                  if (!preRes.ok) {
                    this.snack.open('Prerequisite not completed', 'Close', { duration: 3000 });
                    return;
                  }
  
                  // ✅ Enroll student
                  this.enrollmentService.enroll(this.studentId, section.id, selectedDate).subscribe({
                    next: () => {
                      this.snack.open('Enrolled successfully!', 'Close', { duration: 2500 });
                      this.refreshSections();
                    },
                    error: (err) => {
                        const msg =
                          err?.error?.error ||       // Spring Boot `error` field
                          err?.error?.message ||     // Custom message from backend
                          err?.message ||            // HTTP message
                          'Enrollment failed';
                        this.snack.open(msg, 'Close', { duration: 3000 });
                      }
                  });
                },
                error: () => {
                  this.snack.open('Error validating prerequisite', 'Close', { duration: 3000 });
                }
              });
          },
          error: () => {
            this.snack.open('Error validating conflict', 'Close', { duration: 3000 });
          }
        });
    });
  }

  refreshSections() {
    this.http.get(`${this.base}/students/${this.studentId}/eligible-sections`)
      .subscribe((res: any) => (this.sections = res));
  }
}
