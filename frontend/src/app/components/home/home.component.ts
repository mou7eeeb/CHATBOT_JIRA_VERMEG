import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface Feature {
  icon: string;
  title: string;
  description: string;
}

interface Step {
  number: string;
  title: string;
  description: string;
}

interface Stat {
  value: number;
  suffix: string;
  label: string;
  current: number;
}

interface CompanyLogo {
  name: string;
}

interface TicketCard {
  id: string;
  title: string;
  priority: string;
  priorityColor: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  isScrolled = false;
  isDarkMode = false;
  mobileMenuOpen = false;

  // Typing animation for hero chatbot mockup
  userMessage = 'Show me critical bugs in CRM';
  botMessage = 'I found 12 matching Jira tickets.';
  displayedUserMessage = '';
  displayedBotMessage = '';
  showBotTyping = false;
  showTicketCards = false;

  ticketCards: TicketCard[] = [
    { id: 'CRM-482', title: 'Login fails on SSO redirect', priority: 'Critical', priorityColor: '#EF4444' },
    { id: 'CRM-511', title: 'Payment webhook timeout', priority: 'Critical', priorityColor: '#EF4444' },
    { id: 'CRM-498', title: 'Dashboard chart not loading', priority: 'High', priorityColor: '#F59E0B' }
  ];

  companies: CompanyLogo[] = [
    { name: 'Vermeg' },
    { name: 'Atlassian' },
    { name: 'Capgemini' },
    { name: 'Sopra Steria' },
    { name: 'IBM' },
    { name: 'Microsoft' }
  ];

  features: Feature[] = [
    {
      icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      title: 'Natural Language Search',
      description: 'Ask questions in plain English and get instant, accurate Jira results powered by AI.'
    },
    {
      icon: 'M13 10V3L4 14h7v7l9-11h-7z',
      title: 'Automatic JQL Generation',
      description: 'Skip the syntax. Our AI translates your intent into optimized JQL queries automatically.'
    },
    {
      icon: 'M17 20h5v-2a4 4 0 00-3-3.87M9 20H4v-2a4 4 0 013-3.87m6-1.13a4 4 0 100-8 4 4 0 000 8zm6 0a4 4 0 100-8',
      title: 'Multiple Jira Accounts',
      description: 'Connect and switch seamlessly between unlimited Jira workspaces and projects.'
    },
    {
      icon: 'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z',
      title: 'AI Summaries',
      description: 'Get intelligent, concise summaries of long tickets, threads and epics in seconds.'
    },
    {
      icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5',
      title: 'Smart Ticket Analysis',
      description: 'Detect patterns, blockers and risks across your backlog with deep AI-driven analysis.'
    },
    {
      icon: 'M13 10V3L4 14h7v7l9-11h-7z',
      title: 'Real-Time Search',
      description: 'Lightning-fast search results streamed directly from your connected Jira instance.'
    },
    {
      icon: 'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 10-8 0v4h8z',
      title: 'Secure Authentication',
      description: 'Enterprise-grade JWT security with encrypted credentials and role-based access.'
    },
    {
      icon: 'M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z',
      title: 'Modern Dashboard',
      description: 'Beautiful, data-rich dashboard giving you full visibility over all your Jira activity.'
    }
  ];

  steps: Step[] = [
    { number: '01', title: 'Connect Jira Account', description: 'Securely link one or multiple Jira workspaces in seconds.' },
    { number: '02', title: 'Ask Your Question', description: 'Type a natural language question about your tickets or projects.' },
    { number: '03', title: 'AI Generates JQL', description: 'Our AI engine converts your question into an optimized JQL query.' },
    { number: '04', title: 'Jira API Executes Query', description: 'The query runs instantly against your connected Jira instance.' },
    { number: '05', title: 'Results Displayed Instantly', description: 'Get beautifully organized, actionable results in real time.' }
  ];

  stats: Stat[] = [
    { value: 50000, suffix: '+', label: 'Tickets Analyzed', current: 0 },
    { value: 100, suffix: '+', label: 'Projects', current: 0 },
    { value: 99.9, suffix: '%', label: 'Availability', current: 0 },
    { value: 10, suffix: 'x', label: 'Faster Search', current: 0 }
  ];

  private statsAnimated = false;
  private typingTimeouts: any[] = [];

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    const currentUser = this.authService.currentUserValue;
    if (currentUser && this.authService.isAuthenticated()) {
      this.router.navigate([currentUser.role === 'ADMIN' ? '/admin' : '/dashboard']);
      return;
    }

    this.startHeroTyping();
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme === 'dark';
    this.applyTheme();
  }

  ngOnDestroy(): void {
    this.typingTimeouts.forEach(t => clearTimeout(t));
  }

  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    this.isScrolled = window.scrollY > 40;
    this.checkStatsInView();
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme(): void {
    if (this.isDarkMode) {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen = false;
  }

  goToSignIn(): void {
    this.router.navigate(['/auth'], { queryParams: { mode: 'signin' } });
  }

  goToSignUp(): void {
    this.router.navigate(['/auth'], { queryParams: { mode: 'signup' } });
  }

  scrollToSection(sectionId: string): void {
    this.closeMobileMenu();
    const el = document.getElementById(sectionId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  private startHeroTyping(): void {
    this.typeText(this.userMessage, (text) => this.displayedUserMessage = text, 45, () => {
      const t1 = setTimeout(() => {
        this.showBotTyping = true;
        const t2 = setTimeout(() => {
          this.showBotTyping = false;
          this.typeText(this.botMessage, (text) => this.displayedBotMessage = text, 35, () => {
            const t3 = setTimeout(() => this.showTicketCards = true, 300);
            this.typingTimeouts.push(t3);
          });
        }, 1400);
        this.typingTimeouts.push(t2);
      }, 500);
      this.typingTimeouts.push(t1);
    });
  }

  private typeText(fullText: string, setter: (text: string) => void, speed: number, onDone?: () => void): void {
    let i = 0;
    const step = () => {
      if (i <= fullText.length) {
        setter(fullText.substring(0, i));
        i++;
        const t = setTimeout(step, speed);
        this.typingTimeouts.push(t);
      } else if (onDone) {
        onDone();
      }
    };
    step();
  }

  private checkStatsInView(): void {
    if (this.statsAnimated) return;
    const el = document.getElementById('statistics');
    if (!el) return;
    const rect = el.getBoundingClientRect();
    if (rect.top < window.innerHeight * 0.8 && rect.bottom > 0) {
      this.statsAnimated = true;
      this.animateStats();
    }
  }

  private animateStats(): void {
    const duration = 1800;
    const startTime = performance.now();

    const step = (now: number) => {
      const progress = Math.min((now - startTime) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);

      this.stats.forEach(stat => {
        stat.current = Math.round(stat.value * eased * 10) / 10;
      });

      if (progress < 1) {
        requestAnimationFrame(step);
      } else {
        this.stats.forEach(stat => stat.current = stat.value);
      }
    };

    requestAnimationFrame(step);
  }

  formatStat(stat: Stat): string {
    if (stat.suffix === '%') {
      return stat.current.toFixed(1);
    }
    if (stat.value >= 1000) {
      return Math.round(stat.current).toLocaleString();
    }
    return Math.round(stat.current).toString();
  }
}
