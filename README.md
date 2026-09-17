# ExpenseTracker Backend

A Spring Boot backend for our Personal Finance / Expense Tracker academic project. Covers user registration/login (JWT-based auth) and full CRUD + summary for expenses. No frontend yet — no real database yet either (data lives in memory and resets every restart).

This doc has four parts:
1. [Setup, endpoints, testing](#1-setup) — get it running and try it out
2. [How this backend works](#2-how-this-backend-works-read-this-first) — read this before changing anything, especially if Spring Boot is new to you
3. [Database guide](#3-database-guide-replacing-the-dummy-repositories) — for whoever's adding the real database
4. [Frontend guide](#4-frontend-guide-connecting-to-this-backend) — for whoever's building the UI

---

## 1. Setup

### Request flow

```
Postman/frontend -> Controller -> Service -> DummyRepository (in-memory List)
                                       |
                                       +-> BCrypt (hash/verify password)
                                       +-> JwtService (issue/validate tokens)
```

Every request to a protected endpoint (anything outside `/auth/**`) passes through `JwtAuthenticationFilter` first, which reads the `Authorization: Bearer YOUR_TOKEN_HERE` header before the request ever reaches a controller.

### Project structure

```
src/main/java/com/example/financetracker/
├── controller/   AuthController, ExpenseController, TestController   (HTTP in/out only, no logic)
├── service/      AuthService, ExpenseService                          (business logic lives here)
├── repository/   DummyUserRepository, DummyExpenseRepository          (in-memory storage — stand-in for a real DB)
├── model/        User, Expense
├── dto/          Request/response shapes — kept separate from models on purpose
├── security/     JwtService, JwtAuthenticationFilter
└── config/       SecurityConfig
```

### Getting it running

1. Requires **Java 17+** and **Maven**.
2. Create a `.env` file in the project root with the two lines below (`.env` is already in `.gitignore` — **never commit it**):

```
JWT_SECRET=replace-with-a-random-32-plus-character-string
JWT_EXPIRATION_MS=3600000
```

3. Run:

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

### Endpoints

| Method | Path                     | Auth required | Body                                                |
|--------|--------------------------|----------------|-------------------------------------------------------|
| POST   | `/auth/register`         | No             | `{ "username", "email", "password" }`                 |
| POST   | `/auth/login`            | No             | `{ "username", "password" }`                           |
| GET    | `/api/test`              | Yes (Bearer)   | —                                                      |
| POST   | `/api/expenses`          | Yes (Bearer)   | `{ "description", "amount", "category", "date"? }`     |
| GET    | `/api/expenses`          | Yes (Bearer)   | — (lists only the logged-in user's expenses)           |
| GET    | `/api/expenses/summary`  | Yes (Bearer)   | — (total spend + totals per category)                  |
| GET    | `/api/expenses/{id}`     | Yes (Bearer)   | —                                                      |
| PUT    | `/api/expenses/{id}`     | Yes (Bearer)   | `{ "description", "amount", "category", "date"? }` — full replace, see note below |
| DELETE | `/api/expenses/{id}`     | Yes (Bearer)   | —                                                      |

Expenses are scoped per user — `DummyExpenseRepository` filters everything by the username embedded in the JWT, so one user can never see, edit, or delete another user's expenses. Requesting someone else's expense ID returns `404 Not Found` (not `403`), so it doesn't even confirm to an attacker that the ID exists.

> **Note on PUT:** it's currently a full replace — send all four fields (`date` is the only one that falls back to the existing value if omitted). Sending a partial body will null out the fields you leave off, so always send the complete expense when updating.

### Testing with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@gmail.com","password":"password123"}'
```
Expected: `201 Created`.

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```
Expected: `200 OK`, body `{ "token": "..." }` — save this token, you'll need it for everything below.

**Create an expense:**
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"description":"Groceries","amount":42.50,"category":"Food"}'
```
Expected: `201 Created`, body includes the generated `id` and today's date (since `date` was omitted).

**List your expenses:**
```bash
curl http://localhost:8080/api/expenses -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Get spending summary:**
```bash
curl http://localhost:8080/api/expenses/summary -H "Authorization: Bearer YOUR_TOKEN_HERE"
```
Expected: `{ "totalAmount": ..., "totalsByCategory": { "Food": ..., ... } }`.

**Get / update / delete a specific expense** — swap in the `id` from create. Try it with another user's token too — you should get `404 Not Found` instead of the data.

A Postman collection is in `postman/` if you'd rather test that way.

### Common errors

- **401 on every protected request even with a token** — check there's no extra space/typo after `Bearer `, and that the token hasn't expired (default expiry is 1 hour, controlled by `JWT_EXPIRATION_MS`).
- **App fails to start with a key-length error** — `JWT_SECRET` in `.env` is too short; HMAC-SHA256 needs at least 32 bytes.
- **`.env` values not picked up** — make sure you're running from the project root (where `.env` lives), not from inside `target/`.
- **Fields silently disappear after a PUT** — see the PUT note above; send the full expense object, not just the changed field.

### Current limitations (by design, for now)

- **No persistence** — `DummyUserRepository`/`DummyExpenseRepository` are in-memory lists. Everything resets when the app restarts. Swapping in a real database later only touches the repository classes — see [Part 3](#3-database-guide-replacing-the-dummy-repositories).
- **No input validation yet** — nothing currently stops a negative `amount` or a blank `username`/`password` from being accepted. Don't rely on the API to reject bad input until this is added.
- **No frontend integration yet, and no CORS config** — see [Part 4](#4-frontend-guide-connecting-to-this-backend).

### Branching / workflow

- Work off feature branches, PR into `dev_backend_structure` (or whatever we're using as the integration branch) before merging to `main`.
- Keep `.env` out of every commit — double check before pushing if you've touched `application.properties`.

---

## 2. How this backend works (read this first)

This section explains what's already in the codebase and *why* it's structured this way — written for teammates who haven't worked with Spring Boot before.

### The big picture

This is a REST API: a server that accepts HTTP requests (`GET`, `POST`, `PUT`, `DELETE`) and returns JSON. It doesn't render any web pages itself — a frontend (or Postman, or cURL) calls it over HTTP and gets JSON back.

Two things it does:
1. **Auth** — register an account, log in, get a token proving who you are.
2. **Expenses** — create/read/update/delete expense entries, scoped to whoever's logged in.

### The four layers, and why they're separated

Every feature (auth, expenses) is split across four files instead of one big file. This is the standard Spring Boot pattern, and it's worth understanding *why*, not just copying it:

```
Controller  →  Service  →  Repository  →  (in-memory list, for now)
```

| Layer | File example | Job | Rule of thumb |
|---|---|---|---|
| **Controller** | `ExpenseController.java` | Receives the HTTP request, calls the service, returns the HTTP response | Should contain almost no logic — just "take request in, hand to service, send response back" |
| **Service** | `ExpenseService.java` | The actual business logic ("can this user edit this expense?", "how do I total up spending by category?") | This is where you write the *rules* |
| **Repository** | `DummyExpenseRepository.java` | Stores and retrieves data | Right now it's a Java `List` in memory. Later it'll talk to a real database. **Nothing else in the app needs to know or care which one it is** — that's the whole point |
| **Model** | `Expense.java` | Represents one row of data (one expense, one user) | Just fields + getters/setters, no logic |

**Why bother splitting these up?** Because the database person can rewrite the Repository layer completely (swap the `List` for real SQL) without touching the Controller or Service at all — as long as the method names/signatures stay the same. That's the entire reason this structure exists, and it's why [the database guide](#3-database-guide-replacing-the-dummy-repositories) only touches `repository/` and `model/`.

There's a fifth folder, **`dto/`** (Data Transfer Objects) — these define exactly what JSON shape goes *in* to a request and *out* of a response. They exist so the API never accidentally exposes something it shouldn't (like a password hash) and so the JSON shape can stay stable even if the internal `Expense`/`User` classes change later.

### Key Spring Boot concepts you'll see everywhere

- **`@RestController`** — marks a class as something that handles HTTP requests and returns JSON (as opposed to an HTML page).
- **`@RequestMapping("/api/expenses")`** — sets the base URL path for every method in that controller.
- **`@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`** — map a specific method to a specific HTTP verb + path.
- **`@RequestBody`** — "parse the incoming JSON into this Java object for me."
- **`@PathVariable`** — pulls a value out of the URL itself, e.g. the `{id}` in `/api/expenses/{id}`.
- **`@Service` / `@Repository` / `@Component`** — these all mean roughly "let Spring manage an instance of this class for me." Spring creates one instance of each and automatically passes it into whatever constructor asks for it (this is called **dependency injection** — it's why you see classes taking other classes as constructor parameters without ever calling `new SomeService()` themselves).
- **`@Configuration` / `@Bean`** — used in `SecurityConfig.java` to define app-wide setup, like which routes need auth and how passwords get hashed.

You don't need to memorize these — just recognize them and know roughly what they do when you see them.

### The auth flow, step by step

1. **Register** (`POST /auth/register`): client sends `username`, `email`, `password`. `AuthService` checks the username isn't taken, hashes the password with **BCrypt** (never stores the raw password — not even temporarily), and saves the user.
2. **Login** (`POST /auth/login`): client sends `username`, `password`. `AuthService` looks up the user, checks the password against the stored hash, and if it matches, issues a **JWT** (JSON Web Token) — a signed string that says "this is `username`, valid until `X` time."
3. **Every request after that** includes `Authorization: Bearer YOUR_TOKEN_HERE` in its headers. `JwtAuthenticationFilter` intercepts every request *before* it reaches a controller, checks the token is valid and not expired, and — if so — tells Spring Security "this request is authenticated as `username`."
4. Controllers that need to know who's logged in just take an `Authentication` parameter and call `.getName()` — that's the username the filter already verified. This is why `ExpenseController` never has to check the token itself; by the time its methods run, that's already handled.

**Why a JWT instead of a traditional session?** A session requires the server to remember who's logged in (stored server-side). A JWT is *self-contained* — the token itself proves identity, so the server doesn't need to store anything between requests. This is what "stateless" means in `SecurityConfig` (`SessionCreationPolicy.STATELESS`). It matters for scaling and for API-only backends like this one.

### Where "ownership" is enforced

Every expense is tagged with `ownerUsername` when created. Every single repository method that reads/edits/deletes an expense requires that username to match — this is what stops User A from seeing User B's expenses. It's enforced at the repository (data) layer on purpose, not just in the controller, so there's no code path that can accidentally skip the check.

### Reading order if you're new to this codebase

1. `AuthController.java` → `AuthService.java` → `DummyUserRepository.java` (the simplest full slice, start to finish)
2. `JwtService.java` + `JwtAuthenticationFilter.java` (how a token gets checked on every later request)
3. `ExpenseController.java` → `ExpenseService.java` → `DummyExpenseRepository.java` (same pattern, one more field of complexity — ownership)
4. `SecurityConfig.java` last — it ties the whole filter chain together and makes more sense once you've seen what it's protecting

---

## 3. Database guide: replacing the dummy repositories

This section walks through swapping `DummyUserRepository` and `DummyExpenseRepository` (in-memory lists) for a real database using **Spring Data JPA**.

The good news: because of the layering explained above, this task only touches `model/` and `repository/`. You should not need to change any Controller or Service code, or the security/JWT code at all.

### 3.1 Pick a database

**MySQL** is the easiest choice if the team is more familiar with it from coursework. PostgreSQL works identically for everything below — just swap the driver dependency and connection URL. This guide uses MySQL; ask if you want the Postgres equivalents.

Install MySQL locally, or use a free hosted instance (PlanetScale, Railway, Supabase for Postgres, etc.) if you don't want to install it locally — either works, you just need a connection URL, username, and password.

Create a database:
```sql
CREATE DATABASE financetracker;
```

### 3.2 Add dependencies to `pom.xml`

Add these inside `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 3.3 Add DB connection settings

In `src/main/resources/application.properties`, add (pulling from `.env` the same way `jwt.secret` already does):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financetracker
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Auto-creates/updates tables from your @Entity classes on startup.
# Fine for dev. Never use "update" or "create" against production data.
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
Add `DB_USERNAME` and `DB_PASSWORD` to your local `.env` file (same file `JWT_SECRET` already lives in — it's git-ignored, so each teammate sets their own).

### 3.4 Turn `User` and `Expense` into real entities

This is the main code change. Add JPA annotations to the model classes — the fields stay the same, you're just telling Hibernate (the library behind JPA) how to map them to table columns.

**`User.java`** — before/after shape:
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    // JPA requires a no-args constructor — add one (can be protected)
    protected User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // keep the existing getters — but the fields can no longer be `final`,
    // since JPA needs to be able to set them via reflection when loading
    // a row back out of the database. Drop `final` from every field above.
}
```

Do the same for `Expense.java` — add `@Entity`, `@Id` + `@GeneratedValue` on `id`, `@Column` on the rest, a no-args constructor, and drop `final` from the fields. `ownerUsername` stays as a plain `String` column for now (a `@ManyToOne` relationship to `User` is a nice upgrade later, but not required to get this working).

### 3.5 Replace the Dummy repositories with Spring Data interfaces

This is the payoff of the layering — these two files shrink to almost nothing, because Spring Data JPA writes the implementation for you.

**Replace `DummyUserRepository.java`** with:
```java
package com.example.financetracker.repository;

import com.example.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```
No implementation needed — Spring Data generates the SQL from the method name at startup.

**Replace `DummyExpenseRepository.java`** with:
```java
package com.example.financetracker.repository;

import com.example.financetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOwnerUsername(String ownerUsername);
    Optional<Expense> findByIdAndOwnerUsername(Long id, String ownerUsername);
    void deleteByIdAndOwnerUsername(Long id, String ownerUsername);
}
```

### 3.6 Update `AuthService` and `ExpenseService` to match

The old Dummy repos had custom `save(...)` methods that took raw fields and built the object internally. `JpaRepository`'s `save()` takes the entity object itself instead. So in the services, you now construct the entity, then call `.save(entity)`:

```java
// AuthService.register(), old:
userRepository.save(request.getUsername(), request.getEmail(), hashedPassword);

// new:
User user = new User(request.getUsername(), request.getEmail(), hashedPassword);
userRepository.save(user);
```

Same pattern in `ExpenseService.create()` — build the `Expense` object, then `expenseRepository.save(expense)`.

Also rename every `expenseRepository.findAllByOwner(...)` / `findByIdAndOwner(...)` call to the new method names (`findAllByOwnerUsername` / `findByIdAndOwnerUsername`) to match the interface above.

`ExpenseService.delete()` currently checks a `boolean` return value from `deleteByIdAndOwner`. `JpaRepository`'s generated delete methods return `void`, so you'll need to check existence first:
```java
public void delete(String username, Long id) {
    Expense expense = findOwnedOrThrow(username, id); // reuses existing method — throws 404 if not found/not owned
    expenseRepository.delete(expense);
}
```

**Controllers should not need to change at all** — this is the check that tells you the layering worked correctly.

### 3.7 Test it

1. Start MySQL, run the app (`mvn spring-boot:run`), check the console — Hibernate should log `CREATE TABLE` statements for `users` and `expenses` on first run.
2. Re-run every cURL command from Part 1 — they should all behave identically to before.
3. Restart the app and confirm previously-created data is still there (this is the whole point — proves it's no longer in-memory).
4. Look at the tables directly (`mysql -u root -p financetracker` → `SELECT * FROM users;`) to sanity-check what's actually being stored.

### 3.8 Common issues

- **`Unknown database 'financetracker'`** — you need to `CREATE DATABASE` first; Spring won't do that part for you.
- **App starts but every request 500s with a Hibernate error** — usually a mismatch between an `@Column` type and what's actually in the table if you'd already created tables manually. Easiest fix in dev: drop the tables and let `ddl-auto=update` rebuild them.
- **`Access denied for user`** — double check `DB_USERNAME`/`DB_PASSWORD` in your local `.env`, and that the MySQL user actually has privileges on the `financetracker` database.

---

## 4. Frontend guide: connecting to this backend

This section covers everything a frontend needs to know to talk to this API: the auth flow, every endpoint with example calls, and one backend change you'll need before anything works from a browser.

### 4.1 One backend change you need first: CORS

By default, a browser blocks JavaScript on `http://localhost:3000` (or wherever your frontend runs) from calling `http://localhost:8080` (this backend) — that's the browser's CORS (Cross-Origin Resource Sharing) policy, and it applies no matter how correct your code is. cURL/Postman don't hit this because it's a browser-only restriction, which is why testing worked fine with them but won't from your app without this.

Ask whoever's on backend to add this to `SecurityConfig.java` (or add it yourself, it's a small change):

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000")); // your frontend's dev URL
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```
And add `.cors(cors -> cors.configurationSource(corsConfigurationSource()))` to the `http` chain in `securityFilterChain(...)`, alongside the existing `.csrf(...)` and `.sessionManagement(...)` lines.

Without this, every request from your frontend will fail in the browser console with a CORS error even though the exact same request works fine in Postman — if you see that, this is almost always why.

### 4.2 Base URL

Everything is under `http://localhost:8080` in dev (no version prefix, no `/api` on the auth routes — note `/auth/register` and `/auth/login` are *not* under `/api`, but everything else is under `/api/expenses`).

### 4.3 The auth flow — what your frontend needs to manage

1. **Register** a new user once, or **log in** an existing one. Both return either success or an error — login returns a **JWT token** on success.
2. **Store that token** somewhere the app can read it on every subsequent request — typically in memory (a variable/state) for the session, or `localStorage` if you want it to survive a page refresh (note: `localStorage` is readable by any JS on the page, which is a known XSS tradeoff — fine for a class project, worth knowing for later).
3. **Attach it to every request** that isn't register/login, as a header:

```
Authorization: Bearer YOUR_TOKEN_HERE
```

4. If a request comes back `401 Unauthorized`, the token is missing, expired (default 1 hour), or invalid — send the user back to the login screen.

There's no "logout" endpoint needed server-side, since the server doesn't track sessions — logging out on the frontend just means deleting the stored token.

### 4.4 Every endpoint, with example calls

Using `fetch` here; the same shapes work with `axios` if that's what you're using.

**Register**
```js
await fetch("http://localhost:8080/auth/register", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ username, email, password }),
});
// 201 Created on success
// 400 Bad Request, body "Username already taken" if the username exists
```

**Login**
```js
const res = await fetch("http://localhost:8080/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ username, password }),
});
const { token } = await res.json();
// save `token` — you need it for everything below
// 401 Unauthorized if username/password is wrong
```

**Create an expense**
```js
await fetch("http://localhost:8080/api/expenses", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
  },
  body: JSON.stringify({
    description: "Groceries",
    amount: 42.50,
    category: "Food",
    // date is optional — omit it and the backend defaults to today
  }),
});
// 201 Created, returns the created expense including its generated id
```

**List all your expenses**
```js
const res = await fetch("http://localhost:8080/api/expenses", {
  headers: { "Authorization": `Bearer ${token}` },
});
const expenses = await res.json();
// array of { id, description, amount, category, date }
```

**Get one expense**
```js
await fetch(`http://localhost:8080/api/expenses/${id}`, {
  headers: { "Authorization": `Bearer ${token}` },
});
// 404 if it doesn't exist OR belongs to someone else — you can't tell which, by design
```

**Update an expense (full replace — see warning)**
```js
await fetch(`http://localhost:8080/api/expenses/${id}`, {
  method: "PUT",
  headers: {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
  },
  body: JSON.stringify({
    description: "Groceries (updated)",
    amount: 45.00,
    category: "Food",
    date: "2026-09-16", // ISO format YYYY-MM-DD
  }),
});
```
**Important:** this is a full replace, not a partial patch. Always send all four fields — if your edit form only lets someone change the amount, you still need to send the existing `description`/`category`/`date` alongside it, or those fields get wiped. Pre-fill your edit form from the `GET` response so you always have the full object to send back.

**Delete an expense**
```js
await fetch(`http://localhost:8080/api/expenses/${id}`, {
  method: "DELETE",
  headers: { "Authorization": `Bearer ${token}` },
});
// 204 No Content on success
```

**Spending summary**
```js
const res = await fetch("http://localhost:8080/api/expenses/summary", {
  headers: { "Authorization": `Bearer ${token}` },
});
const { totalAmount, totalsByCategory } = await res.json();
// totalsByCategory is an object like { "Food": 87.50, "Transport": 20.00 }
```

### 4.5 Handling errors generically

Every error response from this backend follows Spring's default shape and an appropriate status code — you generally just need to branch on status:

| Status | Meaning | What to show the user |
|---|---|---|
| `400` | Bad request (e.g. duplicate username on register) | The error message from the response body |
| `401` | Not logged in / bad credentials / expired token | Send to login screen |
| `404` | Expense not found (or not yours) | "Expense not found" — don't distinguish "not yours" from "doesn't exist" |
| `500` | Something broke server-side | Generic "something went wrong," and flag it to backend |

A small helper wrapping `fetch` to auto-attach the token and redirect to login on `401` will save you from repeating this in every component.

### 4.6 Suggested order to build in

1. Register + login screens first, confirm you can store and see the token.
2. A protected "hello world" call to `/api/test` to prove the token round-trips correctly before building anything expense-related.
3. List expenses (read-only view) — simplest expense feature, good first real screen.
4. Create expense form.
5. Edit + delete.
6. Summary/dashboard view last, once create/list are solid — it's just another `GET` but the data shaping (totals by category) is easiest to sanity-check once you already have real expenses to look at.
