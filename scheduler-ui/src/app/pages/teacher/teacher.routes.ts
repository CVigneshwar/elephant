import { Routes } from '@angular/router';
import { TeacherLayoutComponent } from './teacher-layout.component';
import { AuthGuard } from '../../core/gaurds/authGaurd.services';

export const routes: Routes = [
  {
    path: '',
    component: TeacherLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'schedule-generator', pathMatch: 'full' },

      {
        path: 'schedule-generator',
        loadComponent: () =>
          import('./admin-schedule/admin-schedule.component')
            .then(m => m.AdminScheduleComponent)
      },
      {
        path: 'weekly-timetable',
        loadComponent: () =>
          import('./weekly-timetable/weekly-timetable.component')
            .then(m => m.WeeklyTimetableComponent)
      },
      {
        path: 'utilization',
        loadComponent: () =>
          import('./utilization-dashboard/utilization-dashboard.component')
            .then(m => m.UtilizationDashboardComponent)
      }
    ]
  }
];
