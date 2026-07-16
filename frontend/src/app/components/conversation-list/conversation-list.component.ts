import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ChatSessionService } from '../../services/chat-session.service';
import { ChatSession } from '../../models/chat-session.model';
import { MessageResponse } from '../../models/auth.model';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './conversation-list.component.html',
  styleUrls: ['./conversation-list.component.css']
})
export class ConversationListComponent implements OnInit {
  conversations: ChatSession[] = [];
  loading = true;
  error = '';

  constructor(private chatSessionService: ChatSessionService) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.loading = true;
    this.chatSessionService.getAllSessions().subscribe({
      next: (conversations) => {
        this.conversations = conversations;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        this.error = 'Failed to load conversations';
        this.loading = false;
      }
    });
  }

  deleteConversation(id: number): void {
    if (id && confirm('Are you sure you want to delete this conversation? This action is permanent.')) {
      this.chatSessionService.deleteSession(id).subscribe({
        next: () => {
          // Remove from local list only after successful backend deletion
          this.conversations = this.conversations.filter(conv => conv.id !== id);
          this.error = '';
        },
        error: (error) => {
          console.error('Unable to delete conversation:', error);
          this.error = error.error?.message || 'Failed to delete conversation';
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
          this.error = error.error?.message || 'Failed to rename conversation';
        }
      });
    }
  }
}
