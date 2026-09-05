---
name: patient-service-dev
description: >-
  Daily development loop runbook for Patient-Service. Covers how to start
  the service locally, add new REST endpoints, add entity fields, write and
  run tests, work with Flyway migrations, and debug common issues (audit
  fields, H2 console, profile selection). Activate when developing inside
  Patient-Service.
---

# SKILL.md — Patient-Service Dev Loop

> **Skill**: `patient-service-dev`
> **Scope**: `Patient-Service/`
> **Purpose**: Everything a developer needs to know to work productively on
> Patient-Service day-to-day, from starting the app to shipping a new endpoint.

---

## Overview

```
Start local → Inspect data (H2) → Add feature
→ Write tests → Run tests → Self-review (code-review-commit-push skill) → Commit
```

---

## 1. Starting the Service Locally

### Profile: `local` (recommended for manual runs)
```bash
cd Patient-Service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
- Uses **H2 in-memory** database (`jdbc:h2:mem:patient_db`)
- Runs Hibernate `ddl-auto=update` — schema auto-created from entities
- Loads `data.sql` seed data (10 patient records)
- Enables **H2 Console** at `http://localhost:4000/h2-console`
- Flyway is **disabled**

### H2 Console Access
```
URL:      http://localhost:4000/h2-console
JDBC URL: jdbc:h2:mem:patient_db
User:     sa
Password: (empty)
```

### Profile: `test` (used automatically by `mvn test`)
- Same H2 config, no H2 console, used only for automated tests.
- Never run the app on `test` profile manually.

### Profile: `prod` (future)
- PostgreSQL datasource required via environment variables.
- Flyway enabled — schema managed by migration scripts.
- Never use `ddl-auto=update` in prod.

---

## 2. Adding a New REST Endpoint

Follow this checklist **in order** (dependencies flow top-down):

### a. Request & Response DTOs
```java
// src/main/java/.../dto/PatientRequestDTO.java
public record PatientRequestDTO(
    @NotBlank String name,
    @Email @NotBlank String email,
    String phone,
    String address,
    @Past LocalDate birthDate
) {}

// src/main/java/.../dto/PatientResponseDTO.java
public record PatientResponseDTO(
    UUID id, String name, String email,
    String phone, String address,
    LocalDate birthDate, LocalDate regDate,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {}
```
> **Rule:** Never expose the `Patient` entity directly in an API response.
> Always use DTOs to decouple API contract from DB schema.

### b. Mapper (manual until MapStruct is added)
```java
// src/main/java/.../mapper/PatientMapper.java
public class PatientMapper {
    public static Patient toEntity(PatientRequestDTO dto) {
        return Patient.builder()
            .name(dto.name())
            .email(dto.email())
            // ...
            .build();
    }

    public static PatientResponseDTO toDTO(Patient patient) {
        return new PatientResponseDTO(patient.getId(), ...);
    }
}
```

### c. Repository
```java
// src/main/java/.../repository/PatientRepository.java
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByEmail(String email);
}
```

### d. Service Layer
```java
// src/main/java/.../service/PatientService.java
@Service
@Transactional          // ← required on all methods that write to DB
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientResponseDTO createPatient(PatientRequestDTO dto) {
        Patient patient = PatientMapper.toEntity(dto);
        Patient saved = patientRepository.save(patient);
        return PatientMapper.toDTO(saved);
    }
}
```
> **Rule:** `@Transactional` on all write methods in the service layer.
> Read-only methods can use `@Transactional(readOnly = true)`.

### e. Controller
```java
// src/main/java/.../controller/PatientController.java
@RestController
@RequestMapping("/api/v1/patients")
@Validated
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponseDTO createPatient(@Valid @RequestBody PatientRequestDTO dto) {
        return patientService.createPatient(dto);
    }

    @GetMapping("/{id}")
    public PatientResponseDTO getPatient(@PathVariable UUID id) {
        return patientService.getPatient(id);
    }
}
```

### f. Tests (see Section 4 for full test patterns)

### g. Update `architecture.md`
Add the new endpoint to the data flow section in `Patient-Service/.gemini/architecture.md`.

---

## 3. Adding a New Field to the `Patient` Entity

Checklist — do **all** steps, in order:

