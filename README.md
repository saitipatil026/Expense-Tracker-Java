# Expense-Tracker-Java

# 💰 Expense Tracker

A Personal Finance / Expense Tracker built with:

* **Backend:** Java + Spring Boot
* **Database:** PostgreSQL
* **Frontend:** React.js

---

# 📁 Project Structure

```text
ExpenseTracker/
├── pom.xml                  Maven deps: web, security, jjwt (JWT), spring-dotenv, tests
├── .env                     JWT_SECRET, JWT_EXPIRATION_MS (gitignored)
├── .gitignore
├── README.md                 setup, endpoints, curl examples, common errors
└── src/main/java/.../financetracker/
    ├── FinanceTrackerApplication.java
    ├── model/       User.java, Expense.java
    ├── dto/         RegisterRequest, LoginRequest, LoginResponse,
    │                CreateExpenseRequest, UpdateExpenseRequest,
    │                ExpenseResponse, ExpenseSummaryResponse
    ├── repository/  DummyUserRepository, DummyExpenseRepository
    ├── service/     AuthService, ExpenseService
    ├── security/    JwtService, JwtAuthenticationFilter
    ├── controller/  AuthController, TestController, ExpenseController
    └── config/      SecurityConfig
```

> Currently, the backend is already made.
> Frontend and real database need to be connected/added.

---

# 👨‍💻 BACKEND

**Technology:** Java + Spring Boot

The backend already contains:

* User registration
* User login
* BCrypt password hashing
* JWT authentication
* Expense CRUD
* Expense summary
* User-specific expenses

### Run Backend

```bash
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

---

## Backend APIs

### Auth

| Method | URL              | Login Required |
| ------ | ---------------- | -------------- |
| POST   | `/auth/register` | ❌              |
| POST   | `/auth/login`    | ❌              |

### Expenses

| Method | URL                     | Login Required |
| ------ | ----------------------- | -------------- |
| POST   | `/api/expenses`         | ✅              |
| GET    | `/api/expenses`         | ✅              |
| GET    | `/api/expenses/{id}`    | ✅              |
| PUT    | `/api/expenses/{id}`    | ✅              |
| DELETE | `/api/expenses/{id}`    | ✅              |
| GET    | `/api/expenses/summary` | ✅              |

Protected requests need:

```text
Authorization: Bearer <JWT_TOKEN>
```

---

# 🗄️ DATABASE

**Technology:** PostgreSQL + Spring Data JPA

### What to do

Replace the current dummy/in-memory repositories with PostgreSQL.

Currently:

```text
DummyUserRepository
DummyExpenseRepository
```

These use Java `List`s, so data disappears when the backend restarts.

---

## Tables

### users

```text
id
username
email
password
```

### expenses

```text
id
amount
category
date
note
user_id
```

Relationship:

```text
User 1 ──────── * Expenses
```

One user can have multiple expenses.

`user_id` tells us which user owns an expense.

### Important

Passwords must remain **BCrypt hashed**.

Do not store plain-text passwords.

---

# 🎨 FRONTEND

**Technology:** React.js

Create the frontend separately.

### Required Pages

```text
Login
Register
Dashboard
Add Expense
Edit Expense
```

### Dashboard should show

* Total expenses
* Expense list
* Category
* Amount
* Date
* Note
* Edit button
* Delete button
* Expense summary

---

## Connecting Frontend to Backend

Backend:

```text
http://localhost:8080
```

Frontend will call the backend APIs.

Example:

```javascript
fetch("http://localhost:8080/api/expenses", {
  headers: {
    Authorization: `Bearer ${token}`
  }
});
```

After login, save/use the JWT and send it with every protected API request.

---

# 🔄 HOW EVERYTHING CONNECTS

```text
             React
               │
               │ HTTP + JWT
               ↓
        Spring Boot Backend
               │
               ↓
        Spring Data JPA
               │
               ↓
          PostgreSQL
```

---

# 🧪 Testing Order

Before connecting everything:

### 1. Backend

Run:

```bash
mvn spring-boot:run
```

Test APIs using Postman.

### 2. Database

Connect PostgreSQL and make sure users/expenses are being saved.

### 3. Frontend

Run:

```bash
npm install
npm run dev
```

Then connect React to the backend APIs.

---

# ⚠️ Important for Everyone

* Don't change API URLs without telling the team.
* Don't commit `.env` or passwords to GitHub.
* Don't store plain-text passwords.
* A user must only see **their own expenses**.
* Test your changes before pushing.
* Pull the latest code before starting work.

---

# 🎯 Final Goal

```text
             EXPENSE TRACKER
                    │
       ┌────────────┼────────────┐
       ↓            ↓            ↓
    FRONTEND     BACKEND      DATABASE
    React.js   Spring Boot   PostgreSQL
       │            │            │
       └────────────┴────────────┘
                    │
              Working App
```
