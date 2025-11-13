import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ScheduleEvent } from '../../shared/components/weekly-calendar/weekly-calendar.component';

export interface CourseSection {
  id: number;
  course: { id: number; code: string; name: string };
  teacher: { id: number; firstName: string; lastName: string };
  classroom: { id: number; name: string };
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  semester: { id: number; name: string; year: number };
  courseType: string;
}

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private courseScheduleBase = '/api/schedule';
  private enrollmentScheduleBase = '/api/students';

  constructor(private http: HttpClient) {}

  list(): Observable<ScheduleEvent[]> {
    return this.http.get<ScheduleEvent[]>(this.courseScheduleBase);
  }

  generate(): Observable<CourseSection[]> {
    return this.http.post<CourseSection[]>(`${this.courseScheduleBase}/generate`, {});
  }

  getStudentSchedule(studentId: number): Observable<ScheduleEvent[]> {
    return this.http.get<ScheduleEvent[]>(`${this.enrollmentScheduleBase}/${studentId}/schedule`);
  }

 reset(): Observable<{ message: string }> {
  return this.http.delete<{ message: string }>(`${this.courseScheduleBase}/reset`);
}
}
