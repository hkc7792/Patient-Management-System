# CONTEXT.md — Patient-Management-System (Mono-Repo)

> This is the **repo-level** context. For deep per-service detail, see
> `<service>/.gemini/CONTEXT.md` inside each microservice directory.

---

## System Purpose

Event-driven patient management platform built with Java 21 + Spring Boot + Kafka.

**Patient Service** is the authoritative source of truth for patient records.
All downstream services (Billing, Analytics, Notification) react **asynchronously**
to patient domain events via a Kafka topic — they never call Patient Service directly.

The **only synchronous leg** in the system is: `Client ↔ API Gateway ↔ Patient Service`.
Everything else is event-driven.

---

## Service Map

| Service                  | Status     | Port | Role                                                         |
|--------------------------|------------|------|--------------------------------------------------------------|
| **API Gateway**          | 🔲 Planned  | 80   | Auth + routing. The only sync client-facing entry point.     |
| **Auth Service**         | 🔲 Planned  | TBD  | Token validation on behalf of API Gateway.                   |
| **Patient Service**      | ✅ Active   | 4000 | Core patient data + DB. Publishes `PatientRegistered` event. |
| **Billing Service**      | 🔲 Planned  | TBD  | Consumes `PatientRegistered`, publishes `InvoiceCreated`.    |
| **Analytics Service**    | 🔲 Planned  | TBD  | Consumes all events (read-only, no publishing).              |
| **Notification Service** | 🔲 Planned  | TBD  | Consumes events, sends alerts.                               |

---

## Kafka Event Contracts

| Event                | Producer            | Consumers                              |
|----------------------|---------------------|----------------------------------------|
| `PatientRegistered`  | Patient Service     | Billing, Analytics, Notification       |
| `InvoiceCreated`     | Billing Service     | Analytics, Notification                |

**Note:** Billing is both a consumer (of `PatientRegistered`) and a producer
(of `InvoiceCreated`). Analytics and Notification are consumers only.

All events flow through a single shared Kafka topic (`Events`) using the
**Outbox Pattern** — no service ever writes directly to Kafka inside a business
transaction.

---

## Cross-Cutting Conventions

| Convention                        | Detail                                                                    |
|-----------------------------------|---------------------------------------------------------------------------|
| Language & Framework              | Java 21 + Spring Boot 4.x across all services                            |
| Primary keys                      | UUID everywhere — globally unique, safe for distributed systems           |
| Event publishing                  | Outbox Pattern only — write + OutboxEvent in one DB transaction, poller publishes |
| Database ownership                | Database-per-service — no service reads another service's DB              |
| Auth                              | AWS Cognito → JWT → Spring Security filter per service                    |
| Commit style                      | Conventional Commits (`feat`, `fix`, `docs`, `refactor`, `chore`, etc.)  |
| Code review workflow              | See `Patient-Service/.gemini/skills/code-review-commit-push/SKILL.md`    |
| Service documentation             | Every service has `.gemini/CONTEXT.md`, `.gemini/architecture.md`, `.gemini/skills/` |

---

## Repository Layout

```
Patient-Management-System/
├── .gemini/                                    ← Repo-wide context (YOU ARE HERE)
│   ├── CONTEXT.md                              ← This file
│   ├── architecture.md                         ← Full system Mermaid diagram + event contracts
│   └── skills/
│       └── add-new-microservice/
│           └── SKILL.md                        ← Runbook: how to scaffold a new service
│
├── Patient-Service/                            ← ✅ Active microservice
│   ├── .gemini/
│   │   ├── CONTEXT.md                          ← Patient-Service-specific context
│   │   ├── architecture.md                     ← Patient-Service internal architecture
│   │   └── skills/
│   │       ├── code-review-commit-push/SKILL.md ← Git workflow (shared across services)
│   │       └── patient-service-dev/SKILL.md    ← Daily dev loop for Patient-Service
│   └── src/...
│
├── event_driven_patient_management_architecture.png  ← Architecture reference image
└── README.md
```

---

## Tech Stack (Cross-Service)

| Layer             | Technology                                    |
|-------------------|-----------------------------------------------|
| Language          | Java 21                                       |
| Framework         | Spring Boot 4.x                               |
| Event Bus         | Apache Kafka (AWS MSK in prod)                |
| Event Publishing  | Outbox Pattern (Debezium CDC / scheduled poller) |
| Auth              | AWS Cognito + JWT (Spring Security)           |
| Prod DB           | PostgreSQL (per service)                      |
| Test/Local DB     | H2 in-memory (PostgreSQL-compatible mode)     |
| Schema Migration  | Flyway (prod) / Hibernate DDL (dev/test)      |
| Build             | Maven 3 + Spring Boot Maven Plugin            |
| Boilerplate       | Lombok                                        |
| API Gateway       | Spring Cloud Gateway (planned)                |

---

## Upcoming Work (System Level)

- [ ] API Gateway service (Spring Cloud Gateway + JWT filter)
- [ ] Auth Service integration (Cognito token validation)
- [ ] Billing Service (Kafka consumer + `InvoiceCreated` publisher)
- [ ] Analytics Service (Kafka consumer, read-only aggregations)
- [ ] Notification Service (Kafka consumer + email/SMS alerts)
- [ ] Docker Compose for local multi-service dev environment
- [ ] Shared Kafka topic schema registry (Avro / Schema Registry)
- [ ] Central observability stack (distributed tracing, metrics)
