ExpenseTracker Backend

Spring Boot backend for our Personal Finance / Expense Tracker academic project.

The backend currently provides:

User registration and login
JWT authentication
Expense CRUD operations
Expense summary
User-specific expense access
Postman/cURL testing
In-memory storage for now

Current status: No frontend and no real database yet. Data is stored in Java Lists and is lost whenever the application restarts.

1. Tech Stack
Java 17+
Spring Boot
Spring Security
JWT
BCrypt
Maven
Postman / cURL
In-memory List storage

Later:

MySQL
Spring Data JPA
Frontend
2. How the Backend Works

The basic request flow is:

Postman / Frontend
        ↓
   Controller
        ↓
     Service
        ↓
   Repository
        ↓
 In-memory List

For authentication:

Login
  ↓
AuthController
  ↓
AuthService
  ↓
BCrypt verifies password
  ↓
JwtService creates JWT
  ↓
Token returned to client

For protected requests:

Frontend / Postman
        ↓
Authorization: Bearer <token>
        ↓
JwtAuthenticationFilter
        ↓
Check JWT
        ↓
Controller
        ↓
Service
        ↓
Repository

The frontend/Postman does not directly access the repository.

3. Project Structure
src/main/java/com/example/financetracker/

├── controller/
│   ├── AuthController.java
│   ├── ExpenseController.java
│   └── TestController.java
│
├── service/
│   ├── AuthService.java
│   └── ExpenseService.java
│
├── repository/
│   ├── DummyUserRepository.java
│   └── DummyExpenseRepository.java
│
├── model/
│   ├── User.java
│   └── Expense.java
│
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── CreateExpenseRequest.java
│   ├── UpdateExpenseRequest.java
│   ├── ExpenseResponse.java
│   └── ExpenseSummaryResponse.java
│
├── security/
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
│
└── config/
    └── SecurityConfig.java
What each folder does
Folder	Purpose
controller/	Receives HTTP requests and returns responses
service/	Contains business logic
repository/	Stores and retrieves data
model/	Represents User and Expense objects
dto/	Defines request/response JSON
security/	JWT authentication
config/	Spring Security configuration
Important rule

Don't put business logic inside controllers.

The normal pattern is:

Controller → Service → Repository
4. Setup
Requirements

Install:

Java 17+
Maven
Git

Check:

java -version
mvn -version
Clone the project
git clone <GITHUB_REPOSITORY_URL>
cd Expense-Tracker-Java
Environment variables

Create a .env file in the project root:

JWT_SECRET=your_random_secret_key_at_least_32_characters
JWT_EXPIRATION_MS=3600000

Example:

JWT_SECRET=my_super_secret_key_for_expense_tracker_2026
JWT_EXPIRATION_MS=3600000

.env is already in .gitignore.

Never push .env to GitHub.

5. Run the Backend

From the project root:

mvn spring-boot:run

The server should start at:

http://localhost:8080

You can use Postman to test the API.

6. API Endpoints
Authentication
Method	Endpoint	Auth
POST	/auth/register	❌
POST	/auth/login	❌
Register
POST http://localhost:8080/auth/register

Body:

{
  "username": "john",
  "email": "john@gmail.com",
  "password": "password123"
}

Expected:

201 Created
Login
POST http://localhost:8080/auth/login

Body:

{
  "username": "john",
  "password": "password123"
}

Response:

{
  "token": "YOUR_JWT_TOKEN"
}

Save this token.

You need it for protected endpoints.

7. Using the JWT Token

For protected requests, add:

Authorization: Bearer YOUR_JWT_TOKEN

In Postman:

Authorization
Type: Bearer Token
Token: <paste your token>

You don't need to manually write Bearer if Postman is set to Bearer Token.

8. Expense Endpoints
Method	Endpoint	Auth
GET	/api/test	✅
POST	/api/expenses	✅
GET	/api/expenses	✅
GET	/api/expenses/{id}	✅
PUT	/api/expenses/{id}	✅
DELETE	/api/expenses/{id}	✅
GET	/api/expenses/summary	✅
Create Expense
POST http://localhost:8080/api/expenses

Body:

{
  "description": "Groceries",
  "amount": 42.50,
  "category": "Food"
}

date is optional.

If omitted, the backend uses today's date.

Get All Expenses
GET http://localhost:8080/api/expenses

Returns only the logged-in user's expenses.

Get One Expense
GET http://localhost:8080/api/expenses/1

If the expense doesn't exist or belongs to another user, the API returns:

404 Not Found

This prevents users from finding out whether another user's expense ID exists.

Update Expense
PUT http://localhost:8080/api/expenses/1

Body:

{
  "description": "Groceries Updated",
  "amount": 45.00,
  "category": "Food",
  "date": "2026-09-16"
}
Important

Currently PUT works as a full replacement.

Send the complete expense object.

Don't send only:

{
  "amount": 50
}

because the other fields may be cleared.

Delete Expense
DELETE http://localhost:8080/api/expenses/1

Expected:

204 No Content
Expense Summary
GET http://localhost:8080/api/expenses/summary

Example:

{
  "totalAmount": 107.50,
  "totalsByCategory": {
    "Food": 87.50,
    "Transport": 20.00
  }
}
9. User Data Isolation

Users should only be able to access their own expenses.

For example:

User A
 ├── Expense 1
 └── Expense 2

User B
 ├── Expense 3
 └── Expense 4

User A cannot access User B's expenses.

The JWT contains the logged-in username.

The backend uses that username when retrieving, updating and deleting expenses.

10. Testing Order

If you're testing the backend for the first time, use this order:

1. Start Spring Boot
        ↓
2. Register
        ↓
3. Login
        ↓
4. Copy JWT token
        ↓
