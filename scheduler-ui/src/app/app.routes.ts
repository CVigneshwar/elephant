import { Routes } from '@angular/router';
import { AdminScheduleComponent } from './pages/admin-schedule/admin-schedule.component';
import { WeeklyTimetableComponent } from './pages/weekly-timetable/weekly-timetable.component';
import { UtilizationDashboardComponent } from './pages/utilization-dashboard/utilization-dashboard.component';
import { LoginComponent } from './pages/login/login.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },

  // Only keep student and teacher
  { path: 'student', loadChildren: () => import('./pages/student/student.routes').then(m => m.routes) },
  { path: 'teacher', loadChildren: () => import('./pages/teacher/teacher.routes').then(m => m.routes) },

  { path: '**', redirectTo: 'login', pathMatch: 'full' }
];

