# CONTEXT.md — Patient-Management-System

## Project Purpose
A microservice-based Patient Management System designed to store, audit, and propagate
patient records across downstream services using an event-driven architecture
(outbox pattern).

## Current State (Sprint 1 — Foundation)
Only the **Patient-Service** microservice is implemented so far.
It provides the core patient entity, JPA persistence, and an auditing layer.
REST controllers, the outbox pattern, and downstream consumers are planned for
future sprints.

## Repository Layout
```
Patient-Management-System/
├── Patient-Service/               ← Only active microservice
│   ├── src/main/
│   │   ├── java/com/app/patient/patientservice/
│   │   │   ├── PatientServiceApplication.java   ← @SpringBootApplication entry point
│   │   │   ├── config/
│   │   │   │   └── JpaAuditingConfig.java        ← @EnableJpaAuditing, AuditorAware
│   │   │   └── models/
│   │   │       └── Patient.java                  ← Core JPA entity (@Entity, UUID PK)
│   │   └── resources/
│   │       ├── application.properties            ← Base config (active profile = test)
│   │       ├── application-local.properties      ← H2 + local dev overrides
│   │       └── data.sql                          ← 10 seed patient records
│   └── src/test/
│       └── resources/
│           └── application-test.properties       ← H2 in-memory test config
└── README.md
```

## Key Design Decisions

| Decision | Rationale |
|---|---|
| UUID primary key (`@GeneratedValue(strategy = UUID)`) | Globally unique; safe for distributed/event-driven systems |
| JPA Auditing (`@CreatedBy`, `@UpdateTimestamp`, etc.) | Audit trail without repetitive boilerplate in every service |
| AuditorAware returns `"system"` | Placeholder; will be replaced by Cognito/Auth principal |
| H2 `MODE=PostgreSQL` in test/local | Lets tests run without Docker while staying syntax-compatible with prod Postgres |
| Flyway disabled in test/local, enabled in prod | Schema is owned by Flyway in prod; Hibernate `ddl-auto=update` only for dev convenience |
| Outbox pattern (planned) | Decouples patient write operations from downstream event publishing |
| `spring.jpa.defer-datasource-initialization=true` | Ensures `data.sql` runs **after** Hibernate DDL creates the schema |

## Active Spring Profiles

| Profile | Datasource | Flyway | Purpose |
|---|---|---|---|
| `test` (default) | H2 in-memory | disabled | Unit / integration tests |
| `local` | H2 in-memory | disabled | Local manual runs, H2 console |
| `prod` (planned) | PostgreSQL | enabled | Production |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Test DB | H2 (PostgreSQL-compatible mode) |
| Prod DB | PostgreSQL |
| Schema migration | Flyway (prod only) |
| Boilerplate reduction | Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| Build | Maven 3, `spring-boot-maven-plugin` |

## Upcoming Work
- [ ] Repository layer (`PatientRepository extends JpaRepository`)
- [ ] Service layer (`PatientService`)
- [ ] REST controller (`PatientController`) — CRUD endpoints
- [ ] DTOs + MapStruct mappers
- [ ] OutboxEvent entity + publisher
- [ ] Kafka / messaging consumer services
- [ ] Spring Security + Cognito integration (replaces `AuditorAware` stub)
- [ ] Flyway migration scripts (`V1__init.sql`, etc.)
- [ ] Docker Compose for local multi-service setup