import { Component } from '@angular/core';
import { Router, RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { UserContextService } from '../../core/services/user-context.service';

@Component({
  selector: 'app-student-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, MatButtonModule],
  template: `
    <header class="navbar">
      <div class="nav-left">
        <h1 class="logo">🎓 Student Portal</h1>
        <nav class="menu">
          <a mat-button routerLink="browse" routerLinkActive="active">Browse Courses</a>
          <a mat-button routerLink="schedule" routerLinkActive="active">My Schedule</a>
          <a mat-button routerLink="progress" routerLinkActive="active">My Progress</a>
        </nav>
      </div>

      <div class="nav-right">
        <span class="user">👤 {{ userName }}</span>
        <button mat-stroked-button color="warn" (click)="logout()">Logout</button>
      </div>
    </header>

    <router-outlet></router-outlet>
  `,
  styles: [`
    .navbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: linear-gradient(90deg, #1565c0, #1976d2);
      color: #fff;
      padding: 12px 28px;
      box-shadow: 0 2px 5px rgba(0,0,0,0.15);
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .logo {
      font-size: 20px;
      margin: 0;
      font-weight: 600;
    }

    .menu {
      display: flex;
      gap: 12px;
      margin-left: 20px;
    }

    .menu a {
      font-weight: 500;
      color: #e3f2fd;
      border-radius: 6px;
      transition: all 0.25s ease;
      text-transform: none;
      font-size: 14px;
      padding: 6px 14px;
    }

    .menu a:hover {
      background: rgba(255, 255, 255, 0.15);
      color: #fff;
    }

    .menu a.active {
      background: #0d47a1;
      color: #fff;
      font-weight: 600;
      box-shadow: inset 0 -2px 0 #90caf9;
    }

    .nav-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .user {
      font-weight: 500;
      color: #e3f2fd;
      font-size: 14px;
    }

    @media (max-width: 768px) {
      .navbar {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
      }

      .menu {
        flex-wrap: wrap;
        margin-left: 0;
      }

      .nav-right {
        align-self: flex-end;
      }
    }
  `]
})
export class StudentLayoutComponent {
  userName = '';

  constructor(private userCtx: UserContextService, private router: Router) {
    const user = this.userCtx.getUser();
    this.userName = user ? `${user.name}` : '';
  }

  logout() {
    this.userCtx.clearUser();
    this.router.navigate(['/login']);
  }
}
