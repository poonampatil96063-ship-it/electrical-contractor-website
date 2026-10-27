# Electrical Contractor Website (Starter Project)

A starter full-stack website for an electrical contractor:
- React + Vite frontend
- Java + Spring Boot REST API
- MySQL database
- Customer registration and login
- Customers can submit and view their own electrical work requests
- Admin can view requests and create quotations

## Requirements
- Java 17 or 21
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create the database
```sql
CREATE DATABASE electrical_contractor_db;
```

## 2. Configure backend environment
From the `backend` directory, set database credentials in your terminal. Example for Ubuntu:
```bash
export DB_USERNAME="your_mysql_username"
export DB_PASSWORD="your_mysql_password"
export ADMIN_EMAIL="admin@example.com"
export ADMIN_PASSWORD="ChangeThisAdminPassword123!"
```
The admin account is created automatically when the backend starts for the first time. Change the example password before using the project.

## 3. Run the backend
```bash
cd backend
mvn spring-boot:run
```
Backend API runs at `http://localhost:8080`.

## 4. Run the frontend
Open a second terminal:
```bash
cd frontend
npm install
npm run dev
```
Open the local URL printed by Vite, usually `http://localhost:5173`.

## Starter account flow
1. Register a customer from the website.
2. Log in and submit a work request.
3. Log out and log in using the configured admin email/password.
4. Open the Admin section and create a quotation for a request.

## Important security notes
This is a learning starter, not a production-ready deployment. Before publishing it publicly:
- Use HTTPS and secure cookie settings.
- Set strong unique database and admin passwords.
- Add rate limiting, email verification, password reset, audit logging, and stricter validation.
- Review CORS and session settings for your actual domain.
- Use database migrations for production instead of `ddl-auto=update`.
- Back up the database and restrict server access.
- Do not commit real passwords or secrets to GitHub.

## Project structure
```text
electrical-contractor-website/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/electrical/
│       └── resources/application.properties
└── frontend/
    ├── package.json
    ├── index.html
    └── src/
```
