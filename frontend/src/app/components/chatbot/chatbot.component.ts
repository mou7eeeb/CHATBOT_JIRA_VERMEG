import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { ChatMessage } from '../../models/chat.model';
import { JiraTicket } from '../../models/jira-ticket.model';
import { SidebarComponent } from '../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit {
  messages: ChatMessage[] = [];
  userInput: string = '';
  isLoading: boolean = false;
  selectedStatus: string = 'all';

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    this.addWelcomeMessage();
  }

  addWelcomeMessage(): void {
    const welcomeMessage: ChatMessage = {
      id: this.generateId(),
      text: 'Hello! I\'m your AI assistant. Ask me anything you want - I can help with questions, explanations, advice, and much more!',
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
      text: 'Thinking...',
      sender: 'bot',
      timestamp: new Date(),
      isLoading: true
    };
    this.messages.push(loadingMessage);

    const query = this.userInput;
    this.userInput = '';
    this.isLoading = true;

    this.chatService.sendAIMessage(query).subscribe({
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
        
        let errorText = 'Sorry, I encountered an error. Please try again.';
        if (error.error && error.error.message) {
          errorText = error.error.message;
        } else if (error.message) {
          errorText = error.message;
        }
        
        const errorMessage: ChatMessage = {
          id: this.generateId(),
          text: errorText,
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
