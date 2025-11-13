import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';        // ✅ Add this
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { UserContextService } from '../../core/services/user-context.service';
import { AuthService } from '../../core/services/auth.services';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,               // ✅ Required for ngModel
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email = '';

  constructor(
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar,
    private userCtx: UserContextService
  ) {}

  login() {
    if (!this.email.trim()) return;

    this.auth.login(this.email).subscribe({
      next: (user) => {
        this.userCtx.setUser(user);
        if (user.role === 'STUDENT') {
          this.router.navigate(['/student']);
        } else if (user.role === 'TEACHER') {
          this.router.navigate(['/teacher']);
        }
      },
      error: () => {
        this.snack.open('Invalid email or user not found.', 'Close', { duration: 3000 });
      }
    });
  }

  quickLogin(role: 'STUDENT' | 'TEACHER') {
    this.email =
      role === 'STUDENT'
        ? 'karen.allen11@student.maplewood.edu'
        : 'paul.phillips@maplewood.edu';
    this.login();
  }
}
