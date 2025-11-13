import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WeeklyCalendarComponent, ScheduleEvent } from '../../../shared/components/weekly-calendar/weekly-calendar.component';
import { SemesterService } from '../../../core/services/semester.service';
import { ScheduleService } from '../../../core/services/schedule.service';
import { UserContextService } from '../../../core/services/user-context.service';

@Component({
  selector: 'app-my-schedule',
  standalone: true,
  imports: [CommonModule, WeeklyCalendarComponent],
  template: `
    <div class="page">
      <h2>My Schedule</h2>
      <app-weekly-calendar
        [events]="schedule"
        [semesterStart]="semesterStart"
        [semesterEnd]="semesterEnd"
        [isStudentView]="true">
      </app-weekly-calendar>
    </div>
  `,
  styles: [`.page { padding: 20px; }`]
})
export class MyScheduleComponent implements OnInit {
  schedule: ScheduleEvent[] = [];
  semesterStart?: string;
  semesterEnd?: string;
  private base = 'http://localhost:8080/api';

  constructor(
    private semesterService: SemesterService,
    private scheduleService: ScheduleService,
    private userCtx: UserContextService
  ) {}

  ngOnInit() {
    const user = this.userCtx.getUser();
    if (!user) return;

    // ✅ Fetch semester and schedule concurrently
    this.semesterService.getActiveSemester().subscribe(semester => {
      this.semesterStart = semester.startDate;
      this.semesterEnd = semester.endDate;

      this.scheduleService.getStudentSchedule(user.id).subscribe(sections => {
        this.schedule = sections;
      });
    });
  }
}
