import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface JiraTicketDisplay {
  key: string;
  summary: string;
  status: string;
  priority: string;
  assignee: string;
  issueType: string;
  updated: string;
  url: string;
}

@Component({
  selector: 'app-jira-issues-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './jira-issues-table.component.html',
  styleUrls: ['./jira-issues-table.component.scss']
})
export class JiraIssuesTableComponent {
  @Input() tickets: JiraTicketDisplay[] = [];

  getStatusClass(status: string): string {
    const statusLower = status?.toLowerCase() || '';
    
    if (statusLower.includes('done') || statusLower.includes('terminé') || statusLower.includes('closed') || statusLower.includes('fermé')) {
      return 'badge-done';
    }
    if (statusLower.includes('progress') || statusLower.includes('cours') || statusLower.includes('active') || statusLower.includes('review')) {
      return 'badge-in-progress';
    }
    if (statusLower.includes('blocked') || statusLower.includes('bloqué') || statusLower.includes('error')) {
      return 'badge-blocked';
    }
    return 'badge-todo';
  }

  getPriorityClass(priority: string): string {
    const priorityLower = priority?.toLowerCase() || '';
    
    if (priorityLower.includes('highest') || priorityLower.includes('très élevée') || priorityLower.includes('critical')) {
      return 'priority-highest';
    }
    if (priorityLower.includes('high') || priorityLower.includes('élevée')) {
      return 'priority-high';
    }
    if (priorityLower.includes('medium') || priorityLower.includes('moyenne')) {
      return 'priority-medium';
    }
    if (priorityLower.includes('low') || priorityLower.includes('faible')) {
      return 'priority-low';
    }
    return 'priority-lowest';
  }

  openJiraTicket(url: string): void {
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }

  getTicketUrl(key: string): string {
    // This would need to be configured based on the Jira base URL
    // For now, return a placeholder
    return `https://your-jira-instance.atlassian.net/browse/${key}`;
  }
}
