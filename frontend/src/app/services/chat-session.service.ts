import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatSession, ChatMessage, CreateChatSession, UpdateChatSession } from '../models/chat-session.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class ChatSessionService {
  private apiUrl = 'http://localhost:8081/api/conversations';

  constructor(private http: HttpClient) {}

  getAllSessions(): Observable<ChatSession[]> {
    return this.http.get<ChatSession[]>(this.apiUrl);
  }

  getActiveSessions(): Observable<ChatSession[]> {
    return this.http.get<ChatSession[]>(`${this.apiUrl}/active`);
  }

  getSession(id: number): Observable<ChatSession> {
    return this.http.get<ChatSession>(`${this.apiUrl}/${id}`);
  }

  createSession(session: CreateChatSession): Observable<ChatSession> {
    return this.http.post<ChatSession>(this.apiUrl, session);
  }

  createSessionWithAutoTitle(firstMessage: string, jiraConnectionId?: number): Observable<ChatSession> {
    return this.http.post<ChatSession>(`${this.apiUrl}/auto-title`, {
      title: firstMessage,
      jiraConnectionId: jiraConnectionId
    });
  }

  updateSession(id: number, session: UpdateChatSession): Observable<ChatSession> {
    return this.http.patch<ChatSession>(`${this.apiUrl}/${id}`, session);
  }

  deleteSession(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.apiUrl}/${id}`);
  }

  addMessage(sessionId: number, message: ChatMessage): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/${sessionId}/messages`, message);
  }

  getMessages(sessionId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/${sessionId}/messages`);
  }
}
