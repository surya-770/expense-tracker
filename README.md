# Ledgerly Personal Expense Tracker

A full stack personal finance tracker being built in phases from the supplied project brief. The current deliverable completes **Phases 1-8: foundation, secure accounts, categories and transactions, dynamic dashboard, budgets, savings goals, interest tools, reports, and CSV exports**. No sample balances are presented as real data.

## Implemented so far

- Java 21 / Spring Boot 3 application with REST health endpoint.
- HTML/CSS/ES6 frontend using Bootstrap 5.3.8 and Chart.js 4.6.1.
- MySQL 8 container configuration and environment based credentials.
- Flyway migrations for users, categories, transactions, budgets, savings and interest history, including shared default categories.
- Hibernate schema validation (Flyway owns schema changes).
- Responsive frontend navigation shell and data-connected dashboard.
- Browser frontend service check against `/api/health`.
- Email registration and login with BCrypt password hashing and authenticated server sessions.
- CSRF protection for state-changing API calls and credentialed CORS for configured frontend origins.
- Protected profile read/update and password change endpoints; identity comes from the authenticated principal.
- Login and registration interface plus working profile form in the frontend.
- Responsive split-screen login and registration dialog with animated SVG financial artwork, staggered form entry, and reduced-motion support.
- Authentication service unit tests.
- Shared default and user-specific categories with guarded edit/delete behavior.
- User-scoped transaction create, read, update, delete, search, filtering, sorting, and pagination APIs.
- Connected Transactions, Income, Expenses, and Categories screens.
- Dashboard summaries, recent transactions, six-month trends, and category spending distributions aggregated from authenticated-user records.
- Interactive Chart.js cash-flow, expense distribution, and six-month trend charts.
- Monthly overall budgets and optional category allocations, with server validation, duplicate prevention, and live actual-spend monitoring.
- Overall and category warning states at 80% utilization and over-budget states at 100%.
- Savings goals with user-owned contribution history, contribution edits, progress calculations, and completion status. Contributions remain separate from income and expenses.
- Simple and compound interest calculations with high precision decimal math, fractional-period compounding, side-by-side comparison, saved history, a growth chart, and CSV export.
- Monthly, annual, category, budget, savings, and custom date-range reports with interactive charts and authenticated CSV downloads.
- Transaction, income, expense, report, and interest history exports with UTF-8 CSV escaping and formula-injection protection.
- Service unit tests and a MockMvc/H2 integration test for registration, authenticated sessions, and cross-user record isolation.

## Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop with Docker Compose (or an existing MySQL 8 instance)
- A static file server for `frontend/` (needed for browser API calls)

## Supabase setup (new frontend foundation)

1. Copy `frontend/assets/js/config.example.js` to `frontend/assets/js/config.js` and enter the project URL and publishable/anon key from Supabase Project Settings → API.
2. Run `supabase/schema.sql` once in the Supabase SQL Editor. It creates the tables, profile signup trigger, starter categories, and row-level security policies.
3. Serve the frontend and confirm the footer says **Supabase configured**.

Email signup, sign-in, session restore, and sign-out use Supabase Auth. Categories, transactions, budgets, dashboard analytics, reports, and saved interest calculations now use Supabase Postgres with row-level security. Run `supabase/schema.sql` first to create and seed the tables. Savings-goal management and profile updates still use Spring Boot/MySQL. Never use a service-role key in the browser.

## Deploy the frontend to Vercel

The frontend is plain HTML/CSS/JavaScript and needs no Vite build. `vercel.json` disables the stale `vite build` command and serves the `frontend/` folder directly. Commit and push both `vercel.json` files to the connected GitHub branch to trigger a new deployment. The root config supports a repository-root deployment; the frontend config supports setting Vercel's Root Directory to `frontend`.

## Run locally

1. Copy `.env.example` to `.env` and set a private `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD` for local use. Compose reads `.env` automatically.
2. Start MySQL: `docker compose up -d mysql`
3. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your shell to match the database, then start the API:
   - PowerShell: `$env:DB_URL='jdbc:mysql://localhost:3306/expense_tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'; $env:DB_USERNAME='expense_app'; $env:DB_PASSWORD='your-local-password'; mvn spring-boot:run`
   - Or use Maven with equivalent environment variables on macOS/Linux.
4. Confirm `http://localhost:8080/api/health` returns JSON with `status: ok`.
5. Serve the frontend folder, for example `npx serve frontend -l 5500`, then open the displayed local URL. Allowed development origins are configurable with `APP_CORS_ORIGINS`.

If using a separately managed MySQL database, create `expense_tracker` and a least-privilege application user with schema migration privileges before starting the backend. Flyway applies `src/main/resources/db/migration` on startup.

## Project layout

```text
personal-expense-tracker/
├── frontend/                 # Responsive web client
├── src/main/java/com/expensetracker/
│   ├── controller/            # REST endpoints
│   ├── dto/                   # Validated API contracts
│   ├── entity/                # JPA persistence entities
│   ├── repository/            # Spring Data repositories
│   ├── security/              # Authentication (Phase 2)
│   └── service/               # Business logic
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/          # Flyway schema history
├── docker-compose.yml
└── pom.xml
```

