---
name: code-review-push
description: >-
  Standard runbook for checking file changes, verifying build status, logically
  grouping changes by file/feature type, making atomic conventional commits, and
  pushing to the remote repository.
---

# SKILL.md — Code Review, Verification & Push

> **Skill**: `code-review-push`
> **Scope**: Patient-Management-System (mono-repo root & services)
> **Purpose**: Automated and manual workflow before staging, committing, and pushing code.

---

## Workflow Overview

```
1. Inspect Changes → 2. Verify Build (`mvn test` / `mvn install`) → 3. Group logically → 4. Atomic Commits → 5. Push
```

---

## Step 1 — Check File Changes

Inspect all modified and untracked files:
```bash
git status
git diff
```
Verify:
- No temporary/scratch files included
- No secrets or credentials exposed
- Comments are clean and concise

---

## Step 2 — Run Verification Commands

Run the full build verification pipeline in order:
```bash
# 1. Run unit & integration tests
mvn test

# 2. Run fast package build skipping tests
mvn install -DskipTests

# 3. Run full install with tests
mvn install
```
> **Rule:** Do NOT proceed to commit or push if any step in the build fails.

---

## Step 3 — Logical Staging & Grouping

Group changes first by **file/component type**, then by **logically related features**:

| Grouping | Description | Examples |
|---|---|---|
| **Root Docs & Skills** | System context, architecture, root skills | `.gemini/`, `README.md` |
| **Service Docs & Skills** | Service context, architecture, service skills | `Patient-Service/.gemini/` |
| **Build & Config** | Maven dependencies, properties | `pom.xml`, `application*.properties` |
| **Domain & Persistence** | Entities, Repositories | `Patient.java`, `PatientRepository.java` |
| **DTOs & Mappers** | Request/Response DTOs, Mappers | `PatientRequest.java`, `PatientResponse.java`, `PatientMapper.java` |
| **Service & Web Layer** | Service logic, Controllers, Handlers | `PatientService.java`, `PatientController.java`, `GlobalExceptionHandler.java` |
| **Tests** | Unit, Slice, Integration tests | `*Test.java` |

---

## Step 4 — Atomic Conventional Commits

Commit each group separately with a clean, imperative commit message following Conventional Commits:

```bash
git add <file1> <file2>
git commit -m "<type>(<scope>): <short description>"
```

### Commit Types
- `feat`: New feature or capability
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Adding or updating tests
- `refactor`: Code change with no feature/fix change
- `chore`: Build configuration or tool updates

---

## Step 5 — Push to Remote

Push all committed changes to the active branch on remote:
```bash
git push origin <current-branch>
```
