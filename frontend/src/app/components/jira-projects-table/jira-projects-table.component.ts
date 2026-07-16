import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface JiraProjectDisplay {
  key: string;
  name: string;
  projectType: string;
  lead: string;
  url: string;
}

@Component({
  selector: 'app-jira-projects-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './jira-projects-table.component.html',
  styleUrls: ['./jira-projects-table.component.scss']
})
export class JiraProjectsTableComponent {
  @Input() projects: JiraProjectDisplay[] = [];

  openJiraProject(url: string): void {
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }

  getProjectUrl(key: string): string {
    // This would need to be configured based on the Jira base URL
    return `https://your-jira-instance.atlassian.net/browse/${key}`;
  }
}
