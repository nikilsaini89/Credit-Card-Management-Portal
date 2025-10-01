# SmartSwipe — Credit Card Management Platform

## Short summary

SmartSwipe is a full-stack web app for managing credit cards and Buy-Now-Pay-Later (BNPL) plans: users can view/manage cards, apply for new cards, simulate transactions (regular or BNPL), pay EMIs and export reports; admins can review applications, manage users and monitor transactions.

## Table of contents

- [Project at-a-glance](#project-at-a-glance)
- [User view (what users can do)](#user-view-what-users-can-do)
- [Admin view (admin features)](#admin-view-admin-features)
- [Tech stack & tools](#tech-stack--tools)
- [Folder structure (high level)](#folder-structure-high-level)
- [Database schema (summary)](#database-schema-summary)
- [Setup & run (dev)](#setup--run-dev)
- [Testing strategy](#testing-strategy)
- [API highlights](#api-highlights)
- [Team & ownership](#team--ownership)
- [Notes & next steps](#notes--next-steps)

## Project at-a-glance

A Vue 3 + TypeScript front end with a Spring Boot (Java 21) REST API backend and PostgreSQL production database. H2 is used for local/testing. Docker + Docker Compose are used for containerization.

## User view (what users can do)

**Auth:** register / login (JWT), token refresh and logout.

**Dashboard:** overview of active cards, recent transactions and BNPL summary.

**Card management:** view cards (masked card numbers), toggle status (ACTIVE/BLOCKED), request credit limit changes.

**Apply for card:** fill application (annual income, employment, requested limit) and track status (PENDING → APPROVED/REJECTED).

**Transactions:** create simulated transactions (regular or BNPL), view paginated transaction history, filter/export as PDF.

**BNPL:** choose tenure (3,6,9,12 months), view installment schedule, make installment payments and track progress.

**Profile:** update address, income and BNPL eligibility.

## Admin view (admin features)

**Admin dashboard:** metrics (active users, total transactions, pending applications).

**Application review:** view pending card applications, approve/reject with audit trail.

**User & transaction oversight:** view/disable user accounts, inspect suspicious transactions, view system analytics.

Role-based access control (RBAC) enforced via Spring Security and JWT

## Tech stack & tools

### Backend
- Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA/Hibernate, Maven, Lombok.

### Frontend
- Vue 3 + TypeScript, Vite, Vuex, Vue Router, Tailwind CSS, Axios.

### Database & infra
- PostgreSQL 15.x (prod), H2 (dev/tests)

### Testing
- Backend: JUnit 5, Mockito. 
- Frontend: Vitest + Vue Test Utils + JSDOM. 
- E2E: Cypress.

## Folder structure (high level)

(Adapted from the repo layout / IDE screenshot)

```
frontend/
├── src/
│   ├── assets/
│   ├── components/
│   ├── constants/
│   ├── model/
│   ├── router/
│   ├── services/
│   ├── store/
│   ├── views/
│   ├── utils/
│   ├── App.vue
│   └── main.ts
├── .env
├── package.json
└── vite.config.ts

backend/
├── src/
│   └── main/
│       └── java/com/ccms/portal/
│           ├── config/
│           ├── controller/
│           ├── dto/ (request/response)
│           ├── entity/
│           ├── enums/
│           ├── exception/
│           ├── repository/
│           ├── service/
│           └── util/
│       └── resources/
│           ├── application.properties/application.yml
│           └── log4j2.xml
├── pom.xml
└── mvnw
```

There is a standard separation: controllers → services → repositories → entities.

## Database schema (summary)

Main tables (normalized) with core fields:

**User**
- id, name, email (unique), phoneNumber, password (BCrypt), role, createdAt, updatedAt.

**UserProfile**
- id, userId → User, fullName, email, phone, address, annualIncome, isEligibleForBnpl, createdAt, updatedAt.

**Card**
- id, cardNumber (masked in UI), cardTypeId, status (ACTIVE/BLOCKED/INACTIVE), creditLimit, availableLimit, expiryDate, userId.

**CardApplication**
- id, userId, cardTypeId, requestedLimit, annualIncome, employmentStatus, applicationDate, status, reviewedBy.

**Transaction**
- id, cardId, merchantId, amount, transactionDate, category, isBnpl, createdAt.

**BnplInstallment**
- id, transactionId, installmentNumber, amount, dueDate, status (PENDING/PAID/OVERDUE), paymentDate, paymentMethod.

**Relationships**
- User → Cards (1:N), Card → Transactions (1:N), Transaction → BNPL Installments (1:N), User → UserProfile (1:1).

## Setup & run (dev)

### Prereqs
- Java 21, Maven 3.9+, Node.js 18+, npm/yarn, PostgreSQL 15 (or use Docker).

### Backend (dev)
```bash
# clone repo
git clone <repo-url>
cd credit-card-management-portal-backend

# configure src/main/resources/application.yml with DB creds and JWT secret
# example keys: spring.datasource.url=jdbc:postgresql://<host>:5432/smartswipe

mvn clean install
mvn spring-boot:run
```

Healthcheck: http://localhost:8080/actuator/health. Example application.yml snippets and env patterns are included in the project docs.

### Frontend (dev)
```bash
cd smartswipe-frontend
npm install

# set VITE_API_BASE_URL in .env to backend (eg. http://localhost:8080)

npm run dev
# default Vite URL e.g. http://localhost:5173
```

### DB
Create PostgreSQL DB `credit_card_portal` and set credentials in backend application.yml. For dev/tests you can use H2 (in-memory).

## Testing strategy

**Backend unit tests:** `mvn test` (JUnit 5 + Mockito). Aim: 80%+ coverage on service & controller logic.

**Frontend unit tests:** `npm run test` (Vitest + Vue Test Utils).

**E2E:** Cypress test suite covering key flows (login → create BNPL txn → pay EMI). Use H2 or a test Postgres instance for isolation. Test automation is intended to be integrated into CI.

## API highlights

(Full list lives in project docs — these are the main groups)

**Auth:** 
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/logout

**Cards:** 
- GET /api/cards
- PUT /api/cards/{cardId}/status
- PUT /api/cards/limit

**Applications:** 
- POST /api/card-applications
- GET /api/card-applications/my-applications
- admin review endpoints

**Transactions/BNPL:** 
- POST /api/transactions
- GET /api/transactions
- GET /api/bnpl/overview/{cardId}
- POST /api/bnpl/payment

**Analytics/Statements:** endpoints for spending analytics and statements export.

## Team & ownership

- **Abhay Dhek** — Team lead
- **Anchal Jakhar** — JWT Authentication and user profile management.
- **Nikil Saini** — My Cards Section and Admin Dashboard
- **Sumit Negi** — Apply Cards and Cards history
- **Saurav Kumar** — Transaction simulation and BNPL.

## Notes & next steps

**Security:** JWT is in place; protect secrets (JWT_SECRET, DB passwords) via env or secret manager in prod.

**Performance:** pagination + caching recommended for transaction endpoints.

**Future enhancements:** MFA, multi-currency, advanced fraud analytics (documented in appendix).