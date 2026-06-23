import { JiraTicket } from './jira-ticket.model';

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  message: string;
  jqlQuery: string;
  tickets: JiraTicket[];
  totalTickets: number;
  success: boolean;
  error?: string;
}

export interface ChatMessage {
  id: string;
  text: string;
  sender: 'user' | 'bot';
  timestamp: Date;
  tickets?: JiraTicket[];
  jqlQuery?: string;
  isLoading?: boolean;
  error?: boolean;
}
