import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { ChatSessionService } from '../../services/chat-session.service';
import { ChatMessage } from '../../models/chat.model';
import { ChatSession, ChatMessage as ChatSessionMessage } from '../../models/chat-session.model';
import { JiraTicket } from '../../models/jira-ticket.model';
import { SidebarComponent } from '../shared/sidebar/sidebar.component';
import { JiraIssuesTableComponent, JiraTicketDisplay } from '../jira-issues-table/jira-issues-table.component';
import { JiraProjectsTableComponent, JiraProjectDisplay } from '../jira-projects-table/jira-projects-table.component';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent, JiraIssuesTableComponent, JiraProjectsTableComponent],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit {
  messages: ChatMessage[] = [];
  userInput: string = '';
  isLoading: boolean = false;
  selectedStatus: string = 'all';
  currentSessionId: number | null = null;
  sessionTitle: string = 'New Conversation';
  conversations: ChatSession[] = [];
  showConversationList: boolean = true;
  searchQuery: string = '';

  constructor(
    private chatService: ChatService,
    private chatSessionService: ChatSessionService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadConversations();
    const sessionId = this.route.snapshot.paramMap.get('id');
    if (sessionId) {
      this.loadConversation(parseInt(sessionId));
    } else {
      // No conversation ID in URL, load the most recent conversation
      this.loadMostRecentConversation();
    }
  }

  loadConversations(): void {
    this.chatSessionService.getAllSessions().subscribe({
      next: (sessions) => {
        this.conversations = sessions;
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
      }
    });
  }

  loadMostRecentConversation(): void {
    this.chatSessionService.getAllSessions().subscribe({
      next: (sessions) => {
        if (sessions.length > 0) {
          // Load the most recent conversation (first in list since sorted by updatedAt DESC)
          const mostRecent = sessions[0];
          if (mostRecent.id) {
            this.loadConversation(mostRecent.id);
          }
        } else {
          // No conversations yet, show welcome message
          this.addWelcomeMessage();
        }
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        // Fallback to welcome message on error
        this.addWelcomeMessage();
      }
    });
  }

  loadConversation(sessionId: number): void {
    this.chatSessionService.getSession(sessionId).subscribe({
      next: (session) => {
        this.currentSessionId = session.id || null;
        this.sessionTitle = session.title;
        this.messages = [];
        if (session.messages) {
          session.messages.forEach(msg => {
            this.messages.push({
              id: msg.id?.toString() || this.generateId(),
              text: msg.content,
              sender: msg.messageType === 'USER' ? 'user' : 'bot',
              timestamp: new Date(msg.createdAt || new Date()),
              tickets: msg.jiraResults ? JSON.parse(msg.jiraResults) : undefined,
              jqlQuery: msg.generatedJql,
              error: msg.messageType === 'ERROR'
            });
          });
        }
      },
      error: (error) => {
        console.error('Error loading conversation:', error);
        this.addWelcomeMessage();
      }
    });
  }

  createNewConversation(): void {
    this.currentSessionId = null;
    this.sessionTitle = 'New Conversation';
    this.messages = [];
    this.addWelcomeMessage();
    this.router.navigate(['/chatbot'], { replaceUrl: true });
  }

  openConversation(id: number): void {
    this.router.navigate(['/chatbot', id]);
  }

  get filteredConversations(): ChatSession[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) {
      return this.conversations;
    }
    return this.conversations.filter(conv =>
      conv.title?.toLowerCase().includes(query) ||
      conv.lastMessagePreview?.toLowerCase().includes(query)
    );
  }

  deleteConversation(id: number): void {
    if (id && confirm('Are you sure you want to delete this conversation? This action is permanent.')) {
      this.chatSessionService.deleteSession(id).subscribe({
        next: () => {
          this.conversations = this.conversations.filter(conv => conv.id !== id);
          if (this.currentSessionId === id) {
            this.createNewConversation();
          }
        },
        error: (error) => {
          console.error('Unable to delete conversation:', error);
        }
      });
    }
  }

  renameConversation(id: number): void {
    if (!id) return;
    const newTitle = prompt('Enter new title:');
    if (newTitle && newTitle.trim()) {
      this.chatSessionService.updateSession(id, { title: newTitle.trim() }).subscribe({
        next: () => {
          this.loadConversations();
        },
        error: (error) => {
          console.error('Unable to rename conversation:', error);
        }
      });
    }
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

    const query = this.userInput;
    this.userInput = '';
    this.isLoading = true;

    // Create conversation on first message if not exists
    if (!this.currentSessionId) {
      this.chatSessionService.createSessionWithAutoTitle(query).subscribe({
        next: (session) => {
          this.currentSessionId = session.id || null;
          this.sessionTitle = session.title;
          // Update URL to include conversation ID for persistence
          if (this.currentSessionId) {
            this.router.navigate(['/chatbot', this.currentSessionId], { replaceUrl: true });
          }
          // Save user message to the new conversation
          this.saveMessageToDB(userMessage, 'USER');
          // Proceed with AI call
          this.proceedWithAIMessage(query, userMessage);
        },
        error: (error) => {
          console.error('Error creating conversation:', error);
          // Fallback: proceed without saving
          this.proceedWithAIMessage(query, userMessage);
        }
      });
    } else {
      // Save user message to existing conversation
      this.saveMessageToDB(userMessage, 'USER');
      // Proceed with AI call
      this.proceedWithAIMessage(query, userMessage);
    }
  }

  private proceedWithAIMessage(query: string, userMessage: ChatMessage): void {
    const loadingMessage: ChatMessage = {
      id: this.generateId(),
      text: 'Thinking...',
      sender: 'bot',
      timestamp: new Date(),
      isLoading: true
    };
    this.messages.push(loadingMessage);

    this.chatService.sendAIMessage(query).subscribe({
      next: (response) => {
        this.messages = this.messages.filter(m => !m.isLoading);

        const botMessage: ChatMessage = {
          id: this.generateId(),
          text: response.success ? response.message : (response.error || response.message),
          sender: 'bot',
          timestamp: new Date(),
          tickets: response.tickets,
          jqlQuery: response.jqlQuery,
          error: !response.success
        };
        this.messages.push(botMessage);

        // Save bot message to database
        this.saveMessageToDB(botMessage, response.success ? 'ASSISTANT' : 'ERROR', response);

        this.isLoading = false;
        this.scrollToBottom();
      },
      error: (error) => {
        this.messages = this.messages.filter(m => !m.isLoading);

        let errorText = 'Sorry, I encountered an error. Please try again.';
        console.error('Chat error:', error);

        if (error.message) {
          errorText = error.message;
        } else if (error.error && error.error.message) {
          errorText = error.error.message;
        } else if (typeof error === 'string') {
          errorText = error;
        }

        const errorMessage: ChatMessage = {
          id: this.generateId(),
          text: errorText,
          sender: 'bot',
          timestamp: new Date(),
          error: true
        };
        this.messages.push(errorMessage);

        // Save error message to database
        this.saveMessageToDB(errorMessage, 'ERROR');

        this.isLoading = false;
        this.scrollToBottom();
      }
    });
  }

  saveMessageToDB(message: ChatMessage, messageType: 'USER' | 'ASSISTANT' | 'ERROR', response?: any): void {
    if (!this.currentSessionId) return;

    const sessionMessage: ChatSessionMessage = {
      content: message.text,
      messageType: messageType,
      generatedJql: message.jqlQuery,
      jiraResults: message.tickets ? JSON.stringify(message.tickets) : undefined,
      ticketCount: message.tickets?.length
    };

    this.chatSessionService.addMessage(this.currentSessionId, sessionMessage).subscribe({
      next: (savedMessage) => {
        console.log('Message saved to conversation:', this.currentSessionId);
      },
      error: (err) => console.error('Error saving message:', err)
    });
  }

  saveMessages(): void {
    if (!this.currentSessionId) return;

    const sessionId = this.currentSessionId;
    this.messages.forEach((msg, index) => {
      if (index === 0) return; // Skip welcome message

      const messageType = msg.sender === 'user' ? 'USER' : (msg.error ? 'ERROR' : 'ASSISTANT');
      const sessionMessage: ChatSessionMessage = {
        content: msg.text,
        messageType: messageType,
        generatedJql: msg.jqlQuery,
        jiraResults: msg.tickets ? JSON.stringify(msg.tickets) : undefined,
        ticketCount: msg.tickets?.length
      };

      this.chatSessionService.addMessage(sessionId, sessionMessage).subscribe({
        error: (err) => console.error('Error saving message:', err)
      });
    });
  }

  handleEnterKey(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.key === 'Enter' && !keyboardEvent.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  autoResize(textarea: HTMLTextAreaElement): void {
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
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

  convertToTicketDisplay(tickets: JiraTicket[] | undefined): JiraTicketDisplay[] {
    if (!tickets) return [];
    return tickets.map(ticket => ({
      key: ticket.key,
      summary: ticket.fields.summary,
      status: ticket.fields.status?.name || 'Unknown',
      priority: ticket.fields.priority?.name || 'Unknown',
      assignee: ticket.fields.assignee?.displayName || 'Unassigned',
      issueType: ticket.fields.issuetype?.name || 'Unknown',
      updated: ticket.fields.updated,
      url: `https://your-jira-instance.atlassian.net/browse/${ticket.key}`
    }));
  }

  convertToProjectDisplay(projects: any[] | undefined): JiraProjectDisplay[] {
    if (!projects) return [];
    return projects.map(project => ({
      key: project.key,
      name: project.name,
      projectType: project.projectTypeKey || 'Software',
      lead: project.lead?.displayName || 'Unknown',
      url: `https://your-jira-instance.atlassian.net/browse/${project.key}`
    }));
  }
}
