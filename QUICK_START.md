# 🚀 Quick Start Guide - Enterprise Jira Chatbot

## ✅ What's Ready

Your enterprise Jira Chatbot platform has been transformed with:
- **Backend**: JWT authentication, multi-tenant database, Jira connection management
- **Frontend**: Modern login/signup pages, dashboard with sidebar navigation
- **Security**: Protected routes, JWT tokens, password encryption

## 📦 Installation & Setup

### 1. Start Backend (Terminal 1)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

✅ Backend runs on: **http://localhost:8081**

### 2. Start Frontend (Terminal 2)

```bash
cd frontend
npm install
npm start
```

✅ Frontend runs on: **http://localhost:4200**

## 🎯 Test the Application

### Step 1: Create Account
1. Navigate to **http://localhost:4200**
2. You'll be redirected to login page
3. Click **"Sign up"** link
4. Fill in:
   - First Name: `John`
   - Last Name: `Doe`
   - Email: `john.doe@company.com`
   - Password: `password123`
5. Click **"Create Account"**
6. You'll be logged in automatically and redirected to dashboard

### Step 2: Explore Dashboard
- View your user info in top-right corner
- See stats cards (connections, sessions, searches)
- Navigate using sidebar:
  - 📊 Dashboard
  - 💬 AI Chatbot
  - 🔗 Jira Connections

### Step 3: Add Jira Connection
1. Click **"Add Jira Connection"** or navigate to Jira Connections
2. Fill in your Jira details:
   - Connection Name: `My Jira`
   - Jira Base URL: `https://yourcompany.atlassian.net`
   - Email: `your.email@company.com`
   - API Token: `your-jira-api-token`
3. Set as default (optional)
4. Save connection

### Step 4: Use AI Chatbot
1. Navigate to **AI Chatbot** from sidebar
2. Ask questions like:
   - "Show me open bugs"
   - "All high priority issues"
   - "Tickets assigned to me"

## 🔑 API Endpoints

### Public Endpoints
- `POST /api/auth/signup` - Create account
- `POST /api/auth/login` - Login
- `GET /api/health` - Health check

### Protected Endpoints (Require JWT Token)
- `GET /api/jira-connections` - List connections
- `POST /api/jira-connections` - Create connection
- `PUT /api/jira-connections/{id}` - Update connection
- `DELETE /api/jira-connections/{id}` - Delete connection
- `POST /api/jira-connections/{id}/test` - Test connection
- `POST /api/chat` - Send chat message

## 🗄️ Database Access

Access H2 Console: **http://localhost:8081/h2-console**

- **JDBC URL**: `jdbc:h2:mem:jirachatbot`
- **Username**: `sa`
- **Password**: (leave empty)

View tables:
```sql
SELECT * FROM users;
SELECT * FROM jira_connections;
SELECT * FROM chat_sessions;
SELECT * FROM chat_messages;
```

## 🔧 Configuration

### Backend (`application.properties`)
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:h2:mem:jirachatbot
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=VermegJiraChatbotSecretKeyForJWTTokenGenerationAndValidation2024
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:4200
```

### Frontend
- API URL: `http://localhost:8081/api`
- JWT stored in: `localStorage`
- Token header: `Authorization: Bearer <token>`

## 🎨 Features Implemented

### ✅ Authentication
- User registration with validation
- Login with JWT tokens
- Logout functionality
- Protected routes with guards
- Auto token injection in HTTP requests

### ✅ Dashboard
- Modern sidebar navigation
- User profile display
- Stats cards (connections, sessions)
- Quick actions
- Jira connections list
- Empty states

### ✅ Multi-Tenant
- Each user has isolated data
- User-specific Jira connections
- Personal chat history
- Individual settings

### ✅ Security
- BCrypt password encryption
- JWT token authentication
- HTTP-only sessions
- CORS protection
- Protected API endpoints

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check if port 8081 is free
netstat -ano | findstr :8081

# Kill process if needed
taskkill /F /PID <PID>
```

### Frontend errors
```bash
# Clear and reinstall
rm -rf node_modules package-lock.json
npm install
```

### CORS errors
- Verify backend is running on 8081
- Check `cors.allowed-origins` in application.properties
- Ensure frontend runs on 4200

### Authentication not working
- Check browser console for errors
- Verify JWT token in localStorage
- Check Network tab for API responses
- Ensure backend security is configured

## 📂 Project Structure

```
CHATBOT_JIRA_VERMEG/
├── backend/
│   ├── entity/          # Database entities
│   ├── repository/      # JPA repositories
│   ├── service/         # Business logic
│   ├── controller/      # REST endpoints
│   ├── security/        # JWT & Spring Security
│   └── dto/             # Data transfer objects
├── frontend/
│   ├── models/          # TypeScript models
│   ├── services/        # API services
│   ├── guards/          # Route guards
│   ├── interceptors/    # HTTP interceptors
│   └── components/
│       ├── login/       # Login page
│       ├── signup/      # Signup page
│       └── dashboard/   # Main dashboard
└── docs/
    ├── ENTERPRISE_README.md
    ├── DEPLOYMENT_GUIDE.md
    └── QUICK_START.md (this file)
```

## 🎯 Next Steps

### Immediate
1. ✅ Test authentication flow
2. ✅ Add your first Jira connection
3. ⬜ Create Jira connections management page
4. ⬜ Integrate chatbot with new auth system

### Short-term
5. ⬜ Build chat history page
6. ⬜ Add saved searches feature
7. ⬜ Implement user profile page
8. ⬜ Add dark/light theme toggle

### Long-term
9. ⬜ Email verification
10. ⬜ Password reset flow
11. ⬜ Admin panel
12. ⬜ Analytics dashboard

## 💡 Tips

- **JWT Token**: Valid for 24 hours
- **Default User**: Created on signup
- **Database**: In-memory (resets on restart)
- **Production**: Switch to PostgreSQL
- **Security**: Change JWT secret for production

## 📞 Need Help?

1. Check `DEPLOYMENT_GUIDE.md` for detailed setup
2. Review `ENTERPRISE_README.md` for architecture
3. Check browser console for errors
4. Verify backend logs in terminal
5. Test API endpoints with curl/Postman

---

**Your enterprise platform is ready!** 🎉

Start by creating an account at http://localhost:4200
