import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { SidebarComponent } from './components/shared/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Enterprise Jira Chatbot';

  constructor(
    public authService: AuthService,
    public router: Router
  ) {}

  get isAuthPage(): boolean {
    const url = this.router.url;
    return url.includes('/login') || url.includes('/signup') || url.includes('/auth') || url.includes('/forgot-password');
  }
}
