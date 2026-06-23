import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Observable } from 'rxjs';
import { User } from '../../../models/auth.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  isAdmin: boolean = false;
  currentUser$: Observable<User | null>;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.currentUser$ = this.authService.currentUser;
  }

  ngOnInit(): void {
    // Vérifier immédiatement le rôle au chargement
    const currentUser = this.authService.currentUserValue;
    console.log('🔍 Current user on init:', currentUser);
    this.isAdmin = currentUser?.role === 'ADMIN';
    console.log('🔐 Is Admin:', this.isAdmin);
    
    // S'abonner aux changements de l'utilisateur
    this.authService.currentUser.subscribe(user => {
      console.log('👤 User changed:', user);
      this.isAdmin = user?.role === 'ADMIN';
      console.log('🔐 Is Admin updated:', this.isAdmin);
      this.cdr.detectChanges(); // Forcer la détection de changement
    });
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }
}
