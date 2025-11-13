import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { UserContextService } from '../../../core/services/user-context.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';
import { EnrollDialogComponent } from '../enroll-dialog/enroll-dialog.component';

@Component({
  selector: 'app-course-browser',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatSnackBarModule, MatDialogModule],
  templateUrl: './course-browser.component.html',
  styleUrls: ['./course-browser.component.scss']
})
export class CourseBrowserComponent implements OnInit {

  displayedColumns = ['dayOfWeek', 'time', 'course', 'teacher', 'room', 'action', 'courseType'];
  sections: any[] = [];
  studentId!: number;
  loading = true;

  constructor(
    private userCtx: UserContextService,
    private enrollmentService: EnrollmentService,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    const user = this.userCtx.getUser();
    if (!user) return;
    this.studentId = user.id;

    this.loadEligibleSections();
  }

  // -------------------------------
  // Fetch sections via service
  // -------------------------------
  loadEligibleSections() {
    this.loading = true;
    this.enrollmentService.getEligibleSections(this.studentId)
      .subscribe({
        next: (res) => {
          this.sections = res;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.snack.open('Failed to load sections', 'Close', { duration: 3000 });
        }
      });
  }

  // -------------------------------
  // Handle enrollment workflow
  // -------------------------------
  enroll(section: any) {
    const dialogRef = this.dialog.open(EnrollDialogComponent, {
      width: '400px',
      data: { sectionId: section.id, courseName: section.courseName }
    });

    dialogRef.afterClosed().subscribe((date: string | null) => {
      if (!date) return;

      // Step 1 — Validate conflict
      this.enrollmentService.validateConflict(this.studentId, section.id, date)
        .subscribe({
          next: (confRes) => {
            if (!confRes.ok) {
              this.snack.open(confRes.errors?.[0] || 'Schedule conflict', 'Close', { duration: 3000 });
              return;
            }

            // Step 2 — Validate prerequisite
            this.enrollmentService.validatePrerequisite(this.studentId, section.courseId)
              .subscribe({
                next: (preRes) => {
                  if (!preRes.ok) {
                    this.snack.open('Prerequisite not completed', 'Close', { duration: 3000 });
                    return;
                  }

                  // Step 3 — Enroll
                  this.enrollmentService.enroll(this.studentId, section.id, date)
                    .subscribe({
                      next: () => {
                        this.snack.open('Enrolled successfully!', 'Close', { duration: 2500 });
                        this.loadEligibleSections(); // refresh list
                      },
                      error: (err) => {
                        this.snack.open(err.error?.error || 'Enrollment failed', 'Close', { duration: 3000 });
                      }
                    });
                }
              });
          },
          error: () => {
            this.snack.open('Conflict validation error', 'Close', { duration: 3000 });
          }
        });
    });
  }
}
