# ExpenseTracker Backend (Phase 1: Auth + JWT)

A Spring Boot backend for a personal finance/expense tracker. This phase
only covers registration, login, and a protected test endpoint — no
frontend, no real database yet (data lives in memory and resets on
restart).

## Request flow

```
Postman/cURL -> Controller -> Service -> DummyUserRepository (List<User>)
                                  |
                                  +-> BCrypt (hash/verify password)
                                  +-> JwtService (issue token on login)
```

Every request to a protected endpoint also passes through
`JwtAuthenticationFilter`, which reads the `Authorization: Bearer <token>`
header before the request reaches any controller.

## Project structure

```
src/main/java/com/example/financetracker/
├── controller/   AuthController, TestController   (HTTP in/out only)
├── service/      AuthService                       (business logic)
├── repository/   DummyUserRepository                (in-memory storage)
├── model/        User
├── dto/          RegisterRequest, LoginRequest, LoginResponse
├── security/     JwtService, JwtAuthenticationFilter
└── config/       SecurityConfig
```

## Setup

1. Requires **Java 17+** and **Maven**.
2. Edit `.env` and replace `JWT_SECRET` with your own long random string
   (32+ characters). This file is already in `.gitignore` — never commit it.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
   The app starts on `http://localhost:8080`.

## Endpoints

| Method | Path                | Auth required | Body                                                  |
|--------|---------------------|----------------|--------------------------------------------------------|
| POST   | `/auth/register`    | No             | `{ "username", "email", "password" }`                   |
| POST   | `/auth/login`       | No             | `{ "username", "password" }`                            |
| GET    | `/api/test`         | Yes (Bearer)   | —                                                        |
| POST   | `/api/expenses`     | Yes (Bearer)   | `{ "description", "amount", "category", "date"? }`       |
| GET    | `/api/expenses`     | Yes (Bearer)   | —  (lists only the logged-in user's expenses)            |
| GET    | `/api/expenses/summary`| Yes (Bearer)| —  (total spend + totals per category)                   |
| GET    | `/api/expenses/{id}`| Yes (Bearer)   | —                                                        |
| PUT    | `/api/expenses/{id}`| Yes (Bearer)   | `{ "description", "amount", "category", "date"? }`       |
| DELETE | `/api/expenses/{id}`| Yes (Bearer)   | —                                                        |

Expenses are scoped per user: `DummyExpenseRepository` filters everything
by the username embedded in the JWT, so one user can never see, edit, or
delete another user's expenses. Requesting someone else's expense ID
returns `404 Not Found` (not `403 Forbidden`) — this avoids confirming to
an attacker that an ID exists at all.

## Testing with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@gmail.com","password":"password123"}'
```
Expected: `201 Created`, body `"User registered successfully"`.

**Register the same username again** → expected `400 Bad Request`,
`"Username already taken"`.

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```
Expected: `200 OK`, body `{ "token": "..." }`.

**Login with wrong password** → expected `401 Unauthorized`.

**Protected endpoint without a token:**
```bash
curl http://localhost:8080/api/test
```
Expected: `401 Unauthorized`.

**Protected endpoint with a valid token:**
```bash
curl http://localhost:8080/api/test \
  -H "Authorization: Bearer <paste token from login response here>"
```
Expected: `200 OK`, `"Hello, john! You accessed a protected endpoint."`.

**Create an expense (use the token from login):**
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"description":"Groceries","amount":42.50,"category":"Food"}'
```
Expected: `201 Created`, body includes the generated `id` and today's date
(since `date` was omitted).

**List your expenses:**
```bash
curl http://localhost:8080/api/expenses -H "Authorization: Bearer <token>"
```

**Get/update/delete a specific expense** — swap in the `id` from create,
and for another user's token, expect `404 Not Found` instead of the data.

**Get spending summary:**
```bash
curl http://localhost:8080/api/expenses/summary -H "Authorization: Bearer <token>"
```
Expected: `200 OK`, `{ "totalAmount": ..., "totalsByCategory": { "Food": ..., ... } }`.

## Common errors

- **401 on every protected request even with a token** — check there's no
  extra space/typo after `Bearer `, and that the token hasn't expired
  (default expiry is 1 hour, set by `JWT_EXPIRATION_MS`).
- **App fails to start with a key-length error** — `JWT_SECRET` in `.env`
  is too short; HMAC-SHA256 needs at least 32 bytes.
- **`.env` values not picked up** — make sure you run the app from the
  project root (where `.env` lives), not from inside `target/`.

## What changes when a real database is added later

Only `DummyUserRepository` gets replaced (with a `UserRepository extends
JpaRepository<User, Long>`), and `User` gets JPA annotations
(`@Entity`, `@Id`, etc.). `AuthController`, `AuthService`,
`SecurityConfig`, and the JWT classes stay untouched, since they only
depend on the repository's method signatures, not its storage mechanism.

## What stays the same when a frontend is added later

The three endpoints above (`/auth/register`, `/auth/login`, `/api/test`)
and their JSON shapes don't need to change — a frontend just becomes
another HTTP client calling them, the same way Postman/cURL do now.
