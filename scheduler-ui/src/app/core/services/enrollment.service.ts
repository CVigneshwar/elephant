import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private base = '/api/students';

  constructor(private http: HttpClient) {}

  /** ✅ Fetch eligible dates for a specific course section */
  getEligibleDates(sectionId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/course-sections/${sectionId}/eligible-dates`);
  }

  /** ✅ Enroll student in a course section on a chosen date */
  enroll(studentId: number, sectionId: number, enrolledDate: string): Observable<any> {
    return this.http.post(`${this.base}/${studentId}/enroll`, { studentId, sectionId, enrolledDate });
  }
}
