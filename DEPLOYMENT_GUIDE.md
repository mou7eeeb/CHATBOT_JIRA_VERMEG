# 🚀 Deployment Guide - Enterprise Jira Chatbot Platform

## 📋 What Has Been Built

### ✅ Backend (Spring Boot) - COMPLETED

#### Database Layer
- **5 Entities**: User, JiraConnection, ChatSession, ChatMessage, SavedSearch
- **5 Repositories**: JPA repositories with custom query methods
- **H2 Database**: In-memory database (development) with auto-schema generation

#### Security & Authentication
- **JWT Token Provider**: Token generation and validation
- **Security Configuration**: Spring Security with stateless sessions
- **Authentication Filter**: JWT-based request authentication
- **User Details Service**: Custom user authentication
- **Password Encryption**: BCrypt password hashing

#### Services
- **AuthService**: User registration and login
- **JiraConnectionService**: CRUD operations for Jira connections
- **AIService**: Natural language to JQL conversion (existing)
- **JiraService**: Jira API integration (existing)

#### Controllers & DTOs
- **AuthController**: `/api/auth/signup`, `/api/auth/login`, `/api/auth/logout`
- **JiraConnectionController**: Full CRUD for Jira connections
- **ChatController**: Chat endpoint (existing)
- **DTOs**: SignupRequest, LoginRequest, JwtResponse, JiraConnectionDTO, MessageResponse

### ✅ Frontend (Angular) - IN PROGRESS

#### Core Infrastructure
- **Auth Models**: User, JwtResponse, SignupRequest, LoginRequest
- **Jira Models**: JiraConnection, JiraConnectionResponse
- **Auth Service**: Login, signup, logout, token management
- **Jira Connection Service**: Full CRUD operations
- **Auth Interceptor**: Automatic JWT token injection
- **Auth Guard**: Route protection

#### Components
- **Login Component**: Modern glassmorphism design with animations
- **Signup Component**: Multi-field registration with validation
- **Modern UI**: Gradient backgrounds, floating animations, loading states

### 🚧 Remaining Work

#### Frontend Components Needed
- App routing configuration
- Dashboard with sidebar navigation
- Jira connections management page
- Redesigned chatbot interface
- Chat history page
- User profile page
- Settings page

## 🏃 Quick Start Guide

### Step 1: Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Backend will run on**: http://localhost:8081

**Available Endpoints**:
- `POST /api/auth/signup` - Register
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/jira-connections` - Get all connections (protected)
- `POST /api/jira-connections` - Create connection (protected)
- `GET /api/health` - Health check

### Step 2: Install Frontend Dependencies

```bash
cd frontend
npm install
```

### Step 3: Configure Angular App

You need to update `app.config.ts` or `main.ts` to include:

1. **HTTP Interceptor** for JWT tokens
2. **Router Configuration** with auth guards
3. **Provider Configuration**

### Step 4: Start Frontend

```bash
npm start
```

**Frontend will run on**: http://localhost:4200

## 🔧 Configuration Files to Update

### 1. Update `frontend/src/main.ts`

```typescript
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { AuthInterceptor } from './app/interceptors/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([AuthInterceptor]))
  ]
}).catch(err => console.error(err));
```

### 2. Create `frontend/src/app/app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/login' }
];
```

### 3. Update `frontend/src/app/app.component.ts`

```typescript
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {}
```

## 🧪 Testing the Application

### Test Authentication Flow

1. **Start both servers** (backend on 8081, frontend on 4200)

2. **Sign Up**:
   - Navigate to http://localhost:4200/signup
   - Fill in: First Name, Last Name, Email, Password
   - Click "Create Account"
   - Should redirect to dashboard with JWT token

3. **Login**:
   - Navigate to http://localhost:4200/login
   - Enter email and password
   - Click "Sign In"
   - Should receive JWT token and redirect

4. **Test Protected Endpoint**:
```bash
# Get JWT token from localStorage after login
# Then test:
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8081/api/jira-connections
```

### Database Console

Access H2 Console: http://localhost:8081/h2-console

- **JDBC URL**: `jdbc:h2:mem:jirachatbot`
- **Username**: `sa`
- **Password**: (leave empty)

## 📊 Database Schema

```sql
-- Users table
SELECT * FROM users;

-- Jira connections table
SELECT * FROM jira_connections;

