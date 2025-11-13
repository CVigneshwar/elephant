import { Injectable } from '@angular/core';

export interface User {
  id: number;
  name: string;
  role: 'STUDENT' | 'TEACHER';
  email: string;
}

@Injectable({ providedIn: 'root' })
export class UserContextService {
  private user: User | null = null;

  setUser(user: User) {
    this.user = user;
    localStorage.setItem('user', JSON.stringify(user));
  }

  getUser(): User | null {
    if (!this.user) {
      const data = localStorage.getItem('user');
      this.user = data ? JSON.parse(data) : null;
    }
    return this.user;
  }

  clearUser() {
    this.user = null;
    localStorage.removeItem('user');
  }

  isStudent() { return this.getUser()?.role === 'STUDENT'; }
  isTeacher() { return this.getUser()?.role === 'TEACHER'; }
}
