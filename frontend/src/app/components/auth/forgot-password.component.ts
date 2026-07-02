import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email = '';
  step: 'email' | 'code' | 'reset' | 'success' = 'email';
  verificationCode = '';
  newPassword = '';
  confirmPassword = '';
  errorMessage = '';
  successMessage = '';
  isLoading = false;
  receivedCode = '';

  constructor(
    private router: Router,
    private http: HttpClient
  ) {}

  onSendCode() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.email) {
      this.errorMessage = 'Veuillez entrer votre adresse email';
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Adresse email invalide';
      return;
    }

    this.isLoading = true;

    // Appel API pour envoyer le code de vérification
    this.http.post('http://localhost:8081/api/auth/send-verification-code', { email: this.email })
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.receivedCode = response.code; // Pour la démo, on stocke le code
          this.successMessage = `Un code de vérification a été envoyé à votre email. Code: ${response.code}`;
          this.step = 'code';
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Erreur lors de l\'envoi du code';
        }
      });
  }

  onVerifyCode() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.verificationCode) {
      this.errorMessage = 'Veuillez entrer le code de vérification';
      return;
    }

    if (this.verificationCode.length !== 6) {
      this.errorMessage = 'Le code doit contenir 6 chiffres';
      return;
    }

    this.isLoading = true;

    // Appel API pour vérifier le code
    this.http.post('http://localhost:8081/api/auth/verify-code', { 
      email: this.email, 
      code: this.verificationCode 
    })
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.successMessage = 'Code vérifié avec succès';
          this.step = 'reset';
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Code de vérification invalide';
        }
      });
  }

  onResetPassword() {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    if (this.newPassword.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.isLoading = true;

    // Appel API pour réinitialiser le mot de passe
    this.http.post('http://localhost:8081/api/auth/reset-password', {
      email: this.email,
      verificationCode: this.verificationCode,
      newPassword: this.newPassword
    })
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.step = 'success';
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Erreur lors de la réinitialisation du mot de passe';
        }
      });
  }

  goToLogin() {
    this.router.navigate(['/auth']);
  }

  resendCode() {
    this.errorMessage = '';
    this.isLoading = true;

    this.http.post('http://localhost:8081/api/auth/send-verification-code', { email: this.email })
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.receivedCode = response.code;
          this.successMessage = `Un nouveau code a été envoyé à votre email. Code: ${response.code}`;
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Erreur lors de l\'envoi du code';
        }
      });
  }
}
