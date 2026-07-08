import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { AuthSwitchComponent } from './components/auth/auth-switch.component';
import { ForgotPasswordComponent } from './components/auth/forgot-password.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'auth', component: AuthSwitchComponent },
  { path: 'login', redirectTo: '/auth', pathMatch: 'full' },
  { path: 'signup', redirectTo: '/auth', pathMatch: 'full' },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'jira-connections',
    loadComponent: () => import('./components/jira-connections/jira-connections.component').then(m => m.JiraConnectionsComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'chatbot',
    loadComponent: () => import('./components/chatbot/chatbot.component').then(m => m.ChatbotComponent),
    canActivate: [AuthGuard]
  },
  { path: 'admin', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'admin/users',
    loadComponent: () => import('./components/admin/admin-users/admin-users.component').then(m => m.AdminUsersComponent),
    canActivate: [AuthGuard, AdminGuard]
  },
  { path: '**', redirectTo: '/' }
];
