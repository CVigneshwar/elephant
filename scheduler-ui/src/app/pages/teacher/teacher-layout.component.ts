import { Component } from '@angular/core';
import { Router, RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { UserContextService } from '../../core/services/user-context.service';

@Component({
  selector: 'app-teacher-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, MatButtonModule],
  templateUrl: './teacher-layout.component.html',
  styleUrls: ['./teacher-layout.component.scss']
})
export class TeacherLayoutComponent {

  userName = '';

  constructor(private userCtx: UserContextService, private router: Router) {
    const user = this.userCtx.getUser();
    this.userName = user ? user.name : '';
  }

  logout() {
    this.userCtx.clearUser();
    this.router.navigate(['/login']);
  }
}