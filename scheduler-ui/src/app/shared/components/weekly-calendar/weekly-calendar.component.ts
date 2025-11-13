import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { addDays, startOfWeek, addWeeks, format, parseISO, isBefore } from 'date-fns';

export interface ScheduleEvent {
  id: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  courseCode?: string;
  courseName?: string;
  teacherName?: string;
  roomName?: string;
  enrolledDate?: string;
}

@Component({
  selector: 'app-weekly-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './weekly-calendar.component.html',
  styleUrls: ['./weekly-calendar.component.scss']
})
export class WeeklyCalendarComponent implements OnInit, OnChanges {
  @Input() events: ScheduleEvent[] = [];
  @Input() semesterStart?: string;
  @Input() semesterEnd?: string;
  @Input() isStudentView: boolean = false;

  currentWeekStart!: Date;
  days: Date[] = [];
  times = ['09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00'];

  ngOnInit() {
    // default fallback
    this.currentWeekStart = startOfWeek(new Date(), { weekStartsOn: 1 });
    this.setWeekDays();
    this.initializeCalendar();

  }


  ngOnChanges(changes: SimpleChanges) {
    if (changes['semesterStart'] && this.semesterStart) {
      this.initializeCalendar();
    }
  }

  private initializeCalendar() {
    if (this.semesterStart) {
      this.currentWeekStart = startOfWeek(parseISO(this.semesterStart), { weekStartsOn: 1 });
    } else {
      this.currentWeekStart = startOfWeek(new Date(), { weekStartsOn: 1 });
    }
    this.setWeekDays();
  }

  formatDate(date: Date, fmt: string): string {
    return format(date, fmt);
  }

  setWeekDays() {
    this.days = Array.from({ length: 5 }, (_, i) => addDays(this.currentWeekStart, i));
  }

  nextWeek() {
    const nextWeekStart = startOfWeek(addWeeks(this.currentWeekStart, 1), { weekStartsOn: 1 });
  
    if (this.semesterEnd) {
      const semesterEndWeek = startOfWeek(parseISO(this.semesterEnd), { weekStartsOn: 1 });
      if (isBefore(semesterEndWeek, nextWeekStart)) return;
    }
  
    this.currentWeekStart = nextWeekStart;
    this.setWeekDays();
  }
  
  prevWeek() {
    const prevWeekStart = startOfWeek(addWeeks(this.currentWeekStart, -1), { weekStartsOn: 1 });
  
    if (this.semesterStart) {
      const semesterStartWeek = startOfWeek(parseISO(this.semesterStart), { weekStartsOn: 1 });
  
      if (isBefore(prevWeekStart, semesterStartWeek)) return;
    }
  
    this.currentWeekStart = prevWeekStart;
    this.setWeekDays();
  }

  today() {
    this.currentWeekStart = this.semesterStart
      ? startOfWeek(parseISO(this.semesterStart), { weekStartsOn: 1 })
      : startOfWeek(new Date(), { weekStartsOn: 1 });
    this.setWeekDays();
  }

  formatDateRange(): string {
    if (!this.days.length) return '';
    const startLabel = format(this.days[0], 'MMM d, yyyy');
    const endLabel = format(this.days[this.days.length - 1], 'MMM d, yyyy');
    return `${startLabel} - ${endLabel}`;
  }

  getEventsFor(day: Date, time: string) {
    const slotHour = this.toHour(time);
    const dayString = this.formatDate(day, 'yyyy-MM-dd');
    const weekday = this.formatDate(day, 'EEEE').toUpperCase();
  
    return (this.events || []).filter(e => {
      // For student view: only show if enrolledDate matches this day
      if (this.isStudentView) {
        if (!e.enrolledDate) return false;
        return (
          e.enrolledDate === dayString &&
          slotHour >= this.toHour(e.startTime) &&
          slotHour < this.toHour(e.endTime)
        );
      }
  
      // For teacher/admin view: fall back to dayOfWeek logic
      return (
        e.dayOfWeek === weekday &&
        slotHour >= this.toHour(e.startTime) &&
        slotHour < this.toHour(e.endTime)
      );
    });
  }

  private toHour(s: string): number {
    const [h, m] = s.split(':').map(Number);
    return h + m / 60;
  }

  calculateDuration(start: string, end: string): number {
    const dur = this.toHour(end) - this.toHour(start);
    return Math.max(1, Math.round(dur));
  }
}
