# CONTEXT.md — Patient-Service

> **Scope:** This file covers Patient-Service internals only.
> For the full system context (all 6 services, Kafka event contracts, cross-cutting
> conventions), see the repo root: `.gemini/CONTEXT.md`

## Service Purpose
Patient-Service is the **authoritative source of truth** for patient records in the
Patient-Management-System. It owns the `patient_db`, persists patient entities with
full JPA auditing, and will publish `PatientRegistered` events via the Outbox Pattern
to the Kafka event bus.

## Current State (Active Implementation)
The core Patient-Service features are implemented:
- **JPA Entity & Auditing**: `Patient` with UUID PK and `@EnableJpaAuditing` (`created_by`, `created_at`, `updated_by`, `updated_at`).
- **Data Access Layer**: `PatientRepository` extending `JpaRepository` with custom `findByEmail` and `existsByEmail`.
- **Service Layer**: `PatientService` handling CRUD operations and business validation.
- **DTOs & Mapping**: `PatientRequest`, `PatientResponse` records and `PatientMapper` utility.
- **REST Controller**: `PatientController` at `/api/v1/patients` for GET (all / by ID), POST (create), PUT (`/update`), and DELETE (`?email=...`).
- **Global Error Handling**: `GlobalExceptionHandler` mapping validation and runtime exceptions.

## Repository Layout (Patient-Service)
```
Patient-Service/
├── src/main/
│   ├── java/com/app/patient/patientservice/
│   │   ├── PatientServiceApplication.java   ← @SpringBootApplication entry point
│   │   ├── config/
│   │   │   └── JpaAuditingConfig.java        ← @EnableJpaAuditing, AuditorAware stub
│   │   ├── controller/
│   │   │   └── PatientController.java        ← REST endpoints (/api/v1/patients)
│   │   ├── dto/
│   │   │   ├── PatientRequest.java           ← Inbound DTO with validation
│   │   │   └── PatientResponse.java          ← Outbound DTO
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java   ← Exception handler
│   │   ├── mapper/
│   │   │   └── PatientMapper.java            ← Entity ↔ DTO mapper
│   │   ├── models/
│   │   │   └── Patient.java                  ← Core JPA entity (@Entity, UUID PK)
│   │   ├── repository/
│   │   │   └── PatientRepository.java        ← Spring Data JPA repository
│   │   └── service/
│   │       └── PatientService.java           ← Business logic (@Service, @Transactional)
│   └── resources/
│       ├── application.properties            ← Base config (active profile = local)
│       ├── application-local.properties      ← H2 + local dev overrides
│       └── data.sql                          ← 10 seed patient records
└── src/test/
    ├── java/com/app/patient/patientservice/
    │   ├── PatientServiceApplicationTests.java
    │   ├── mapper/PatientMapperTest.java
    │   └── repository/PatientRepositoryTest.java
    └── resources/
        └── application-test.properties       ← H2 in-memory test config
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
| `local` (default) | H2 in-memory | disabled | Local manual runs, H2 console, default dev loop |
| `test` | H2 in-memory | disabled | Unit / integration tests (`@ActiveProfiles("test")`) |
| `prod` (planned) | PostgreSQL | enabled | Production |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Persistence | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Test DB | H2 (PostgreSQL-compatible mode) |
| Prod DB | PostgreSQL |
| Schema migration | Flyway (prod only) |
| Boilerplate reduction | Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) |
| Build | Maven 3, `spring-boot-maven-plugin` |

## Upcoming Work
- [x] Repository layer (`PatientRepository extends JpaRepository`)
- [x] Service layer (`PatientService`)
- [x] REST controller (`PatientController`) — CRUD endpoints
- [x] DTOs + Mappers (`PatientRequest`, `PatientResponse`, `PatientMapper`)
- [ ] OutboxEvent entity + publisher
- [ ] Kafka / messaging consumer services
- [ ] Spring Security + Cognito integration (replaces `AuditorAware` stub)
- [ ] Flyway migration scripts (`V1__init.sql`, etc.)
- [ ] Docker Compose for local multi-service setup