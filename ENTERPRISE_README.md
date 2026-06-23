# 🚀 Enterprise AI Jira Chatbot Platform

A modern, multi-tenant SaaS platform for intelligent Jira ticket management using AI-powered natural language processing.

## 🎯 Project Overview

This is a complete enterprise-grade application that transforms Jira ticket management through an AI-powered chatbot interface. Built with Spring Boot (backend) and Angular (frontend), it features JWT authentication, multi-tenant architecture, and a modern UI/UX.

## ✨ Key Features

### 🔐 Authentication & Security
- JWT-based authentication
- Secure signup/login/logout
- Password encryption (BCrypt)
- Protected routes and API endpoints
- Session management

### 👥 Multi-Tenant Architecture
- Individual user accounts with roles (USER, ADMIN)
- Isolated data per user
- Personal Jira connections
- Private chat history
- User-specific settings

### 🔗 Jira Connection Management
- Multiple Jira account support per user
- Secure credential storage
- Connection testing
- Default connection selection
- CRUD operations for connections

### 🤖 AI-Powered Chatbot
- Natural language to JQL conversion
- OpenAI integration (optional)
- Rule-based fallback system
- Chat session management
- Message history persistence

### 📊 Database Schema
- **users**: User accounts and authentication
- **jira_connections**: User-specific Jira credentials
- **chat_sessions**: Conversation sessions
- **chat_messages**: Individual messages
- **saved_searches**: Saved JQL queries

## 🏗️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security + JWT
- **Database**: H2 (development) / PostgreSQL (production)
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **Build Tool**: Maven

### Frontend
- **Framework**: Angular 17
- **UI Components**: Modern custom components
- **Styling**: TailwindCSS (planned)
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router with Guards

## 📁 Project Structure

```
CHATBOT_JIRA_VERMEG/
├── backend/
│   ├── src/main/java/com/vermeg/jirachatbot/
│   │   ├── entity/              # Database entities
│   │   │   ├── User.java
│   │   │   ├── JiraConnection.java
│   │   │   ├── ChatSession.java
│   │   │   ├── ChatMessage.java
│   │   │   └── SavedSearch.java
│   │   ├── repository/          # JPA repositories
│   │   ├── service/             # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── JiraConnectionService.java
│   │   │   ├── AIService.java
│   │   │   └── JiraService.java
│   │   ├── controller/          # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── JiraConnectionController.java
│   │   │   └── ChatController.java
│   │   ├── security/            # Security configuration
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── UserPrincipal.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   └── config/              # Configuration classes
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   └── src/
│       ├── app/
│       │   ├── auth/            # Authentication module
│       │   ├── dashboard/       # Dashboard components
│       │   ├── jira-connections/# Jira management
│       │   ├── chatbot/         # AI chatbot interface
│       │   ├── guards/          # Route guards
│       │   └── services/        # API services
│       └── assets/
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+
- Angular CLI 17+

### Backend Setup

1. **Navigate to backend directory**:
```bash
cd backend
```

2. **Install dependencies**:
```bash
mvn clean install
```

3. **Configure application.properties** (optional):
```properties
# Database (H2 by default)
spring.datasource.url=jdbc:h2:mem:jirachatbot

# JWT Configuration
jwt.secret=VermegJiraChatbotSecretKeyForJWTTokenGenerationAndValidation2024
jwt.expiration=86400000

# OpenAI (optional)
openai.api.key=your-openai-api-key-here
```

4. **Run the backend**:
```bash
mvn spring-boot:run
```

Backend will start on: **http://localhost:8081**

### Frontend Setup

1. **Navigate to frontend directory**:
```bash
cd frontend
```

2. **Install dependencies**:
```bash
npm install
```

3. **Start the development server**:
```bash
npm start
```

Frontend will start on: **http://localhost:4200**

## 📡 API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Jira Connections
- `GET /api/jira-connections` - Get all user connections
- `POST /api/jira-connections` - Create new connection
- `GET /api/jira-connections/{id}` - Get connection by ID
- `PUT /api/jira-connections/{id}` - Update connection
- `DELETE /api/jira-connections/{id}` - Delete connection
- `POST /api/jira-connections/{id}/test` - Test connection

### Chat (Protected)
- `POST /api/chat` - Send message to chatbot
- `GET /api/health` - Health check

## 🔑 Authentication Flow

1. **Signup**: User creates account → Receives JWT token
2. **Login**: User authenticates → Receives JWT token
3. **API Requests**: Include token in header: `Authorization: Bearer <token>`
4. **Protected Routes**: Token validated on each request

## 💾 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);
```

### Jira Connections Table
```sql
CREATE TABLE jira_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    connection_name VARCHAR(255) NOT NULL,
    jira_base_url VARCHAR(255) NOT NULL,
    jira_email VARCHAR(255) NOT NULL,
    jira_api_token VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    last_tested_at TIMESTAMP,
    last_test_success BOOLEAN,
    last_test_message TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 🎨 UI Features (Planned)

- Modern dashboard with analytics
- Sidebar navigation
- Dark/Light theme toggle
- Responsive design
- Glassmorphism effects
- Beautiful gradients
- Loading indicators
- Error handling
- Toast notifications

## 🔒 Security Features

- Password encryption (BCrypt)
- JWT token-based authentication
- Secure HTTP-only cookies (optional)
- CORS configuration
- SQL injection prevention (JPA)
- XSS protection
- CSRF protection

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📦 Deployment

### Backend (Production)

1. **Update application.properties for production**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jirachatbot
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

2. **Build JAR**:
```bash
mvn clean package -DskipTests
```

3. **Run**:
```bash
java -jar target/jira-chatbot-1.0.0.jar
```

### Frontend (Production)

1. **Build for production**:
```bash
npm run build --prod
```

2. **Deploy dist folder** to your web server

## 🛠️ Development Status

### ✅ Completed
- Database schema and entities
- JWT authentication system
- User registration and login
- Jira connection management
- Security configuration
- Repository layer
- Service layer
- Controller layer
- DTOs and validation

### 🚧 In Progress
- Angular authentication module
- Modern UI components
- Dashboard implementation
- Chat interface redesign

### 📋 Planned
- Chat history page
- Saved searches feature
- Analytics dashboard
- User profile management
- Dark/Light theme
- Email verification
- Password reset
- Admin panel

## 🤝 Contributing

This is an internship project for Vermeg. For questions or issues, contact the development team.

## 📄 License

Proprietary - Vermeg Internal Use Only

## 👨‍💻 Author

Developed as part of an internship project at Vermeg

## 🙏 Acknowledgments

- Spring Boot Team
- Angular Team
- Jira API Documentation
- OpenAI API

---

**Note**: This is an enterprise-grade application designed for production use within Vermeg. Ensure all credentials and API keys are properly secured before deployment.
