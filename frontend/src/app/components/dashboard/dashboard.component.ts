import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { JiraConnectionService } from '../../services/jira-connection.service';
import { AdminService, DashboardStats } from '../../services/admin.service';
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
  adminStats: DashboardStats | null = null;
  adminStatsLoading = false;

  constructor(
    private authService: AuthService,
    private jiraConnectionService: JiraConnectionService,
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.loadJiraConnections();
    if (this.isAdmin) {
      this.loadAdminStats();
    }
  }

  loadAdminStats(): void {
    this.adminStatsLoading = true;
    this.adminService.getDashboardStats().subscribe({
      next: (stats) => {
        this.adminStats = stats;
        this.adminStatsLoading = false;
      },
      error: (error) => {
        console.error('Error loading admin stats:', error);
        this.adminStatsLoading = false;
      }
    });
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
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

  get activeConnectionsCount(): number {
    return this.jiraConnections.filter(c => c.isActive).length;
  }

  get userInitials(): string {
    if (!this.currentUser) return '';
    return `${this.currentUser.firstName[0]}${this.currentUser.lastName[0]}`.toUpperCase();
  }
}
