import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

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

  constructor(private router: Router) {}

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

    // Simulation d'envoi de code (à remplacer par un vrai appel API)
    setTimeout(() => {
      this.isLoading = false;
      this.successMessage = 'Un code de vérification a été envoyé à votre email';
      this.step = 'code';
    }, 1500);
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

    // Simulation de vérification (à remplacer par un vrai appel API)
    setTimeout(() => {
      this.isLoading = false;
      // Pour la démo, on accepte n'importe quel code
      this.successMessage = 'Code vérifié avec succès';
      this.step = 'reset';
    }, 1000);
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

    // Simulation de réinitialisation (à remplacer par un vrai appel API)
    setTimeout(() => {
      this.isLoading = false;
      this.step = 'success';
    }, 1500);
  }

  goToLogin() {
    this.router.navigate(['/auth']);
  }

  resendCode() {
    this.errorMessage = '';
    this.isLoading = true;

    setTimeout(() => {
      this.isLoading = false;
      this.successMessage = 'Un nouveau code a été envoyé à votre email';
    }, 1000);
  }
}
