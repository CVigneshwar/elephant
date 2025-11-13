import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { forkJoin } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { UserContextService } from '../../../core/services/user-context.service';

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
  base = 'http://localhost:8080/api';
  studentId!: number;

  displayedColumnsHistory = ['semester', 'course', 'type', 'credits', 'status'];
  displayedColumnsCurrent = ['day', 'time', 'course', 'teacher', 'room', 'date'];

  constructor(private http: HttpClient, private userCtx: UserContextService) {}

  ngOnInit(): void {
    const user = this.userCtx.getUser();
    if (!user) return;
    this.studentId = user.id;

    forkJoin({
      progress: this.http.get(`${this.base}/students/${this.studentId}/progress`),
      history: this.http.get(`${this.base}/students/${this.studentId}/history`),
      enrollments: this.http.get(`${this.base}/students/${this.studentId}/enrollments/current`)
    }).subscribe(res => {
      this.progress = res.progress;
      this.history = res.history as any[];
      this.enrollments = res.enrollments as any[];
    });
  }

  getStatusClass(status: string) {
    return status === 'passed' ? 'passed' : 'failed';
  }
}
