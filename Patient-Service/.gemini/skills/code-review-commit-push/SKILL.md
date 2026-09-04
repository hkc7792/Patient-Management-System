# SKILL.md — Code Review, Commit & Push

> **Skill**: `code-review-commit-push`
> **Scope**: Patient-Management-System / Patient-Service
> **Purpose**: Standard workflow every developer must follow before pushing any change.

---

## Overview

```
Write Code → Self Review → Stage Logically → Commit (Conventional) → Push → PR
```

---

## Step 1 — Self Code Review Checklist

Before staging anything, review your own diff (`git diff`):

### General
- [ ] No dead code, no commented-out blocks left behind
- [ ] No `System.out.println` / raw debug logging
- [ ] No hardcoded secrets, passwords, or tokens
- [ ] No `TODO` left without a GitHub issue reference

### Java / Spring
- [ ] Entities have `@NotBlank` / `@Email` / `@Past` where appropriate
- [ ] No entity objects leaked in API response — use DTOs
- [ ] Service methods are `@Transactional` where DB writes occur
- [ ] New fields in `Patient` have correct `nullable`, `length`, `updatable` constraints
- [ ] Lombok annotations used — no manual getters/setters/constructors
- [ ] `AuditorAware` fields (`createdBy`, `updatedBy`) not manually set

### SQL / Data
- [ ] `data.sql` saved as **UTF-8 No-BOM**
- [ ] UUID column uses `gen_random_uuid()` (not `RANDOM_UUID()`) for H2 PostgreSQL mode
- [ ] Timestamps in `data.sql` use space separator: `'2024-01-10 09:00:00'`
- [ ] Any new table has a corresponding Flyway script for prod

### Tests
- [ ] New feature has at least one unit test
- [ ] `mvn clean test` passes locally before committing

---

## Step 2 — Logical Staging (Atomic Commits)

**Never `git add .` everything into one commit.**  
Group changes by their concern:

```bash
# 1. Check what changed
git status
git diff

# 2. Stage one logical group at a time
git add <file1> <file2>
git commit -m "..."

# 3. Repeat for each logical group
git add <file3>
git commit -m "..."
```

### What counts as one logical commit?

| Group | What belongs together |
|---|---|
| **Model change** | Entity file + any migration SQL |
| **Feature** | Service + Repository + Controller for one endpoint |
| **Test** | Test class(es) for the feature above |
| **Config** | Properties file(s) for one concern |
| **Seed data** | Only `data.sql` |
| **Bug fix** | Only the files that fix the single bug |
| **Docs** | Only markdown / documentation files |
| **Refactor** | Rename / move with zero behaviour change |

---

## Step 3 — Conventional Commit Format

```
<type>(<scope>): <short imperative description>

[optional body — what and why, not how]
[optional footer — breaking changes, issue refs]
```

### Types

| Type | When to use |
|---|---|
| `feat` | New feature or capability |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code restructure, no behaviour change |
| `test` | Adding or fixing tests |
| `chore` | Tooling, config, dependencies |
| `build` | Maven / build system changes |
| `style` | Formatting, whitespace (no logic change) |

### Scopes for this project

| Scope | Examples |
|---|---|
| `model` | `Patient.java` entity changes |
| `config` | Spring config, properties files |
| `seed` | `data.sql` |
| `test` | Test classes, test properties |
| `docs` | CONTEXT.md, Architecture.md, SKILL.md |
| `deps` | `pom.xml` dependency changes |
| `ci` | GitHub Actions / pipeline |
| `patient-service` | Cross-cutting Patient-Service changes |

### Real examples from this project

```
feat(model): add Patient entity with UUID PK and JPA auditing
feat(config): enable JPA auditing with AuditorAware stub
feat(seed): add data.sql with 10 meaningful patient seed records
fix(seed): use gen_random_uuid() and UTF-8 No-BOM for H2 compatibility
fix(test): defer datasource init so data.sql runs after Hibernate DDL
docs: add CONTEXT.md, Architecture.md, and SKILL.md
refactor(docs): move CONTEXT.md and Architecture.md into Patient-Service/.gemini/
refactor(docs): move SKILL.md into Patient-Service/.gemini/skills/code-review-commit-push/
```

### Rules
- Subject line ≤ 72 characters
- Use **imperative mood**: "add", "fix", "move" — not "added", "fixing"
- No period at the end of the subject line
- Body explains **why**, not what (code shows what)

---

## Step 4 — Push & Pull Request

```bash
# Always pull latest before pushing to avoid conflicts
git pull --rebase origin <branch>

# Push your branch
git push origin <branch>

# Open PR — title must follow Conventional Commit format
# e.g. "feat(model): add Patient entity with UUID PK"
```

### PR Checklist
- [ ] PR title follows Conventional Commit format
- [ ] Description explains the **why** of the change
- [ ] Linked to a GitHub issue (if applicable): `Closes #<issue>`
- [ ] `mvn clean install` passes in CI
- [ ] No unrelated files are included in the PR
- [ ] Reviewer assigned

---

## Quick Reference

```bash
# Review your own diff before staging
git diff
git diff --staged          # after git add, before commit

# Undo staging (keep changes in working tree)
git restore --staged <file>

# Amend last commit message (before push only)
git commit --amend -m "corrected message"

# Interactive rebase to squash/reorder (before push only)
git rebase -i HEAD~<n>

# Check commit log
git log --oneline -10
```