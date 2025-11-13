import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';

export interface Semester {
  id: number;
  name: string;
  year: number;
  startDate: string;
  endDate: string;
  orderInYear: number;
  isActive: boolean;
}

@Injectable({ providedIn: 'root' })
export class SemesterService {
  private base = '/api/semesters';
  private activeSemesterCache$?: Observable<Semester>;

  constructor(private http: HttpClient) {}

  /** ✅ Fetch active semester (cached for session performance) */
  getActiveSemester(): Observable<Semester> {
    if (!this.activeSemesterCache$) {
      this.activeSemesterCache$ = this.http
        .get<Semester>(`${this.base}/active`)
        .pipe(shareReplay(1)); // cache last successful result
    }
    return this.activeSemesterCache$;
  }
}
