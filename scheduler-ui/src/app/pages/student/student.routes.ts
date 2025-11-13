import { Routes } from '@angular/router';
import { StudentLayoutComponent } from './student-layout.component';
import { AuthGuard } from '../../core/gaurds/authGaurd.services';

export const routes: Routes = [
  {
    path: '',
    component: StudentLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'browse', pathMatch: 'full' },
      {
        path: 'browse',
        loadComponent: () => import('./course-browser/course-browser.component')
          .then(m => m.CourseBrowserComponent)
      },
      {
        path: 'schedule',
        loadComponent: () => import('./my-schedule/my-schedule.component')
          .then(m => m.MyScheduleComponent)
      },
      {
        path: 'progress',
        loadComponent: () => import('./my-progress/my-progress.component')
          .then(m => m.MyProgressComponent)
      }
    ]
  }
];
