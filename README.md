# Expense Tracker — Backend

A Personal Finance / Expense Tracker backend built with Java + Spring Boot.
Backend only — no frontend is included in this project, but the API is
designed so a frontend can be connected to it later without any changes
to the endpoints, request shapes, or response shapes.

## 1. Project overview

Users register and log in with a username/email/password. Every
authenticated user can create, view, update, and delete their own
expenses, and see a summary of their spending by category. Data is
stored persistently in a local SQLite database file — no separate
database server to install or run.

## 2. Technologies

- Java 17
- Spring Boot 3 (Spring Web, Spring Security, Spring Data JPA)
- Hibernate (via Spring Data JPA)
- SQLite (file-based database, via the `org.xerial:sqlite-jdbc` driver and
  the `hibernate-community-dialects` module, since Hibernate has no
  official SQLite dialect)
- JJWT for JSON Web Tokens (plain HMAC secret — no RSA, no key files)
- BCrypt for password hashing
- Maven

## 3. Requirements

- Java 17 or newer
- Maven (or use the included `./mvnw` wrapper — no separate install needed)
- No database server to install. SQLite is a single file that Spring Boot
  creates automatically the first time you run the app.

## 4. The SQLite database

- The database is a single file: **`expense_tracker.db`**, created
  automatically in the project's root folder the first time you run the
  application (via `spring.jpa.hibernate.ddl-auto=update` in
  `application.properties`, which also keeps the table schema in sync
  with the `User`/`Expense` entities as they change).
- No database server, no install, no username/password — it's just a
  local file.
- The file is listed in `.gitignore` (along with `*.sqlite`/`*.sqlite3`)
  so it never gets committed. If you delete the file, Spring Boot
  recreates it (empty) the next time you start the app.
- To inspect the data directly, you can open `expense_tracker.db` with
  any SQLite browser (e.g. "DB Browser for SQLite") or the `sqlite3`
  command-line tool.

## 5. Environment variables

Only the JWT settings need configuring — there's no database
username/password because SQLite is a local file, not a server you
connect to.

1. Copy `.env.example` to `.env`.
2. Replace `JWT_SECRET` with your own long, random string (32+
   characters). This is loaded automatically at startup by the
   `spring-dotenv` dependency in `pom.xml` — you don't need to export
   anything manually.
3. `.env` is gitignored — never commit it with a real secret in it.

```
JWT_SECRET=your_secret_here
JWT_EXPIRATION_MS=3600000
```

## 6. How to run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`, and `expense_tracker.db`
appears in the project root on first run.

## 7. Project structure

```
src/main/java/com/example/expensetracker/
├── controller/
│   ├── AuthController.java     → POST /auth/register, POST /auth/login
│   ├── ExpenseController.java  → all /api/expenses/** endpoints
│   └── TestController.java     → GET /api/test
├── service/
│   ├── AuthService.java        → registration/login business logic
│   └── ExpenseService.java     → expense CRUD + ownership checks + summary
├── repository/
│   ├── UserRepository.java     → Spring Data JPA interface for User
│   └── ExpenseRepository.java  → Spring Data JPA interface for Expense
├── model/
│   ├── User.java                → id, username, email, passwordHash
│   └── Expense.java             → id, description, amount, category, date, user
├── dto/
│   ├── RegisterRequest.java, LoginRequest.java, LoginResponse.java
│   └── CreateExpenseRequest.java, UpdateExpenseRequest.java,
│       ExpenseResponse.java, ExpenseSummaryResponse.java
├── security/
│   ├── JwtService.java              → creates/validates JWTs
│   └── JwtAuthenticationFilter.java → reads the Authorization header on every request
└── config/
    └── SecurityConfig.java     → which routes are public/protected, CORS, password hashing bean
```

**Controller** only knows about HTTP — it reads the request, calls a
service method, and returns a response. **Service** holds the actual
business rules (is this username taken, does this password match, does
this expense belong to this user). **Repository** is a thin interface —
Spring Data JPA generates the SQL from the method names, so there's
almost no code to write here. **Entity** (`model/`) is the Java shape
that Hibernate maps directly to a database table. **DTO** is the JSON
shape the outside world actually sees — kept deliberately separate from
the entities so things like `passwordHash` or JPA relationship details
never leak into an API response.

## 8. Authentication flow

**Register** → Controller → `AuthService` → BCrypt hashes the password
→ `UserRepository` saves the user → SQLite.

**Login** → Controller → `AuthService` → BCrypt verifies the password
against the stored hash → `JwtService` generates a signed JWT →
returned to the client as `{ "token": "..." }`.

**Protected request** → client sends `Authorization: Bearer <token>` →
`JwtAuthenticationFilter` runs before the controller, validates the
token, and (if valid) tells Spring Security who's making the request →
`SecurityConfig` allows the request through → Controller → Service →
Repository → SQLite.

### JWT

A signed, stateless token — no server-side session is kept. The secret
used to sign/verify it is a plain HMAC key read from the `JWT_SECRET`
environment variable (never hardcoded, never an RSA key pair, no
`.pem` files anywhere in this project).

### BCrypt

A one-way password hash. Raw passwords are never stored — only
`passwordHash`. Login checks a password with
`passwordEncoder.matches(rawPassword, storedHash)`, which re-hashes the
input and compares, never "decrypting" anything.

### Spring Security

Configured in `SecurityConfig.java` as stateless (no sessions): `/auth/**`
is public, everything under `/api/**` requires a valid JWT.
`JwtAuthenticationFilter` is the piece that actually reads and validates
the token on each request.

## 9. Database flow

```
Postman → Controller → Service → Repository → SQLite → Response
```

