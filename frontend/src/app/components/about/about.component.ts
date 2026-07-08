import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent {
  author = {
    name: 'Mouheb Sayadi',
    phone: '24025287',
    position: 'Stagiaire',
    company: 'Vermeg',
    education: 'Étudiant en 3ème année Cycle Ingénieur',
    specialization: 'Intelligence Artificielle et Data Science',
    github: 'https://github.com/mou7eeeb'
  };

  features = [
    {
      icon: 'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z',
      title: 'Intelligence Artificielle',
      description: 'Utilisation de l\'IA pour comprendre et traiter les requêtes en langage naturel.'
    },
    {
      icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5',
      title: 'Intégration Jira',
      description: 'Connexion directe avec vos instances Jira Cloud pour un accès instantané à vos tickets.'
    },
    {
      icon: 'M13 10V3L4 14h7v7l9-11h-7z',
      title: 'Recherche Rapide',
      description: 'Génération automatique de requêtes JQL optimisées pour des résultats ultra-rapides.'
    },
    {
      icon: 'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 10-8 0v4h8z',
      title: 'Sécurité Enterprise',
      description: 'Authentification JWT et chiffrement des données pour une sécurité maximale.'
    }
  ];

  technologies = [
    { name: 'Angular 17', category: 'Frontend' },
    { name: 'TypeScript', category: 'Frontend' },
    { name: 'Tailwind CSS', category: 'Frontend' },
    { name: 'Spring Boot', category: 'Backend' },
    { name: 'Java 21', category: 'Backend' },
    { name: 'JWT', category: 'Security' },
    { name: 'H2 Database', category: 'Database' },
    { name: 'Jira REST API', category: 'Integration' }
  ];
}
