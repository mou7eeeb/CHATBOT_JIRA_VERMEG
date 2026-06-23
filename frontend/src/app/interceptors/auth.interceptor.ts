import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Ne pas ajouter le token aux requêtes d'authentification
    if (request.url.includes('/api/auth/')) {
      console.log('AuthInterceptor - Skipping auth endpoints:', request.url);
      return next.handle(request);
    }
    
    // Récupérer le token directement depuis localStorage
    const token = localStorage.getItem('token');
    
    console.log('AuthInterceptor - URL:', request.url);
    console.log('AuthInterceptor - Token:', token ? 'EXISTS' : 'NULL');
    
    if (token) {
      const clonedRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('AuthInterceptor - Authorization header added');
      return next.handle(clonedRequest);
    }
    
    console.log('AuthInterceptor - No token, request sent without Authorization');
    return next.handle(request);
  }
}
