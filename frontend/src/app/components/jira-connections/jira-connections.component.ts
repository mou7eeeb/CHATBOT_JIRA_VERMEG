import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { JiraConnectionService } from '../../services/jira-connection.service';
import { JiraConnection, JiraConnectionResponse } from '../../models/jira-connection.model';
import { SidebarComponent } from '../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-jira-connections',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './jira-connections.component.html',
  styleUrls: ['./jira-connections.component.css']
})
export class JiraConnectionsComponent implements OnInit {
  connections: JiraConnectionResponse[] = [];
  loading = true;
  showForm = false;
  editMode = false;
  currentConnection: JiraConnection = this.getEmptyConnection();
  error = '';
  success = '';

  constructor(private jiraConnectionService: JiraConnectionService) {}

  ngOnInit(): void {
    this.loadConnections();
  }

  loadConnections(): void {
    this.loading = true;
    this.jiraConnectionService.getAllConnections().subscribe({
      next: (connections) => {
        this.connections = connections;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading connections:', error);
        this.error = 'Failed to load connections';
        this.loading = false;
      }
    });
  }

  showAddForm(): void {
    this.currentConnection = this.getEmptyConnection();
    this.editMode = false;
    this.showForm = true;
    this.error = '';
    this.success = '';
  }

  showEditForm(connection: JiraConnectionResponse): void {
    this.currentConnection = {
      id: connection.id,
      connectionName: connection.connectionName,
      jiraBaseUrl: connection.jiraBaseUrl,
      jiraEmail: connection.jiraEmail,
      jiraApiToken: '', // Don't populate for security
      isDefault: connection.isDefault,
      isActive: connection.isActive
    };
    this.editMode = true;
    this.showForm = true;
    this.error = '';
    this.success = '';
  }

  cancelForm(): void {
    this.showForm = false;
    this.currentConnection = this.getEmptyConnection();
    this.error = '';
    this.success = '';
  }

  saveConnection(): void {
    this.error = '';
    this.success = '';

    if (this.editMode && this.currentConnection.id) {
      this.jiraConnectionService.updateConnection(this.currentConnection.id, this.currentConnection).subscribe({
        next: () => {
          this.success = 'Connection updated successfully';
          this.showForm = false;
          this.loadConnections();
        },
        error: (error) => {
          this.error = error.error?.message || 'Failed to update connection';
        }
      });
    } else {
      this.jiraConnectionService.createConnection(this.currentConnection).subscribe({
        next: () => {
          this.success = 'Connection created successfully';
          this.showForm = false;
          this.loadConnections();
        },
        error: (error) => {
          this.error = error.error?.message || 'Failed to create connection';
        }
      });
    }
  }

  testConnection(id: number): void {
    this.jiraConnectionService.testConnection(id).subscribe({
      next: (result) => {
        this.success = result.lastTestMessage || 'Connection test successful';
        this.loadConnections();
      },
      error: (error) => {
        this.error = error.error?.message || 'Connection test failed';
      }
    });
  }

  deleteConnection(id: number): void {
    if (confirm('Are you sure you want to delete this connection?')) {
      this.jiraConnectionService.deleteConnection(id).subscribe({
        next: () => {
          this.success = 'Connection deleted successfully';
          this.loadConnections();
        },
        error: (error) => {
          this.error = error.error?.message || 'Failed to delete connection';
        }
      });
    }
  }

  setAsDefault(id: number): void {
    this.jiraConnectionService.setAsDefault(id).subscribe({
      next: () => {
        this.success = 'Connection set as default';
        this.loadConnections();
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to set as default';
      }
    });
  }

  toggleStatus(id: number): void {
    this.jiraConnectionService.toggleStatus(id).subscribe({
      next: () => {
        this.success = 'Connection status updated';
        this.loadConnections();
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to update status';
      }
    });
  }

  private getEmptyConnection(): JiraConnection {
    return {
      connectionName: '',
      jiraBaseUrl: '',
      jiraEmail: '',
      jiraApiToken: '',
      isDefault: false,
      isActive: true
    };
  }
}
