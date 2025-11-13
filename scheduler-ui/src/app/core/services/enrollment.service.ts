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

  // -------------------------------
  // Progress Overview
  // -------------------------------
  getProgress(studentId: number): Observable<any> {
    return this.http.get(`${this.base}/${studentId}/progress`);
  }

  // -------------------------------
  // Academic History
  // -------------------------------
  getAcademicHistory(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${studentId}/history`);
  }

  // -------------------------------
  // Current Semester Enrollments
  // -------------------------------
  getCurrentEnrollments(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${studentId}/enrollments/current`);
  }


  // -------------------------------
  // Fetch eligible course sections
  // -------------------------------
  getEligibleSections(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${studentId}/eligible-sections`);
  }

  // -------------------------------
  // Validate conflict
  // -------------------------------
  validateConflict(studentId: number, sectionId: number, enrolledDate: string): Observable<any> {
    return this.http.post(`${this.base}/${studentId}/validate-conflict`, { studentId, sectionId, enrolledDate });
  }

  // -------------------------------
  // Validate prerequisite
  // -------------------------------
  validatePrerequisite(studentId: number, courseId: number): Observable<any> {
    return this.http.post(`${this.base}/${studentId}/validate-prereq`, {
      courseId: courseId
    });
  }


}