5. Test /api/test
        ↓
6. Create expense
        ↓
7. Get all expenses
        ↓
8. Get expense by ID
        ↓
9. Update expense
        ↓
10. Get summary
        ↓
11. Delete expense

A Postman collection is available in:

postman/
11. Database — Later
Current situation

We are not using MySQL yet.

Currently:

DummyUserRepository
        ↓
List<User>

DummyExpenseRepository
        ↓
List<Expense>

When the application restarts:

ALL DATA IS LOST
Later

We will replace this with:

Spring Boot
    ↓
Service
    ↓
JPA Repository
    ↓
MySQL

The database work should mainly involve:

model/
repository/
application.properties
pom.xml

The goal is to avoid unnecessarily changing the controllers and business logic.

12. Database Setup — Later

When the team is ready to add MySQL:

Create database
CREATE DATABASE financetracker;
Add dependencies

In pom.xml:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
.env

Add:

DB_USERNAME=root
DB_PASSWORD=your_mysql_password

Keep the existing JWT variables too.

application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/financetracker
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

The database teammate should read the project code before making these changes.

13. Frontend — Later

The frontend will communicate with this backend using HTTP.

React / HTML / JS
        ↓
HTTP Request
        ↓
Spring Boot
        ↓
JSON Response

Base URL:

http://localhost:8080

For example:

fetch("http://localhost:8080/api/expenses", {
    headers: {
        "Authorization": `Bearer ${token}`
    }
});
14. CORS

When a browser frontend starts calling the backend, CORS will need to be configured.

Postman and cURL don't have the same browser CORS restriction.

For example, if the frontend runs at: 

http://localhost:5173

the backend needs to allow that origin.

The backend/security teammate should handle this in SecurityConfig.java.

Important: The frontend's actual development URL should be used. Vite commonly uses 5173, while other setups may use a different port. 

15. Frontend Development Order

Recommended order:

1. Register page
        ↓
2. Login page
        ↓
3. Store JWT
        ↓
4. Test authenticated request
        ↓
5. Display expenses
        ↓
6. Add expense
        ↓
7. Edit expense
        ↓
8. Delete expense
        ↓
9. Dashboard / Summary

The frontend does not need to understand the Java repository layer.

It only needs to know:

URL
HTTP method
Request body
Authorization header
Response
16. Common Errors
401 Unauthorized

Check:

Token is present
Token hasn't expired
Header is correct
Postman is using Bearer Token

Correct:

Authorization: Bearer <token>
JWT key-length error

Make sure:

JWT_SECRET=...

is long enough.

Use a random secret of at least 32 characters.

.env not working

Make sure .env is in the project root:

Expense-Tracker-Java/
├── .env
├── pom.xml
├── README.md
└── src/

Don't commit it.

404 Not Found for an expense

The expense may:

Not exist
Belong to another user

Both intentionally return 404.

Data disappeared

That's expected right now.

We're using in-memory storage:

List<User>
List<Expense>

Restarting Spring Boot clears the data.

After MySQL is added, data will persist.

17. GitHub Team Workflow

Everyone should work from the same repository.

Don't directly work on main.

Create a feature branch:

git checkout -b your-feature-name

Examples:

git checkout -b expense-crud
git checkout -b mysql-database
git checkout -b frontend

After making changes:

git add .
git commit -m "Add expense CRUD"
git push -u origin expense-crud

Then create a Pull Request on GitHub.

Basic workflow:

main
  │
  ├── backend branch
  │
  ├── database branch
  │
  └── frontend branch
          ↓
       Pull Request
          ↓
       Review
          ↓
         main
Important

Before starting new work:

git checkout main
git pull

Then create/update your feature branch.

Don't commit:

.env
target/
18. Who Should Work Where?

A simple division can be:

Backend

Responsible for:

controller/
service/
security/
config/

Current work includes:

Authentication
JWT
Expense CRUD
API behavior
Validation
CORS
Database

Responsible for:

model/
repository/
pom.xml
application.properties

Main work:

MySQL
JPA
Entity mapping
Repository interfaces
Database testing
Frontend

Responsible for:

frontend/

Main work:

Register
Login
Expense list
Add expense
Edit expense
Delete expense
Dashboard
Connecting to REST API

The frontend does not directly access the database.

19. Important Rules for the Team
Don't
Push .env
Push passwords/API keys
Directly edit another person's feature without discussing it
Commit target/
Put database code inside controllers
Put frontend code inside the Spring Boot controller/ package
Change API endpoints without telling the team
Do
Use feature branches
Make small commits
Pull before starting work
Test your changes
Create a Pull Request
Tell the team when you change an endpoint or JSON format
20. If You're New to Spring Boot

Don't try to understand the entire project at once.

Read in this order:

1. AuthController.java
        ↓
2. AuthService.java
        ↓
3. DummyUserRepository.java

This shows the complete flow:

HTTP request
    ↓
Controller
    ↓
Service
    ↓
Repository

Then read:

4. JwtService.java
5. JwtAuthenticationFilter.java
6. ExpenseController.java
7. ExpenseService.java
8. DummyExpenseRepository.java
9. SecurityConfig.java

Once you understand this flow, the database and frontend parts will be much easier to understand.

21. The Main Idea

You don't need to understand every Spring Boot feature immediately.

For this project, remember:

FRONTEND / POSTMAN
        ↓
    CONTROLLER
        ↓
      SERVICE
        ↓
    REPOSITORY
        ↓
 DATABASE / LIST

Authentication adds:

LOGIN
  ↓
BCrypt
  ↓
JWT
  ↓
JWT Filter
  ↓
Protected API

And the frontend simply communicates with the backend through the API.

This is the structure the whole team should follow.
