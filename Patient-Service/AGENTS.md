# AGENTS.md — Patient-Service AI Agent Guidance

> **Scope:** `Patient-Service/` microservice  
> **Purpose:** Service-level operational guide for AI subagents working on `Patient-Service`. Contains component layout, active REST contracts, Spring profiles, data initialization rules, and testing runbook.

---

## 1. Service Role & Boundaries

`Patient-Service` is the **authoritative source of truth** for patient records in `patient_db`.

- **Primary Responsibility**: Manages patient lifecycle (`Patient` entity), exposes REST API (`/api/v1/patients`), and publishes `PatientRegistered` domain events via the Outbox Pattern.
- **Strict Isolation**: Does NOT interact synchronously with other downstream microservices. All outbound communication is event-driven.

---

## 2. Package & Component Layout

```
Patient-Service/src/main/java/com/app/patient/patientservice/
├── PatientServiceApplication.java   ← @SpringBootApplication entry point
├── config/
│   └── JpaAuditingConfig.java        ← @EnableJpaAuditing, AuditorAware stub ("system")
├── controller/
│   └── PatientController.java        ← REST Controller @RequestMapping("/api/v1/patients")
├── dto/
│   ├── PatientRequest.java           ← Inbound DTO with Bean Validation (@NotBlank, @Email, @Past)
│   └── PatientResponse.java          ← Outbound DTO (exposes safe fields; internal audit omitted)
├── exception/
│   └── GlobalExceptionHandler.java   ← Global REST error handling
├── mapper/
│   └── PatientMapper.java            ← Entity ↔ DTO transformation utility
├── models/
│   └── Patient.java                  ← @Entity table=patients (UUID PK, JPA Auditing)
├── repository/
│   └── PatientRepository.java        ← JpaRepository (findByEmail, existsByEmail)
└── service/
    └── PatientService.java           ← Business logic layer (@Service, @Transactional)
```

---

## 3. REST API Endpoint Specifications

Base Path: `/api/v1/patients`

| HTTP Method | Endpoint Path | Query / Request Body | Response Status | Description |
| :--- | :--- | :--- | :--- | :--- |
| **GET** | `/` | None | `200 OK` | Retrieves list of all patient records (`List<PatientResponse>`). |
| **GET** | `/{id}` | Path variable `UUID id` | `200 OK` / `404 Not Found` | Retrieves single patient record by UUID. |
| **POST** | `/` | `@Valid @RequestBody PatientRequest` | `201 Created` | Creates new patient record. Returns `PatientResponse`. |
| **PUT** | `/update` | `@Valid @RequestBody PatientRequest` | `200 OK` | Updates existing patient matching `email`. Returns `PatientResponse`. |
| **DELETE** | `/` | `@RequestParam String email` | `204 No Content` | Deletes patient matching `email` query parameter. |

---

## 4. Spring Profiles & Database Configuration

| Profile | Datasource URL | Flyway | DDL-Auto | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `local` (default) | `jdbc:h2:mem:patient_db;MODE=PostgreSQL` | Disabled | `update` | Local development & manual testing (`http://localhost:4000/h2-console`). |
| `test` | `jdbc:h2:mem:patient_db;MODE=PostgreSQL` | Disabled | `update` | Isolated unit & slice tests (`@ActiveProfiles("test")`). |
| `prod` | PostgreSQL instance | Enabled | `validate` | Production deployment (Flyway manages migrations). |

> [!IMPORTANT]
> **Datasource Initialization Rule**:
> `spring.jpa.defer-datasource-initialization=true` MUST be present in `application-local.properties` to ensure `data.sql` executes **after** Hibernate creates the `patients` table schema.

---

## 5. Subagent Self-Review & Coding Checklist

Before making changes or declaring task completion inside `Patient-Service/`, subagents MUST check:

1. **DTO Protection**: Never expose the raw `Patient` JPA entity in controller responses. Always map through `PatientMapper` to `PatientResponse`.
2. **Transaction Management**: Annotate write methods in `PatientService` with `@Transactional`. Use `@Transactional(readOnly = true)` for read-only queries.
3. **Bean Validation**: Ensure incoming `PatientRequest` parameters are annotated with `@Valid`.
4. **Seed Data Integrity**: If entity fields change, update all 10 seed rows in `src/main/resources/data.sql` to match.
5. **Auditing Rules**: Do NOT manually overwrite `@CreatedBy`, `@CreationTimestamp`, `@LastModifiedBy`, or `@UpdateTimestamp` fields.

---

## 6. Verification Commands for Subagents

```bash
# Navigate to service directory
cd Patient-Service

# Run complete test suite (uses test profile automatically)
./mvnw clean test

# Run a specific test class
./mvnw test -Dtest=PatientRepositoryTest

# Run locally on local profile (H2 console at /h2-console)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
