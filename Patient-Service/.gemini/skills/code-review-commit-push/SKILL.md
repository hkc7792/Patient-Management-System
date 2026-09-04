# SKILL.md — Developer Skills & Conventions

> Quick reference for every developer working on Patient-Management-System.
> Read this before writing any code.

---

## 1. Project Bootstrap

```bash
# Clone
git clone https://github.com/<org>/Patient-Management-System.git
cd Patient-Management-System/Patient-Service

# Build & test (H2, no Docker required)
mvn clean install

# Run locally (H2, H2 console at http://localhost:4000/h2-console)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 2. Entity Conventions

| Rule | Example |
|---|---|
| Always annotate with `@Entity` + `@Table(name="…")` | `@Table(name = "patients")` |
| UUID primary key via Hibernate strategy | `@GeneratedValue(strategy = GenerationType.UUID)` |
| Use `@NotBlank` / `@Email` / `@Past` on entity fields | validated at persistence layer |
| Use `@CreationTimestamp` / `@UpdateTimestamp` for timestamps | no manual `LocalDateTime.now()` |
| Use `@CreatedBy` / `@LastModifiedBy` for actor audit | populated by `JpaAuditingConfig` |
| Mark `id` and `createdAt` as `updatable = false` | enforces immutability after insert |
| Unique constraints via `@Index(unique=true)` in `@Table` | `idx_patients_email` |
| Use Lombok: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` | no manual getters/setters |

---

## 3. Spring Profiles

| Profile | When to use | How to activate |
|---|---|---|
| `test` | Automated tests (default) | `spring.profiles.active=test` in `application.properties` |
| `local` | Local manual run | `mvn spring-boot:run -Dspring-boot.run.profiles=local` |
| `prod` | Production | Set env var `SPRING_PROFILES_ACTIVE=prod` |

---

## 4. Database Rules

### Test / Local (H2)
- JDBC URL must include `MODE=PostgreSQL` — keeps SQL syntax compatible.
- Use `gen_random_uuid()` in raw SQL (not `RANDOM_UUID()`) because H2 PostgreSQL mode maps the Postgres function name.
- Always add `spring.jpa.defer-datasource-initialization=true` so `data.sql` runs **after** Hibernate DDL.
- Save all SQL files as **UTF-8 without BOM** — H2 SQL parser rejects the BOM byte.

### Production (PostgreSQL)
- Flyway owns all schema changes. **Never** set `ddl-auto=create` or `ddl-auto=update` in prod.
- Place migration scripts in `src/main/resources/db/migration/` as `V<n>__<description>.sql`.
- `data.sql` is not loaded in prod — seed data goes into a Flyway migration.

---

## 5. Seed Data (`data.sql`)

Located at `Patient-Service/src/main/resources/data.sql`.
Loaded automatically by Spring Boot on startup when `spring.sql.init.mode=always`.

Rules:
- Use `gen_random_uuid()` for the `id` column — mirrors JPA UUID generation.
- Use ISO timestamps without `T` separator: `'2024-01-10 09:00:00'` (H2 compatible).
- File encoding: **UTF-8 No BOM**.

---

## 6. Adding a New Feature (Checklist)

```
[ ] Create entity in models/ with UUID PK + auditing fields
[ ] Create repository interface extending JpaRepository<Entity, UUID>
[ ] Create service class annotated @Service @Transactional
[ ] Create request/response DTOs (no entity leakage in API)
[ ] Create @RestController with @Valid on request bodies
[ ] Write unit tests (service layer with Mockito)
[ ] Write integration tests (@SpringBootTest with H2 test profile)
[ ] Add Flyway migration script for prod schema change
[ ] Update CONTEXT.md and Architecture.md if design changes
```

---

## 7. Auditing

`JpaAuditingConfig` provides an `AuditorAware<String>` bean that currently
returns `"system"` for all operations.

**When auth is wired in**, replace the lambda body to extract the principal:
```java
// Example: Spring Security
return () -> Optional.ofNullable(SecurityContextHolder.getContext())
    .map(ctx -> ctx.getAuthentication())
    .filter(Authentication::isAuthenticated)
    .map(Authentication::getName);
```

---

## 8. Commit Conventions

Use **Conventional Commits**:

```
<type>(<scope>): <short description>

Types:  feat | fix | docs | refactor | test | chore | build
Scope:  patient-service | model | config | seed | deps | ci

Examples:
  feat(model): add Patient entity with UUID PK and JPA auditing
  feat(config): enable JPA auditing with AuditorAware stub
  feat(seed): add data.sql with 10 patient seed records
  fix(seed): use gen_random_uuid() and UTF-8 NoBOM for H2 compatibility
  fix(test): defer datasource init so data.sql runs after Hibernate DDL
  docs: add CONTEXT.md, Architecture.md, SKILL.md
```

---

## 9. Build Commands

```bash
mvn clean compile          # Compile only
mvn clean test             # Run tests (H2, no Docker)
mvn clean install          # Compile + test + package + install to local repo
mvn clean install -DskipTests   # Skip tests (use sparingly)
mvn spring-boot:run -Dspring-boot.run.profiles=local   # Run locally
```