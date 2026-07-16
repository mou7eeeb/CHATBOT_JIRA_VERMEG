import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ChatRequest, ChatResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  sendMessage(message: string): Observable<ChatResponse> {
    const request: ChatRequest = {
      message: message
    };

    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  sendAIMessage(message: string): Observable<ChatResponse> {
    const request: ChatRequest = {
      message: message
    };

    return this.http.post<ChatResponse>(`${this.apiUrl}/chat/ai`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Try to extract error message from backend response
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server returned code ${error.status}: ${error.message}`;
      }
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
