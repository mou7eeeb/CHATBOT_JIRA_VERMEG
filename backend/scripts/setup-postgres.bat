@echo off
REM PostgreSQL Setup Script for Windows
REM This script helps set up the PostgreSQL database for AI Jira Chatbot

echo ========================================
echo PostgreSQL Setup for AI Jira Chatbot
echo ========================================
echo.

REM Check if PostgreSQL is installed
where psql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: PostgreSQL is not installed or not in PATH
    echo Please install PostgreSQL from https://www.postgresql.org/download/windows/
    pause
    exit /b 1
)

echo PostgreSQL found!
echo.

REM Prompt for PostgreSQL password
set /p PGPASSWORD="Enter PostgreSQL postgres user password: "
echo.

REM Create database
echo Creating database 'jirachatbot'...
psql -U postgres -c "CREATE DATABASE jirachatbot;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Database created successfully!
) else (
    echo Database already exists or creation failed. Continuing...
)
echo.

REM Run initialization script
echo Running initialization script...
psql -U postgres -d jirachatbot -f ..\src\main\resources\db\init.sql
if %ERRORLEVEL% EQU 0 (
    echo Initialization completed successfully!
) else (
    echo ERROR: Initialization failed!
    pause
    exit /b 1
)
echo.

REM Create .env file if it doesn't exist
if not exist ..\.env (
    echo Creating .env file...
    copy ..\.env.example ..\.env
    echo.
    echo IMPORTANT: Please edit backend\.env file with your PostgreSQL credentials!
    echo.
) else (
    echo .env file already exists.
    echo.
)

echo ========================================
echo Setup completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Edit backend\.env with your PostgreSQL credentials
echo 2. Run: cd backend
echo 3. Run: mvn clean install
echo 4. Run: mvn spring-boot:run
echo.
pause