Every expense row stores a foreign key back to the `User` who owns it
(a `@ManyToOne` relationship). Ownership is enforced at the query level
in `ExpenseRepository.findByIdAndUser(id, user)` — if the ID exists but
belongs to someone else, the query returns nothing, and the API responds
with the same 404 it would give for a nonexistent ID. This is what stops
a user from reading another user's expense just by guessing IDs.

## 10. API endpoints (exact paths — do not change)

### Auth

| Method | Path | Auth required | Request body |
|--------|------|----------------|---------------|
| POST | `/auth/register` | No | `{ "username": "...", "email": "...", "password": "..." }` |
| POST | `/auth/login` | No | `{ "username": "...", "password": "..." }` |

**Register — example request:**
```json
{ "username": "testuser", "email": "test@example.com", "password": "password123" }
```
**Register — example response (201 Created):**
```json
{ "message": "User registered successfully" }
```

**Login — example request:**
```json
{ "username": "testuser", "password": "password123" }
```
**Login — example response (200 OK):**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...." }
```

### Test

| Method | Path | Auth required |
|--------|------|----------------|
| GET | `/api/test` | Yes (Bearer JWT) |

**Example response (200 OK):**
```json
{ "message": "You are authenticated!", "user": "testuser" }
```

### Expenses (all require `Authorization: Bearer <token>`)

| Method | Path | Body |
|--------|------|------|
| POST | `/api/expenses` | `{ "description": "...", "amount": 250, "category": "...", "date": "2026-09-19" }` |
| GET | `/api/expenses` | — |
| GET | `/api/expenses/{id}` | — |
| PUT | `/api/expenses/{id}` | same shape as POST |
| DELETE | `/api/expenses/{id}` | — |
| GET | `/api/expenses/summary` | — |

**Create — example request:**
```json
{ "description": "Lunch", "amount": 250, "category": "Food", "date": "2026-09-19" }
```
**Create — example response (201 Created):**
```json
{ "id": 1, "description": "Lunch", "amount": 250, "category": "Food", "date": "2026-09-19" }
```

**Get all — example response (200 OK):**
```json
[
  { "id": 1, "description": "Lunch", "amount": 250, "category": "Food", "date": "2026-09-19" }
]
```

**Get by id — 200 OK if you own it, 404 if it doesn't exist or belongs to someone else.**

**Update — same request/response shape as create; 404 if not owned.**

**Delete — 204 No Content on success; 404 if not owned.**

**Summary — example response (200 OK):**
```json
{
  "totalAmount": 250,
  "categoryTotals": { "Food": 250 }
}
```

None of these responses include `passwordHash` or any other internal
field — only what's declared in the DTOs.

## 11. Testing with Postman — recommended order

1. Start the app (`./mvnw spring-boot:run`) — `expense_tracker.db` is created automatically.
2. `POST /auth/register` with the JSON above.
3. `POST /auth/login` — copy the `token` from the response.
4. In Postman, go to the request's **Authorization** tab → type **Bearer Token** → paste the token.
5. `GET /api/test` — confirms the token works.
6. `POST /api/expenses` — create one.
7. `GET /api/expenses` — see it listed.
8. `GET /api/expenses/{id}` — using the id from step 6.
9. `PUT /api/expenses/{id}` — update it.
10. `DELETE /api/expenses/{id}` — remove it.
11. `GET /api/expenses/summary` — check totals.

cURL equivalent for the first few steps:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

curl http://localhost:8080/api/test \
  -H "Authorization: Bearer <paste token here>"
```

## 12. How a future frontend connects

The frontend never needs to know a database is involved at all — it
only ever talks to the REST API above. The expected flow:

1. Register / log in → receive `{ "token": "..." }`.
2. Store the token (e.g. in memory or local storage on the frontend side).
3. Attach it as `Authorization: Bearer <token>` on every request to
   `/api/**`.
4. Use the expense endpoints exactly as documented above.

`SecurityConfig.java` already includes a CORS configuration
(`corsConfigurationSource()` bean) allowing requests from
`http://localhost:3000` and `http://localhost:5173` — the default ports
for Create React App and Vite. Update the allowed origins there once you
know where the frontend will actually run.

## 13. Common errors

- **`Whitelabel Error Page` / 404 on a URL you expect to work** — double-check
  the exact path and HTTP method against the table above; a GET to a
  POST-only endpoint (or vice versa) returns 404/405, not your controller's response.
- **401 on a protected endpoint** — check the `Authorization` header is
  exactly `Bearer <token>` (capital B, one space), and that the token
  hasn't expired (`JWT_EXPIRATION_MS`).
- **`Table "USERS" not found` or similar on first run** — make sure
  `spring.jpa.hibernate.ddl-auto=update` is set; this is what creates
  the tables automatically from the entities.
- **`No Dialect mapping for JDBC type` or dialect-related startup errors** —
  confirm both the `sqlite-jdbc` and `hibernate-community-dialects`
  dependencies are present in `pom.xml`, and that
  `spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect`
  is set exactly as shown.
- **404 on `/api/expenses/5` that you expect to work** — if that
  expense belongs to a different user than the one whose token you're
  using, this 404 is correct behavior (ownership enforcement), not a bug.
- **Database looks "wrong" when inspected directly** — SQLite has
  dynamic typing, so numeric/date columns may display differently than
  in MySQL/Postgres when viewed in a raw SQLite browser. The values read
  back correctly through the API regardless.
- **`database is locked`** — SQLite allows only one writer at a time;
  this can happen under heavy concurrent testing but is very unlikely
  during normal Postman testing.

## 14. Git/GitHub safety

- `.env` (real secrets) and `expense_tracker.db` (real data) are both
  gitignored — never commit either.
- `.env.example` (no real secret) is committed, so anyone cloning the
  repo knows what variables to set.
- No RSA key files exist anywhere in this project — JWT signing uses a
  single HMAC secret, nothing else.
