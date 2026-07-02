import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-auth-switch',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './auth-switch.component.html',
  styleUrls: ['./auth-switch.component.css']
})
export class AuthSwitchComponent {
  isSignIn = true;
  
  // Sign In form
  signInEmail = '';
  signInPassword = '';
  
  // Sign Up form
  signUpFirstName = '';
  signUpLastName = '';
  signUpEmail = '';
  signUpPassword = '';
  signUpConfirmPassword = '';
  
  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleMode() {
    this.isSignIn = !this.isSignIn;
    this.clearMessages();
    this.resetForms();
  }

  clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
  }

  resetForms() {
    this.signInEmail = '';
    this.signInPassword = '';
    this.signUpFirstName = '';
    this.signUpLastName = '';
    this.signUpEmail = '';
    this.signUpPassword = '';
    this.signUpConfirmPassword = '';
  }

  onSignIn() {
    this.clearMessages();
    
    if (!this.signInEmail || !this.signInPassword) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    this.isLoading = true;
    this.authService.login({ email: this.signInEmail, password: this.signInPassword })
      .subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Invalid email or password';
        }
      });
  }

  onSignUp() {
    this.clearMessages();
    
    if (!this.signUpFirstName || !this.signUpLastName || !this.signUpEmail || 
        !this.signUpPassword || !this.signUpConfirmPassword) {
      this.errorMessage = 'Please fill in all fields';
      return;
    }

    if (this.signUpPassword !== this.signUpConfirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    if (this.signUpPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters';
      return;
    }

    this.isLoading = true;
    this.authService.signup({
      firstName: this.signUpFirstName,
      lastName: this.signUpLastName,
      email: this.signUpEmail,
      password: this.signUpPassword
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Account created successfully! Please sign in.';
        setTimeout(() => {
          this.isSignIn = true;
          this.signInEmail = this.signUpEmail;
          this.resetForms();
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to create account';
      }
    });
  }
}
