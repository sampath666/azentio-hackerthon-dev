import { Routes } from '@angular/router';
import { Dashboard } from './dashboard';

export const routes: Routes = [
  {
    path: 'dashboard',
    component: Dashboard
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];