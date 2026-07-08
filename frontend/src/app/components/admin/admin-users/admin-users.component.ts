import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, User } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading = true;
  error: string | null = null;
  searchTerm = '';
  
  showModal = false;
  modalMode: 'create' | 'edit' = 'create';
  currentUser: User = this.getEmptyUser();
  
  showDeleteConfirm = false;
  userToDelete: User | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  get totalUsersCount(): number {
    return this.users.length;
  }

  get activeUsersCount(): number {
    return this.users.filter(u => u.enabled).length;
  }

  get adminUsersCount(): number {
    return this.users.filter(u => u.role === 'ADMIN').length;
  }

  getUserInitials(user: User): string {
    const first = user.firstName?.[0] || '';
    const last = user.lastName?.[0] || '';
    return `${first}${last}`.toUpperCase();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = null;
    
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.filteredUsers = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load users';
        this.loading = false;
        console.error('Error loading users:', err);
      }
    });
  }

  filterUsers(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredUsers = this.users.filter(user =>
      user.email.toLowerCase().includes(term) ||
      user.firstName.toLowerCase().includes(term) ||
      user.lastName.toLowerCase().includes(term)
    );
  }

  openCreateModal(): void {
    this.modalMode = 'create';
    this.currentUser = this.getEmptyUser();
    this.showModal = true;
  }

  openEditModal(user: User): void {
    this.modalMode = 'edit';
    this.currentUser = { ...user };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.currentUser = this.getEmptyUser();
  }

  saveUser(): void {
    if (this.modalMode === 'create') {
      this.adminService.createUser(this.currentUser).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
        },
        error: (err) => {
          alert('Error creating user: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.adminService.updateUser(this.currentUser.id, this.currentUser).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
        },
        error: (err) => {
          alert('Error updating user: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  confirmDelete(user: User): void {
    this.userToDelete = user;
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.userToDelete = null;
    this.showDeleteConfirm = false;
  }

  deleteUser(): void {
    if (this.userToDelete) {
      this.adminService.deleteUser(this.userToDelete.id).subscribe({
        next: () => {
          this.loadUsers();
          this.cancelDelete();
        },
        error: (err) => {
          alert('Error deleting user: ' + (err.error?.message || err.message));
          this.cancelDelete();
        }
      });
    }
  }

  toggleUserStatus(user: User): void {
    this.adminService.toggleUserStatus(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (err) => {
        alert('Error toggling user status: ' + (err.error?.message || err.message));
      }
    });
  }

  changeUserRole(user: User, newRole: 'USER' | 'ADMIN'): void {
    this.adminService.changeUserRole(user.id, newRole).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (err) => {
        alert('Error changing user role: ' + (err.error?.message || err.message));
      }
    });
  }

  private getEmptyUser(): User {
    return {
      id: 0,
      email: '',
      firstName: '',
      lastName: '',
      role: 'USER',
      enabled: true,
      createdAt: '',
      updatedAt: '',
      password: ''
    };
  }
}
