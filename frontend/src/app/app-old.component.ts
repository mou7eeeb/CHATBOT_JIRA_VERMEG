import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from './services/chat.service';
import { ChatMessage } from './models/chat.model';
import { JiraTicket } from './models/jira-ticket.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponentOld {
  messages: ChatMessage[] = [];
  userInput: string = '';
  isLoading: boolean = false;
  selectedStatus: string = 'all';

  constructor(private chatService: ChatService) {
    this.addWelcomeMessage();
  }

  addWelcomeMessage(): void {
    const welcomeMessage: ChatMessage = {
      id: this.generateId(),
      text: 'Hello! I\'m your Jira assistant. Ask me about tickets using natural language. For example: "show me open bugs in CRM" or "all high priority issues".',
      sender: 'bot',
      timestamp: new Date()
    };
    this.messages.push(welcomeMessage);
  }

  sendMessage(): void {
    if (!this.userInput.trim() || this.isLoading) {
      return;
    }

    const userMessage: ChatMessage = {
      id: this.generateId(),
      text: this.userInput,
      sender: 'user',
      timestamp: new Date()
    };
    this.messages.push(userMessage);

    const loadingMessage: ChatMessage = {
      id: this.generateId(),
      text: 'Searching Jira tickets...',
      sender: 'bot',
      timestamp: new Date(),
      isLoading: true
    };
    this.messages.push(loadingMessage);

    const query = this.userInput;
    this.userInput = '';
    this.isLoading = true;

    this.chatService.sendMessage(query).subscribe({
      next: (response) => {
        this.messages = this.messages.filter(m => !m.isLoading);
        
        const botMessage: ChatMessage = {
          id: this.generateId(),
          text: response.message,
          sender: 'bot',
          timestamp: new Date(),
          tickets: response.tickets,
          jqlQuery: response.jqlQuery
        };
        this.messages.push(botMessage);
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: (error) => {
        this.messages = this.messages.filter(m => !m.isLoading);
        
        const errorMessage: ChatMessage = {
          id: this.generateId(),
          text: 'Sorry, I encountered an error. Please make sure the backend is running and try again.',
          sender: 'bot',
          timestamp: new Date(),
          error: true
        };
        this.messages.push(errorMessage);
        this.isLoading = false;
        this.scrollToBottom();
      }
    });
  }

  getFilteredTickets(tickets: JiraTicket[] | undefined): JiraTicket[] {
    if (!tickets) return [];
    if (this.selectedStatus === 'all') return tickets;
    return tickets.filter(t => t.fields.status.name.toLowerCase() === this.selectedStatus.toLowerCase());
  }

  getUniqueStatuses(tickets: JiraTicket[] | undefined): string[] {
    if (!tickets) return [];
    const statuses = tickets.map(t => t.fields.status.name);
    return ['all', ...Array.from(new Set(statuses))];
  }

  getStatusColor(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower.includes('done') || statusLower.includes('closed')) return '#00875A';
    if (statusLower.includes('progress')) return '#0052CC';
    if (statusLower.includes('open') || statusLower.includes('to do')) return '#6554C0';
    return '#42526E';
  }

  getPriorityColor(priority: string): string {
    const priorityLower = priority.toLowerCase();
    if (priorityLower.includes('highest') || priorityLower.includes('critical')) return '#DE350B';
    if (priorityLower.includes('high')) return '#FF5630';
    if (priorityLower.includes('medium')) return '#FF991F';
    if (priorityLower.includes('low')) return '#36B37E';
    return '#42526E';
  }

  clearChat(): void {
    this.messages = [];
    this.addWelcomeMessage();
  }

  private generateId(): string {
    return Date.now().toString() + Math.random().toString(36).substr(2, 9);
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }
}