- [ ] Add field to `Patient.java` with proper JPA constraints (`nullable`, `length`, `updatable`)
      and Bean Validation annotations (`@NotBlank`, `@Email`, `@Past`, etc.)
- [ ] Add the column to `data.sql` seed rows (all 10 rows must include the new column)
- [ ] Add field to `PatientRequestDTO` and `PatientResponseDTO`
- [ ] Update `PatientMapper` to map the new field
- [ ] Create Flyway migration for prod:
      `src/main/resources/db/migration/V<n>__add_<fieldname>_to_patients.sql`
      ```sql
      ALTER TABLE patients ADD COLUMN <fieldname> VARCHAR(100);
      ```
- [ ] Update any `@WebMvcTest` request builders that construct full patient JSON
- [ ] Run `./mvnw clean test` — must pass before committing

---

## 4. Writing & Running Tests

### Running All Tests
```bash
./mvnw clean test
# Uses `test` profile automatically (H2 in-memory, no H2 console)
```

### Running a Single Test Class
```bash
./mvnw test -Dtest=PatientControllerTest
```

### Test Slice Types

| Test type | Annotation | Loads | Use for |
|-----------|-----------|-------|---------|
| Unit test | (none / Mockito) | Nothing | Service logic, mappers |
| Web layer | `@WebMvcTest` | Controller + MVC only | Controller input validation, HTTP codes |
| Data layer | `@DataJpaTest` | JPA + H2 only | Repository queries, entity constraints |
| Integration | `@SpringBootTest` | Full context | End-to-end within the service |

### Example: Controller Slice Test
```java
@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean PatientService patientService;

    @Test
    void createPatient_returnsCreated() throws Exception {
        // arrange
        var response = new PatientResponseDTO(UUID.randomUUID(), "Aarav", ...);
        given(patientService.createPatient(any())).willReturn(response);

        // act + assert
        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Aarav", "email": "a@b.com", "birthDate": "1990-01-01" }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aarav"));
    }
}
```

---

## 5. Flyway Migration (Prod Schema Changes)

Flyway is **disabled locally** (Hibernate `ddl-auto=update` handles it).
For every schema change that will go to prod, create a migration file:

### Naming Convention
```
src/main/resources/db/migration/V<version>__<description>.sql
```
Examples:
```
V1__create_patients_table.sql
V2__add_phone_to_patients.sql
V3__add_outbox_events_table.sql
```

### Rules
- Version numbers must be strictly increasing integers.
- Never edit an already-applied migration — create a new one.
- Test the migration against a real PostgreSQL instance before merging to main.
- Enable Flyway in `application-prod.properties`:
  ```properties
  spring.flyway.enabled=true
  spring.flyway.locations=classpath:db/migration
  spring.jpa.hibernate.ddl-auto=validate
  ```

---

## 6. Debugging Common Issues

### Why does `createdBy` always show `"system"`?
`JpaAuditingConfig.auditorAware()` returns `"system"` as a placeholder until
Cognito auth is wired in. When Spring Security is added:
```java
// Replace the stub with:
return () -> Optional.ofNullable(
    SecurityContextHolder.getContext().getAuthentication()
).map(Authentication::getName);
```

### Why doesn't `data.sql` load?
Check `application.properties` (or the active profile):
```properties
# This must be present — ensures data.sql runs AFTER Hibernate DDL creates the schema
spring.jpa.defer-datasource-initialization=true
```
Also confirm `data.sql` is saved as **UTF-8 No-BOM** — a BOM causes H2 parse errors.

### Why does H2 console show "Database not found"?
Make sure you're using the `local` profile (not `test`).
The `local` profile sets `DB_CLOSE_DELAY=-1` to keep H2 alive while the app runs.
Verify the JDBC URL exactly: `jdbc:h2:mem:patient_db`

### Why do UUID columns fail in `data.sql`?
Use `gen_random_uuid()` — not `RANDOM_UUID()`. H2 running in `MODE=PostgreSQL`
supports the PostgreSQL function name.

---

## 7. Quick Reference Commands

```bash
# Start with local profile (H2 console enabled)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Run all tests
./mvnw clean test

# Run tests for a specific class
./mvnw test -Dtest=<ClassName>

# Build without tests
./mvnw clean package -DskipTests

# Check for dependency updates
./mvnw versions:display-dependency-updates

# View current git status before committing
git diff
git status
```
