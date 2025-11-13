import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { forkJoin } from 'rxjs';

import { UserContextService } from '../../../core/services/user-context.service';
import { EnrollmentService } from '../../../core/services/enrollment.service';

@Component({
  selector: 'app-my-progress',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatProgressBarModule],
  templateUrl: './my-progress.component.html',
  styleUrls: ['./my-progress.component.scss']
})
export class MyProgressComponent implements OnInit {
  progress: any;
  history: any[] = [];
  enrollments: any[] = [];
  studentId!: number;

  displayedColumnsHistory = ['semester', 'course', 'type', 'credits', 'status'];
  displayedColumnsCurrent = ['day', 'time', 'course', 'teacher', 'room', 'date'];

  constructor(
    private userCtx: UserContextService,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit(): void {
    const user = this.userCtx.getUser();
    if (!user) return;
    this.studentId = user.id;

    forkJoin({
      progress: this.enrollmentService.getProgress(this.studentId),
      history: this.enrollmentService.getAcademicHistory(this.studentId),
      enrollments: this.enrollmentService.getCurrentEnrollments(this.studentId)
    }).subscribe(res => {
      this.progress = res.progress;
      this.history = res.history;
      this.enrollments = res.enrollments;
    });
  }

  getStatusClass(status: string) {
    return status === 'passed' ? 'passed' : 'failed';
  }
}
