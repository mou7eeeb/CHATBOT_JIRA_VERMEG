export interface ChatMessage {
  id?: number;
  content: string;
  messageType: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'ERROR';
  generatedJql?: string;
  jiraResults?: string;
  ticketCount?: number;
  createdAt?: string;
}

export interface ChatSession {
  id?: number;
  title: string;
  jiraConnectionId?: number;
  jiraConnectionName?: string;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
  lastMessagePreview?: string;
  messageCount?: number;
  messages?: ChatMessage[];
}

export interface CreateChatSession {
  title: string;
  jiraConnectionId?: number;
}

export interface UpdateChatSession {
  title: string;
}
