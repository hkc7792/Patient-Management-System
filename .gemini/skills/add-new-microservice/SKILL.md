---
name: add-new-microservice
description: >-
  Runbook for scaffolding a new Spring Boot microservice into the
  Patient-Management-System mono-repo. Covers package structure, port
  assignment, Kafka consumer skeleton, .gemini/ documentation bootstrap,
  Docker Compose entry, and the 10-step pre-commit checklist. Activate
  whenever a new service directory needs to be created.
---

# SKILL.md — Add New Microservice

> **Skill**: `add-new-microservice`
> **Scope**: Patient-Management-System (mono-repo root)
> **Purpose**: Ensure every new service is scaffolded consistently —
> same naming, same package structure, same `.gemini/` docs, same conventions.

---

## Overview

```
Decide service name → Generate Spring Boot project → Apply conventions
→ Bootstrap .gemini/ docs → Add Kafka skeleton → Update root docs
→ Add Docker Compose entry → Commit
```

---

## Step 1 — Decide Service Identity

Before writing any code, answer these questions:

| Question | Example |
|----------|---------|
| Service name (PascalCase dir) | `Billing-Service` |
| Spring app name (kebab-case) | `billing-service` |
| Java package suffix | `com.app.patient.billingservice` |
| Port | See port registry below |
| Kafka role | Consumer / Producer / Both |
| Events consumed | `PatientRegistered` |
| Events produced | `InvoiceCreated` |
| DB required? | Yes (own schema) / No |

### Port Registry

| Service              | Port |
|----------------------|------|
| Patient-Service      | 4000 |
| API-Gateway          | 80   |
| Billing-Service      | 4001 |
| Analytics-Service    | 4002 |
| Notification-Service | 4003 |
| Auth-Service         | 4004 |

> **Rule:** Always assign the next available port and update this table.

---

## Step 2 — Generate the Spring Boot Project

Use [start.spring.io](https://start.spring.io) or the Spring CLI:

```
Group:    com.app.patient
Artifact: <ServiceName>-Service   (e.g. Billing-Service)
Package:  com.app.patient.<servicename>service   (e.g. com.app.patient.billingservice)
Java:     21
Spring Boot: 4.x (match existing services)
```

### Mandatory Starters

Always include:
- `spring-boot-starter-web` (or `webflux` if reactive)
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa` (if DB required)
- `spring-kafka` (if Kafka consumer/producer)
- `lombok`
- `postgresql` (runtime)
- `h2` (test/local)

For Kafka consumers also add:
- `spring-boot-starter-test`
- `spring-kafka-test` (EmbeddedKafka for tests)

---

## Step 3 — Apply Package & File Conventions

```
<ServiceName>-Service/
├── src/main/java/com/app/patient/<servicename>service/
│   ├── <ServiceName>Application.java     ← @SpringBootApplication entry point
│   ├── config/
│   │   └── KafkaConsumerConfig.java      ← if Kafka consumer
│   ├── consumer/
│   │   └── PatientEventConsumer.java     ← @KafkaListener class
│   ├── models/                           ← JPA entities (if DB required)
│   └── service/                          ← Business logic
├── src/main/resources/
│   ├── application.properties            ← base config
│   ├── application-local.properties      ← H2 + local dev
│   └── application-test.properties       ← H2 + EmbeddedKafka
└── src/test/
```

### `application.properties` Template

```properties
spring.application.name=<service-name>
server.port=<port>
spring.profiles.active=test

# Kafka (consumer example)
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=<service-name>-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

---

## Step 4 — Kafka Consumer Skeleton (if applicable)

```java
package com.app.patient.<servicename>service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatientEventConsumer {

    // Topic name matches the Kafka topic defined in root .gemini/architecture.md
    @KafkaListener(topics = "patient.events", groupId = "${spring.kafka.consumer.group-id}")
    public void onPatientRegistered(String message) {
        log.info("Received PatientRegistered event: {}", message);
        // TODO: deserialize → validate → business logic
    }
}
```

### Consumer Group ID Convention
```
<service-name>-group
# e.g. billing-service-group, analytics-service-group
```

---

## Step 5 — Bootstrap `.gemini/` Documentation

Every new service **must** have all three of these files before the first commit:

### 5a. Copy and fill CONTEXT.md template

```markdown
# CONTEXT.md — <ServiceName>-Service

> For system-wide context and service map, see repo root `.gemini/CONTEXT.md`

## Service Purpose
[One paragraph: what this service does, what events it consumes/produces]

## Kafka Role
- **Consumes:** `PatientRegistered` from topic `patient.events`
- **Produces:** `InvoiceCreated` to topic `patient.events` (if applicable)

## Internal Layers
[Describe packages once they exist]

## Tech Stack
[Fill from cross-cutting stack + any service-specific additions]

## Upcoming Work
- [ ] ...
```

### 5b. Copy and fill architecture.md template

```markdown
# architecture.md — <ServiceName>-Service

> For full system architecture, see repo root `.gemini/architecture.md`

## Service-Internal Architecture

[Mermaid diagram of internal layers once they exist]

## Data Flow
[Describe event consumption → processing → event publishing flow]
```

### 5c. Create at least one SKILL.md

Minimum: copy `code-review-commit-push` SKILL from Patient-Service (same workflow applies).
Add a service-specific dev SKILL later.

---

## Step 6 — Update Root Documentation

After scaffolding, update **two** root files:

### 6a. `.gemini/CONTEXT.md` — Service Map table
Change the service row from `🔲 Planned` to `🔧 In Progress` (or `✅ Active` when deployed).
Fill in the assigned port.

### 6b. `.gemini/architecture.md` — Per-Service Links section
Add a link:
```markdown
- [<ServiceName>-Service internal architecture](<path>/<ServiceName>-Service/.gemini/architecture.md)
```

### 6c. `README.md` — Service table
Update the status row for the new service.

---

## Step 7 — Add Docker Compose Entry

In `docker-compose.yml` (create at repo root if it doesn't exist):

```yaml
  <service-name>:
    build:
      context: ./<ServiceName>-Service
      dockerfile: Dockerfile
    ports:
      - "<port>:<port>"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-<service>:5432/<service>_db
    depends_on:
      - kafka
      - postgres-<service>
    networks:
      - pms-network
```

---

## Step 8 — Pre-Commit Checklist

- [ ] `spring.application.name` matches kebab-case service name
- [ ] Port registered in Step 1 port registry and `application.properties`
- [ ] Package name follows `com.app.patient.<servicename>service`
- [ ] `.gemini/CONTEXT.md` created and filled
- [ ] `.gemini/architecture.md` created (can be minimal)
- [ ] At least one `.gemini/skills/<name>/SKILL.md` present
- [ ] Root `.gemini/CONTEXT.md` service map updated
- [ ] Root `.gemini/architecture.md` links section updated
- [ ] Root `README.md` service table updated
- [ ] `mvn clean test` passes with at least one placeholder test
- [ ] Commit message: `feat(<service-name>): scaffold <ServiceName>-Service`

---

## Quick Reference — Naming Rules

| Thing | Convention | Example |
|-------|-----------|---------|
| Directory name | PascalCase with hyphen | `Billing-Service` |
| Spring app name | kebab-case | `billing-service` |
| Java package | lowercase no hyphen | `com.app.patient.billingservice` |
| Main class | PascalCase + `Application` | `BillingServiceApplication` |
| Consumer group | kebab-case + `-group` | `billing-service-group` |
| DB name | snake_case + `_db` | `billing_db` |
| Docker service | snake_case | `billing_service` |
