# Jira Chatbot - Intelligent Ticket Search

A full-stack web application that allows users to search Jira tickets using natural language. The system uses AI to convert user questions into JQL (Jira Query Language), fetches data from Jira, and displays results in a modern chat interface.

## 🎯 Features

- **Natural Language Processing**: Ask questions in plain English
- **AI-Powered JQL Generation**: Converts natural language to JQL using Google AI (Gemini) with demo mode fallback
- **Real-time Jira Integration**: Fetches tickets directly from Jira Cloud
- **Modern Chat Interface**: Beautiful, responsive UI with chat history
- **User Authentication**: JWT-based authentication with email verification
- **Email Service**: Gmail SMTP integration for verification codes and password reset
- **Docker Support**: Containerized deployment with docker-compose
- **Ticket Filtering**: Filter results by status
- **Loading Indicators**: Visual feedback during searches
- **Error Handling**: Graceful error messages and recovery

## 🏗️ Architecture

### Backend (Spring Boot)
- **Java 17+**
- **Spring Boot 3.2.0**
- **Lombok** for cleaner code
- **Spring Security** with JWT authentication
- **Spring Mail** for email sending
- **Google AI (Gemini)** for AI-powered responses
- **H2 Database** for development
- **PostgreSQL** support for production
- **REST API** with `/api/chat` endpoint

### Frontend (Angular)
- **Angular 17**
- **Standalone Components**
- **RxJS** for reactive programming
- **Modern CSS** with gradients and animations
- **JWT Authentication**

## 📋 Prerequisites

### Backend
- Java 17 or higher
- Maven 3.6+
- Jira Cloud account with API access
- Google AI API key (optional, for AI features)
- Gmail account with App Password (for email features)

### Frontend
- Node.js 18+ and npm
- Angular CLI 17+

### Docker (Optional)
- Docker Desktop
- Docker Compose

## 🚀 Setup Instructions

### 1. Configure Jira Credentials

1. Go to [Atlassian Account Settings](https://id.atlassian.com/manage-profile/security/api-tokens)
2. Create an API token
3. Edit `backend/src/main/resources/application.properties`:

```properties
jira.domain=https://your-domain.atlassian.net
jira.email=your-email@example.com
jira.api-token=your-api-token-here
```

### 2. Configure Google AI (Optional)

If you want to use AI-powered responses:

1. Get a Google AI API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Edit `backend/src/main/resources/application.properties`:

```properties
google.ai.key=your-google-ai-api-key-here
google.ai.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
google.ai.model=gemini-pro
```

**Note**: If Google AI is not configured, the system will automatically use demo mode.

### 3. Configure Email Service (Optional)

For email verification and password reset:

1. Enable 2FA on your Gmail account
2. Generate an App Password from [Google Account Security](https://myaccount.google.com/security)
3. Edit `backend/src/main/resources/application.properties`:

```properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-password
app.email.enabled=true
```

### 4. Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8081`

### 5. Start the Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

### 6. Docker Deployment (Alternative)

Using Docker Compose:

```bash
docker-compose up --build
```

This will start both frontend (port 80) and backend (port 8081) in containers.

## 🎮 Usage

1. Open your browser to `http://localhost:4200`
2. Type natural language queries in the chat input, such as:
   - "show me open bugs in CRM"
   - "all high priority issues"
   - "issues assigned to john"
   - "my open tasks"
3. View the results with ticket details, status, and priority
4. Filter tickets by status using the dropdown
5. Clear chat history with the trash icon

## 📡 API Endpoints

### POST /api/chat
Search Jira tickets using natural language.

**Request:**
```json
{
  "message": "show me open bugs in CRM",
  "sessionId": "optional-session-id"
}
```

**Response:**
```json
{
  "message": "Found 5 tickets matching your query.",
  "jqlQuery": "project = CRM AND issuetype = Bug AND status != Done",
  "tickets": [...],
  "totalTickets": 5,
  "success": true
}
```

### GET /api/health
Health check endpoint.

## 🤖 AI Features

### Google AI (Gemini) Integration
When configured, the system uses Google AI (Gemini) for intelligent responses with high accuracy.

### Demo Mode Fallback
If Google AI is not configured or quota is exceeded, the system uses demo mode:
- Provides intelligent responses based on pattern matching
- Graceful fallback when AI is unavailable
- No service interruption

## 🎨 UI Features

- **Gradient Design**: Modern purple gradient theme
- **Responsive Layout**: Works on desktop and mobile
- **Smooth Animations**: Fade-in effects and transitions
- **Loading States**: Animated dots during searches
- **Status Colors**: Color-coded ticket statuses
- **Priority Indicators**: Visual priority levels
- **Chat History**: Persistent conversation view

## 🛠️ Project Structure

```
CHATBOT_JIRA_VERMEG/
├── backend/
│   ├── src/main/java/com/vermeg/jirachatbot/
│   │   ├── JiraChatbotApplication.java
│   │   ├── config/
│   │   │   ├── CorsConfig.java
│   │   │   ├── GoogleAIConfig.java
│   │   │   └── JiraConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── ChatController.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   └── PasswordResetRequest.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── JiraConnection.java
│   │   │   ├── SavedSearch.java
│   │   │   └── ChatSession.java
│   │   ├── model/
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   └── JiraTicket.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── service/
│   │       ├── AIService.java
│   │       ├── AuthService.java
│   │       ├── ChatService.java
│   │       ├── EmailService.java
│   │       ├── IntelligentChatService.java
│   │       └── JiraService.java
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/
│   │       └── init.sql
│   ├── Dockerfile
│   ├── .dockerignore
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   │   ├── auth/
│   │   │   │   ├── chatbot/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── jira-connections/
│   │   │   │   └── shared/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   └── ...
│   │   ├── index.html
│   │   └── ...
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── .dockerignore
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
├── docker-compose.yml
└── README.md
```

## 🔒 Security Notes

- Never commit API tokens to version control
- Use environment variables for production deployments
- Keep your Jira API token secure
- Implement authentication for production use

## 🐛 Troubleshooting

### Backend won't start
- Verify Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`
- Ensure port 8080 is available

### Frontend won't start
- Verify Node.js is installed: `node -v`
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`

### CORS errors
- Verify backend is running on port 8080
- Check CORS configuration in `application.properties`

### Jira connection fails
- Verify your Jira domain is correct
- Check API token is valid
- Ensure your Jira account has access to projects

### No tickets returned
- Verify JQL query is valid
- Check you have access to the projects
- Try simpler queries first

## 📝 Example Queries

- "show me open bugs in CRM"
- "all high priority issues"
- "issues assigned to john"
- "my open tasks"
- "closed stories in PROJECT"
- "bugs in progress"

## 🚀 Production Deployment

### Backend
1. Build the JAR: `mvn clean package`
2. Run: `java -jar target/jira-chatbot-1.0.0.jar`
3. Configure environment variables for credentials

### Frontend
1. Build for production: `ng build --configuration production`
2. Deploy the `dist/` folder to your web server
3. Update API URL in `chat.service.ts`

## 📄 License

This project is created for educational and internship purposes.

## 👥 Author

Created for VERMEG internship project.

## 🙏 Acknowledgments

- Spring Boot Framework
- Angular Framework
- Atlassian Jira API
- Google AI (Gemini) API
- Docker
