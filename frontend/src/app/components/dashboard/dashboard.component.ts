import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { JiraConnectionService } from '../../services/jira-connection.service';
import { User } from '../../models/auth.model';
import { JiraConnectionResponse } from '../../models/jira-connection.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  jiraConnections: JiraConnectionResponse[] = [];
  loading = true;
  sidebarCollapsed = false;

  constructor(
    private authService: AuthService,
    private jiraConnectionService: JiraConnectionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadJiraConnections();
  }

  loadJiraConnections(): void {
    this.jiraConnectionService.getAllConnections().subscribe({
      next: (connections) => {
        this.jiraConnections = connections;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading connections:', error);
        this.loading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  get userInitials(): string {
    if (!this.currentUser) return '';
    return `${this.currentUser.firstName[0]}${this.currentUser.lastName[0]}`.toUpperCase();
  }
}