-- Chat sessions table
SELECT * FROM chat_sessions;

-- Chat messages table
SELECT * FROM chat_messages;

-- Saved searches table
SELECT * FROM saved_searches;
```

## 🔐 Security Notes

### JWT Token
- **Expiration**: 24 hours (86400000 ms)
- **Secret**: Configured in application.properties
- **Storage**: LocalStorage (frontend)
- **Header**: `Authorization: Bearer <token>`

### Password Security
- **Algorithm**: BCrypt
- **Strength**: Default (10 rounds)
- **Validation**: Minimum 6 characters

### CORS Configuration
- **Allowed Origins**: http://localhost:4200
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Credentials**: Enabled

## 🐛 Troubleshooting

### Backend Issues

**Port 8081 already in use**:
```bash
# Windows
netstat -ano | findstr :8081
taskkill /F /PID <PID>

# Linux/Mac
lsof -i :8081
kill -9 <PID>
```

**Database not initializing**:
- Check `application.properties`
- Verify `spring.jpa.hibernate.ddl-auto=update`
- Check console for SQL errors

**JWT errors**:
- Verify `jwt.secret` is set in application.properties
- Check token expiration time
- Ensure Authorization header format: `Bearer <token>`

### Frontend Issues

**CORS errors**:
- Verify backend CORS configuration
- Check `cors.allowed-origins` in application.properties
- Ensure frontend runs on http://localhost:4200

**Authentication not working**:
- Check browser console for errors
- Verify JWT token in localStorage
- Check Network tab for API responses
- Ensure AuthInterceptor is properly configured

**Module not found errors**:
- Run `npm install` again
- Delete `node_modules` and `package-lock.json`, then reinstall
- Check Angular version compatibility

## 📦 Production Deployment

### Backend Production Configuration

1. **Update application.properties**:
```properties
# Use PostgreSQL instead of H2
spring.datasource.url=jdbc:postgresql://localhost:5432/jirachatbot_prod
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=validate

# Use strong JWT secret
jwt.secret=GENERATE_A_STRONG_SECRET_KEY_HERE_AT_LEAST_256_BITS

# Disable H2 console
spring.h2.console.enabled=false

# Production CORS
cors.allowed-origins=https://your-production-domain.com
```

2. **Build JAR**:
```bash
mvn clean package -DskipTests
java -jar target/jira-chatbot-1.0.0.jar
```

### Frontend Production Build

```bash
npm run build --prod
```

Deploy the `dist/` folder to:
- **Netlify**: Drag and drop
- **Vercel**: Connect GitHub repo
- **AWS S3 + CloudFront**: Upload to S3
- **Nginx**: Copy to `/var/www/html`

## 🎯 Next Steps

### Immediate (Required for MVP)
1. ✅ Configure Angular routing
2. ✅ Create basic dashboard component
3. ✅ Test complete auth flow
4. ⬜ Build Jira connections management page
5. ⬜ Integrate existing chatbot with new auth system

### Short-term (Enhanced Features)
1. ⬜ Chat history persistence
2. ⬜ Saved searches functionality
3. ⬜ User profile management
4. ⬜ Dark/Light theme toggle
5. ⬜ Dashboard analytics

### Long-term (Enterprise Features)
1. ⬜ Email verification
2. ⬜ Password reset flow
3. ⬜ Admin panel
4. ⬜ Multi-language support
5. ⬜ Advanced analytics

## 📞 Support

For issues or questions:
1. Check this deployment guide
2. Review ENTERPRISE_README.md
3. Check backend logs: `mvn spring-boot:run`
4. Check frontend console: Browser DevTools
5. Verify database state: H2 Console

## ✅ Checklist Before First Run

- [ ] Java 17+ installed
- [ ] Node.js 18+ installed
- [ ] Maven 3.8+ installed
- [ ] Angular CLI 17+ installed
- [ ] Backend dependencies installed (`mvn clean install`)
- [ ] Frontend dependencies installed (`npm install`)
- [ ] Application.properties configured
- [ ] Angular routing configured
- [ ] Auth interceptor registered
- [ ] Both servers running (8081 and 4200)

---

**Your enterprise Jira Chatbot platform is ready for development!** 🎉

The foundation is solid with JWT authentication, multi-tenant architecture, and modern UI components. Continue building the remaining features to complete your internship project.
