#!/bin/bash
# PostgreSQL Setup Script for Linux/macOS
# This script helps set up the PostgreSQL database for AI Jira Chatbot

echo "========================================"
echo "PostgreSQL Setup for AI Jira Chatbot"
echo "========================================"
echo ""

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo "ERROR: PostgreSQL is not installed or not in PATH"
    echo "Please install PostgreSQL:"
    echo "  macOS: brew install postgresql@15"
    echo "  Ubuntu/Debian: sudo apt install postgresql postgresql-contrib"
    exit 1
fi

echo "PostgreSQL found!"
echo ""

# Prompt for PostgreSQL password
read -sp "Enter PostgreSQL postgres user password: " PGPASSWORD
echo ""
export PGPASSWORD

# Create database
echo "Creating database 'jirachatbot'..."
psql -U postgres -c "CREATE DATABASE jirachatbot;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "Database created successfully!"
else
    echo "Database already exists or creation failed. Continuing..."
fi
echo ""

# Run initialization script
echo "Running initialization script..."
psql -U postgres -d jirachatbot -f ../src/main/resources/db/init.sql
if [ $? -eq 0 ]; then
    echo "Initialization completed successfully!"
else
    echo "ERROR: Initialization failed!"
    exit 1
fi
echo ""

# Create .env file if it doesn't exist
if [ ! -f ../.env ]; then
    echo "Creating .env file..."
    cp ../.env.example ../.env
    echo ""
    echo "IMPORTANT: Please edit backend/.env file with your PostgreSQL credentials!"
    echo ""
else
    echo ".env file already exists."
    echo ""
fi

echo "========================================"
echo "Setup completed successfully!"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Edit backend/.env with your PostgreSQL credentials"
echo "2. Run: cd backend"
echo "3. Run: mvn clean install"
echo "4. Run: mvn spring-boot:run"
echo ""

# Unset password
unset PGPASSWORD
