import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ScheduleEvent, WeeklyCalendarComponent } from '../../../shared/components/weekly-calendar/weekly-calendar.component';
import { ScheduleService } from '../../../core/services/schedule.service';
import { finalize } from 'rxjs/operators';
import { SemesterService } from '../../../core/services/semester.service';

@Component({
  selector: 'app-admin-schedule',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatDialogModule,
    WeeklyCalendarComponent
  ],
  templateUrl: './admin-schedule.component.html',
  styleUrls: ['./admin-schedule.component.scss']
})
export class AdminScheduleComponent {
  data: ScheduleEvent[] = [];
  loading = false;
  semesterStart?: string;
  semesterEnd?: string;

  constructor(
    private schedule: ScheduleService,
    private semesterService: SemesterService,
    private snack: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.semesterService.getActiveSemester().subscribe(semester => {
      this.semesterStart = semester.startDate;
      this.semesterEnd = semester.endDate;
      this.refresh();
    });
  }

  generate() {
    this.loading = true;
    this.schedule.generate()
      .pipe(finalize(() => this.refresh()))
      .subscribe({
        next: () => this.snack.open('✅ Schedule generated successfully', 'Close', { duration: 2500 }),
        error: () => this.snack.open('⚠️ Error generating schedule', 'Close', { duration: 2500 })
      });
  }

  refresh() {
    this.loading = true;
    this.schedule.list()
      .pipe(finalize(() => this.loading = false))
      .subscribe(res => this.data = res);
  }

  /**  Reset all generated sections & enrollments */
  resetAll() {
    const confirmed = confirm('⚠️ This will delete all course sections and student enrollments. Continue?');
    if (!confirmed) return;

    this.loading = true;
    this.schedule.reset()
      .pipe(finalize(() => this.refresh()))
      .subscribe({
        next: (res) => {this.snack.open(res.message, 'Close', { duration: 2500 });},
        error: () => this.snack.open('⚠️ Failed to reset schedule', 'Close', { duration: 2500 })
      });
  }
}
