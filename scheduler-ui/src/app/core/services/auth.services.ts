import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: 'STUDENT' | 'TEACHER';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  /** 🔐 Calls backend login API by email */
  login(email: string): Observable<AuthUser> {
    const params = new HttpParams().set('email', email.trim());
    return this.http.get<AuthUser>(`${this.baseUrl}/login`, { params });
  }
}
