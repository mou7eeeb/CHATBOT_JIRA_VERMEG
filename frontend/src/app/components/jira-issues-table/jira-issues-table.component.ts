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
  selectedStatus = 'all';

  get filteredTickets(): JiraTicketDisplay[] {
    if (this.selectedStatus === 'all') return this.tickets;
    return this.tickets.filter(ticket => this.getStatusGroup(ticket.status) === this.selectedStatus);
  }

  get availableFilters(): { value: string; label: string; count: number }[] {
    const filters = [
      { value: 'all', label: 'Toutes', count: this.tickets.length },
      { value: 'todo', label: 'À faire', count: 0 },
      { value: 'in-progress', label: 'En cours', count: 0 },
      { value: 'done', label: 'Terminées', count: 0 },
      { value: 'blocked', label: 'Bloquées', count: 0 }
    ];

    this.tickets.forEach(ticket => {
      const filter = filters.find(item => item.value === this.getStatusGroup(ticket.status));
      if (filter) filter.count++;
    });

    return filters.filter(filter => filter.value === 'all' || filter.count > 0);
  }

  selectStatus(status: string): void {
    this.selectedStatus = status;
  }

  trackByKey(_index: number, ticket: JiraTicketDisplay): string {
    return ticket.key;
  }

  private getStatusGroup(status: string): string {
    const value = this.normalize(status);
    if (this.containsAny(value, ['done', 'closed', 'resolved', 'termine', 'ferme'])) return 'done';
    if (this.containsAny(value, ['blocked', 'bloque', 'error'])) return 'blocked';
    if (this.containsAny(value, ['progress', 'cours', 'active', 'review', 'revue'])) return 'in-progress';
    return 'todo';
  }

  private normalize(value: string): string {
    return (value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  }

  private containsAny(value: string, terms: string[]): boolean {
    return terms.some(term => value.includes(term));
  }

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
