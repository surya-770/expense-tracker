# API reference

Base URL: `http://localhost:8080/api`. JSON requests and responses use UTF-8. All financial data endpoints require the authenticated server session.

## Session and CSRF

1. Call `GET /auth/csrf`; the server sets `XSRF-TOKEN` and returns the same value as JSON.
2. Send the cookie and `X-XSRF-TOKEN` header on every POST, PUT, PATCH, and DELETE request.
3. The frontend uses `credentials: include` so the session cookie is sent.
4. Call `POST /auth/logout` to invalidate the session.

Registration and login also require CSRF. Passwords must be 10-72 characters and include uppercase and lowercase letters, a number, and a symbol. Passwords are stored using BCrypt. The frontend CORS origins are configured with `APP_CORS_ORIGINS`.

## Authentication and profile

| Method and path | Request | Result |
|---|---|---|
| `GET /health` | — | Service health |
| `GET /auth/csrf` | — | CSRF token |
| `POST /auth/register` | `{fullName,email,password,confirmPassword,currency}` | Creates account and starts session |
| `POST /auth/login` | `{email,password}` | Starts session |
| `POST /auth/logout` | — | Ends session |
| `GET /profile` | — | Authenticated profile |
| `PUT /profile` | `{fullName,currency}` | Updated profile |
| `PUT /profile/password` | `{currentPassword,newPassword,confirmPassword}` | `204` when changed |

## Categories and transactions

Category type is `INCOME` or `EXPENSE`. Shared default categories are available to every account; custom categories are private. Deleting a custom category referenced by transactions is rejected.

| Method and path | Request or query | Result |
|---|---|---|
| `GET /categories` | — | Shared plus owned categories |
| `POST /categories` | `{type,name,icon,color}` | Create custom category |
| `PUT /categories/{id}` | `{type,name,icon,color}` | Edit custom category |
| `DELETE /categories/{id}` | — | Delete unused custom category |
| `GET /transactions` | `type,categoryId,paymentMethod,startDate,endDate,search,page,size,sortBy,direction` | Page of owned transactions |
| `POST /transactions` | `{type,amount,date,categoryId,source,paymentMethod,description}` | Create transaction |
| `GET /transactions/{id}` | — | Owned transaction |
| `PUT /transactions/{id}` | Same fields as create | Update owned transaction |
| `DELETE /transactions/{id}` | — | Delete owned transaction |
| `GET /transactions/export` | Same filters as list | UTF-8 CSV attachment |

Amounts must be positive and have at most two fractional digits. Income requires a source. Expenses require a payment method (`CASH`, `UPI`, `DEBIT_CARD`, `CREDIT_CARD`, `NET_BANKING`, or `OTHER`). Category type must match transaction type. Page size is limited to 100. Sort fields are `date`, `amount`, and `category`.

## Dashboard

| Method and path | Query | Result |
|---|---|---|
| `GET /dashboard/summary` | Optional `month`, `year` | Selected month totals, all-time balance, budget remaining, active savings-goal count |
| `GET /dashboard/charts` | Optional `month`, `year` | Six-month trend and selected-month expense distribution |
| `GET /dashboard/recent-transactions` | `limit` from 1 to 20 | Latest owned records |

Net balance is all-time income less all-time expenses. Monthly net cash flow is selected-month income less expenses. Budget remaining and savings contributions are separate values.

## Budgets

| Method and path | Request or query | Result |
|---|---|---|
| `GET /budgets` | Optional `year` | Owned plans |
| `POST /budgets` | `{month,year,totalBudget,allocations:[{categoryId,plannedAmount}]}` | Create unique monthly plan |
| `GET /budgets/{id}` | — | Plan plus actual spending |
| `PUT /budgets/{id}` | Same fields as create | Update the same plan month |
| `GET /budgets/monitoring` | Optional `month`, `year` | Overall and category utilization/status |

Category allocations must use expense categories, cannot repeat a category, and cannot sum above the overall budget. Utilization below 80% is normal, from 80% to under 100% is a warning, and 100% or above is over budget.

## Savings

| Method and path | Request | Result |
|---|---|---|
| `GET /savings-goals` | — | Owned goals, contributions, and progress |
| `POST /savings-goals` | `{name,targetAmount,targetDate,description}` | Create goal |
| `GET /savings-goals/{id}` | — | Owned goal |
| `PUT /savings-goals/{id}` | Same fields as create | Update goal |
| `PUT /savings-goals/{id}/status` | `{status:ACTIVE\|COMPLETED}` | Update status |
| `DELETE /savings-goals/{id}` | — | Delete goal and its ledger |
| `GET /savings-goals/{id}/contributions` | — | Contribution history |
| `POST /savings-goals/{id}/contributions` | `{amount,date,description}` | Add separate contribution |
| `PUT /savings-goals/{id}/contributions/{contributionId}` | Same fields | Update owned contribution |
| `DELETE /savings-goals/{id}/contributions/{contributionId}` | — | Delete owned contribution |

Progress is contribution total divided by target. Contributions are not recorded as income or expenses.

## Interest

Calculator request fields: `{principal,annualRate,duration,durationUnit,compoundingFrequency}`. `durationUnit` is `YEARS` or `MONTHS`; `compoundingFrequency` is `YEARLY`, `HALF_YEARLY`, `QUARTERLY`, or `MONTHLY`. Compound mode requires a frequency; simple mode ignores it. The calculator supports up to 100 years and rounds currency to two decimal places using half-up rounding.

| Method and path | Request | Result |
|---|---|---|
| `POST /interest/simple` | Calculator request | Estimate plus growth points |
| `POST /interest/compound` | Calculator request | Estimate plus growth points |
| `GET /interest/history` | `page`, `size` | Saved owned calculations |
| `POST /interest/history` | `{type:SIMPLE\|COMPOUND,calculation:{...}}` | Recalculates and saves |
| `DELETE /interest/history/{id}` | — | Delete owned calculation |
| `GET /interest/history/export` | — | Current-user CSV attachment |

## Reports and exports

| Method and path | Query | Result |
|---|---|---|
| `GET /reports/monthly` | Optional `year`, `month` | Selected-month financial summary |
| `GET /reports/yearly` | Optional `year` | Annual totals and monthly trend |
| `GET /reports/categories` | Required `startDate`, `endDate` | Expense totals per category |
| `GET /reports/budget` | Optional `year`, `month` | Budget vs actual |
| `GET /reports/savings` | — | Goal progress |
| `GET /reports/custom` | Required date range; optional `type`, `categoryId`, `paymentMethod` | Matching transactions and totals |
| `GET /reports/export` | `type` (`monthly`, `yearly`, `categories`, `budget`, `savings`, `income`, `expenses`, `transactions`), optional period/date filters | CSV attachment |

CSV is generated on the backend from the authenticated account's records, uses UTF-8 with a BOM, quotes and escapes cells, and guards spreadsheet formula cells.