## API (current)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | API liveness and application identifier |
| GET | `/api/auth/csrf` | Issue/read the CSRF token before state-changing requests |
| POST | `/api/auth/register` | Register (fullName, email, password, confirmPassword, currency) and start a session |
| POST | `/api/auth/login` | Authenticate (email, password) and start a session |
| POST | `/api/auth/logout` | End the authenticated session |
| GET | `/api/profile` | Read the signed-in user's profile |
| PUT | `/api/profile` | Update fullName and currency |
| PUT | `/api/profile/password` | Change password after current-password verification |
| GET | `/api/categories` | List shared defaults and current-user categories |
| POST | `/api/categories` | Create a user-specific category |
| PUT | `/api/categories/{id}` | Edit an owned custom category |
| DELETE | `/api/categories/{id}` | Delete an unused owned custom category |
| GET | `/api/transactions` | Search and page user-owned records; filters: type, categoryId, paymentMethod, startDate, endDate, search; sorts: date, amount, category |
| POST | `/api/transactions` | Create income or expense record |
| GET | `/api/transactions/{id}` | Read one owned record |
| PUT | `/api/transactions/{id}` | Edit an owned record |
| DELETE | `/api/transactions/{id}` | Delete an owned record |
| GET | `/api/dashboard/summary` | Read selected-month income/expenses and all-time net balance |
| GET | `/api/dashboard/charts` | Read six-month cash-flow trend and selected-month category distribution |
| GET | `/api/dashboard/recent-transactions` | Read the latest 1-20 signed-in-user records |
| GET | `/api/budgets` | List the signed-in user's plans, optionally for a year |
| POST | `/api/budgets` | Create a unique monthly plan and category allocations |
| GET | `/api/budgets/{id}` | Read a plan and actual spend |
| PUT | `/api/budgets/{id}` | Update the plan for its existing month |
| GET | `/api/budgets/monitoring` | Monitor overall and category budget status for a month |
| GET | `/api/savings-goals` | List goals with contribution history and progress |
| POST | `/api/savings-goals` | Create a goal |
| GET | `/api/savings-goals/{id}` | Read one owned goal |
| PUT | `/api/savings-goals/{id}` | Edit an owned goal |
| PUT | `/api/savings-goals/{id}/status` | Mark a goal active or complete |
| DELETE | `/api/savings-goals/{id}` | Delete a goal and its contribution ledger |
| GET | `/api/savings-goals/{id}/contributions` | Read contribution history |
| POST | `/api/savings-goals/{id}/contributions` | Add a contribution |
| PUT | `/api/savings-goals/{id}/contributions/{contributionId}` | Edit a contribution |
| DELETE | `/api/savings-goals/{id}/contributions/{contributionId}` | Delete a contribution |
| POST | `/api/interest/simple` | Calculate simple interest and projected growth |
| POST | `/api/interest/compound` | Calculate compound interest and projected growth |
| GET | `/api/interest/history` | Read saved calculations |
| POST | `/api/interest/history` | Recalculate and save a calculation |
| DELETE | `/api/interest/history/{id}` | Delete an owned calculation |
| GET | `/api/interest/history/export` | Export the current user's calculations as CSV |
| GET | `/api/reports/monthly` | Selected month actuals and balance |
| GET | `/api/reports/yearly` | Annual income, expenses, net, and month breakdown |
| GET | `/api/reports/categories` | Expense totals by category for a date range |
| GET | `/api/reports/budget` | Budget versus actual report |
| GET | `/api/reports/savings` | Savings goal progress report |
| GET | `/api/reports/custom` | Filtered records and totals for a date range |
| GET | `/api/reports/export?type=...` | CSV for monthly, yearly, category, budget, savings, income, expense, or transaction reports |
| GET | `/api/transactions/export` | Filtered transaction history CSV |

All modifying API requests require the `X-XSRF-TOKEN` header matching the `XSRF-TOKEN` cookie. The frontend API service handles this automatically. Send session cookies with `credentials: include`. Use the same hostname (`localhost`) for both the frontend and API during local development so browser cookie same-site rules permit the session.

## Database design

Migration V1 creates user-owned financial records and indexes for user/date lookups. Money uses `DECIMAL(19,2)`, emails and monthly plans have uniqueness constraints, allocations prevent duplicate categories per plan, and contributions remain a separate ledger rather than being classified as income or expense. `user_id` is never accepted as an ownership authority from clients; authenticated ownership enforcement is implemented with Phase 2 APIs.

## Planned build phases

1. Project initialization and database architecture — **complete**
2. Registration, login, security, profile and user isolation — **complete**
3. Categories, income, expenses and transaction history — **complete**
4. Dashboard and financial analytics — **complete**
5. Budget planner and monitoring — **complete**
6. Savings goals and contributions — **complete**
7. Interest calculator — **complete**
8. Reports, charts and CSV exports — **complete**
9. Testing, validation, error handling and UI refinement — **complete**
10. Final integration and documentation — **complete**

Build command: `mvn package`. Run tests with `mvn test`. The suite includes an H2-backed API integration test. Docker is unavailable in this environment, so MySQL itself could not be started here. Maven used a workspace-local dependency cache because the default user Maven cache was inaccessible.
