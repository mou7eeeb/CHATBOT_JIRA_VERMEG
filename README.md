# Jira Chatbot - Intelligent Ticket Search

A full-stack web application that allows users to search Jira tickets using natural language. The system uses AI to convert user questions into JQL (Jira Query Language), fetches data from Jira, and displays results in a modern chat interface.

## 🎯 Features

- **Natural Language Processing**: Ask questions in plain English
- **AI-Powered JQL Generation**: Converts natural language to JQL using OpenAI (with rule-based fallback)
- **Real-time Jira Integration**: Fetches tickets directly from Jira Cloud
- **Modern Chat Interface**: Beautiful, responsive UI with chat history
- **Ticket Filtering**: Filter results by status
- **Loading Indicators**: Visual feedback during searches
- **Error Handling**: Graceful error messages and recovery

## 🏗️ Architecture

### Backend (Spring Boot)
- **Java 17+**
- **Spring Boot 3.2.0**
- **Lombok** for cleaner code
- **REST API** with `/api/chat` endpoint

### Frontend (Angular)
- **Angular 17**
- **Standalone Components**
- **RxJS** for reactive programming
- **Modern CSS** with gradients and animations

## 📋 Prerequisites

### Backend
- Java 17 or higher
- Maven 3.6+
- Jira Cloud account with API access

### Frontend
- Node.js 18+ and npm
- Angular CLI 17+

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

### 2. Configure OpenAI (Optional)

If you want to use AI-powered JQL generation:

1. Get an OpenAI API key from [OpenAI Platform](https://platform.openai.com/api-keys)
2. Edit `backend/src/main/resources/application.properties`:

```properties
openai.api.key=your-openai-api-key-here
```

**Note**: If OpenAI is not configured, the system will automatically use a rule-based fallback system.

### 3. Start the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 4. Start the Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

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

### OpenAI Integration
When configured, the system uses GPT-3.5-turbo to convert natural language to JQL with high accuracy.

### Rule-Based Fallback
If OpenAI is not available, the system uses pattern matching to generate JQL:
- Detects project names
- Identifies issue types (bug, story, task)
- Recognizes status keywords (open, closed, in progress)
- Extracts priority levels
- Finds assignee information

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
│   │   │   ├── JiraConfig.java
│   │   │   └── OpenAIConfig.java
│   │   ├── controller/
│   │   │   └── ChatController.java
│   │   ├── model/
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   └── JiraTicket.java
│   │   └── service/
│   │       ├── AIService.java
│   │       ├── ChatService.java
│   │       └── JiraService.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── models/
│   │   │   │   ├── chat.model.ts
│   │   │   │   └── jira-ticket.model.ts
│   │   │   ├── services/
│   │   │   │   └── chat.service.ts
│   │   │   ├── app.component.ts
│   │   │   ├── app.component.html
│   │   │   └── app.component.css
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.css
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
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
- OpenAI API
