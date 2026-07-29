import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Observable } from 'rxjs';
import { User } from '../../../models/auth.model';
import { NotificationService } from '../../../services/notification.service';
import { Notification } from '../../../models/notification.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  currentUser$: Observable<User | null>;
  isDarkMode: boolean = false;
  showNotifications: boolean = false;
  unreadNotifications: number = 0;
  notifications: Notification[] = [];
  notificationsLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.currentUser$ = this.authService.currentUser;
  }

  ngOnInit(): void {
    // Check for saved dark mode preference
    const savedDarkMode = localStorage.getItem('darkMode');
    if (savedDarkMode) {
      this.isDarkMode = savedDarkMode === 'true';
    }
    this.refreshNotifications();
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('darkMode', this.isDarkMode.toString());
    // Dark mode toggle is design-only for now
    // Implementation would require actual CSS variables update
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) this.refreshNotifications();
  }

  refreshNotifications(): void {
    this.notificationsLoading = true;
    this.notificationService.getNotifications().subscribe({
      next: notifications => {
        this.notifications = notifications;
        this.unreadNotifications = notifications.filter(item => !item.read).length;
        this.notificationsLoading = false;
      },
      error: () => this.notificationsLoading = false
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.read) return;
    this.notificationService.markAsRead(notification.id).subscribe(updated => {
      notification.read = updated.read;
      this.unreadNotifications = Math.max(0, this.unreadNotifications - 1);
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.notifications.forEach(item => item.read = true);
      this.unreadNotifications = 0;
    });
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  getUserInitials(user: User | null): string {
    if (!user) return '?';
    const firstName = user.firstName?.charAt(0) || '';
    const lastName = user.lastName?.charAt(0) || '';
    return (firstName + lastName).toUpperCase() || '?';
  }

  getUserFullName(user: User | null): string {
    if (!user) return 'User';
    return `${user.firstName} ${user.lastName}`.trim() || 'User';
  }
}
