import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JiraConnection, JiraConnectionResponse } from '../models/jira-connection.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class JiraConnectionService {
  private apiUrl = 'http://localhost:8081/api/jira-connections';

  constructor(private http: HttpClient) {}

  getAllConnections(): Observable<JiraConnectionResponse[]> {
    return this.http.get<JiraConnectionResponse[]>(this.apiUrl);
  }

  getConnection(id: number): Observable<JiraConnectionResponse> {
    return this.http.get<JiraConnectionResponse>(`${this.apiUrl}/${id}`);
  }

  createConnection(connection: JiraConnection): Observable<JiraConnectionResponse> {
    return this.http.post<JiraConnectionResponse>(this.apiUrl, connection);
  }

  updateConnection(id: number, connection: JiraConnection): Observable<JiraConnectionResponse> {
    return this.http.put<JiraConnectionResponse>(`${this.apiUrl}/${id}`, connection);
  }

  deleteConnection(id: number): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.apiUrl}/${id}`);
  }

  testConnection(id: number): Observable<JiraConnectionResponse> {
    return this.http.post<JiraConnectionResponse>(`${this.apiUrl}/${id}/test`, {});
  }
}
