# Aimr Notify

> A multi-tenant SaaS notification delivery platform — built for scale, reliability, and developer control.

Aimr Notify is a production-grade backend platform inspired by Twilio and SendGrid. It enables teams to send transactional and broadcast notifications across multiple channels (Email, SMS, Push) with built-in multi-tenancy, idempotency, rate limiting, and a resilient delivery pipeline.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Data Model](#data-model)
- [Notification Pipeline](#notification-pipeline)
- [Security](#security)
- [Monitoring](#monitoring)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)

---

## Overview

Aimr Notify is designed as a **multi-tenant notification infrastructure platform**. Each tenant (organization) can manage their own members, API keys, sender identities, and notification channels — fully isolated from other tenants.

The system is split into two independently deployable Spring profiles:

- **`api`** — Handles REST API requests, authentication, and Kafka publishing
- **`worker`** — Consumes Kafka topics and executes actual notification delivery

Both profiles live in a single Spring Boot codebase and are deployed as separate Kubernetes pods.

---

## Key Features

- **Multi-Tenancy** — Full tenant isolation with per-tenant members, API keys, rate limits, and sender identities
- **Multi-Channel Delivery** — Email (Gmail + SendGrid), SMS, and FCM Push notifications
- **Idempotent Delivery** — Redis Lua-based deduplication ensures no notification is delivered twice
- **Rate Limiting** — Per-tenant, per-channel atomic rate limiting via Redis Lua scripts
- **Broadcast Notifications** — Fan-out from a single API call to all eligible recipients with inflight concurrency control
- **Resilient Email Pipeline** — Dual-provider (Gmail → SendGrid) with Resilience4j circuit breaker fallover
- **Retry & Dead Letter** — Per-channel retry topics and DLQ for failed deliveries
- **Template Rendering** — Dynamic `{{placeholder}}` variable substitution in notification templates
- **Soft Delete & Tenant Sweeper** — Two-phase tenant deletion with staged background cleanup via Spring Batch
- **Cursor-based Pagination** — Composite `{createdAt, _id}` pagination for efficient notification history queries

---

## Architecture

```
Client
  │
  ▼
[ REST API Layer ] — Spring Boot (api profile)
  │   JWT + API Key Auth
  │   Tenant isolation via X-Tenant-ID header
  │
  ▼
[ Kafka ] ─────────────────────────────────────────────────
  │                                                        │
  ▼                                                        ▼
[ notification.ingest ]               [ notification.{channel}.deliver ]
  │                                          │
  ▼                                          ▼
[ IngestConsumer ]              [ EmailDeliveryWorker / SmsWorker / PushWorker ]
  │                                          │
  ▼                                          │── Retry → notification.{channel}.retry
[ NotificationGuardService ]               │── DLQ   → notification.{channel}.dlq
  │  Redis Lua (dedup + rate limit)
  │
  └──► publish to notification.{channel}.deliver
```

**Broadcast Flow:**

```
POST /broadcasts
  │
  ▼
[ BroadcastConsumer ]
  Expands batch → individual records
  │
  ▼
[ notification.ingest ] (per recipient)
  │
  ▼
[ IngestConsumer → NotificationGuardService → channel.deliver ]
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 (Virtual Threads) |
| Framework | Spring Boot 4 (Jakarta EE 11) |
| Security | Spring Security 7 |
| Message Broker | Apache Kafka (KRaft mode) |
| Primary Database | PostgreSQL |
| Document Store | MongoDB |
| Cache / Guard | Redis (AOF persistence, Lua scripts) |
| Resilience | Resilience4j (Circuit Breaker) |
| Email Delivery | Gmail SMTP + SendGrid |
| Push Delivery | Firebase Cloud Messaging (FCM) |
| Batch Processing | Spring Batch |
| Monitoring | Prometheus + Grafana |
| Password Hashing | Argon2id |
| ID Generation | UUIDv7 |
| Build Tool | Maven (Maven Wrapper) |

---

## Data Model

### PostgreSQL (Relational)

| Table | Purpose |
|---|---|
| `tenants` | Top-level tenant records |
| `users` | Pure authentication identity records |
| `tenant_memberships` | Operational actor — links user to tenant with role |
| `api_keys` | API keys scoped to a tenant membership |
| `invitations` | Email-based tenant invitations |
| `bindings` | Sender identity ↔ channel bindings per tenant |
| `sender_identities` | Verified sender addresses / phone numbers / FCM configs |

### MongoDB (Documents)

| Collection | Purpose |
|---|---|
| `notifications` | Individual notification records with delivery status |
| `templates` | Reusable notification templates with `{{placeholder}}` variables |

**Notification Statuses:** `PENDING` → `PENDING_RETRY` → `DELIVERED` / `FAILED`

---

## Notification Pipeline

### Single Notification

1. Client sends `POST /notifications` with API key
2. `NotificationAuthFilter` validates key and resolves tenant context
3. Request is validated and a `PENDING` notification record is created in MongoDB
4. An event is published to `notification.ingest`
5. `IngestConsumer` picks it up and passes to `NotificationGuardService`
6. Guard runs a single atomic Redis Lua round-trip:
   - SHA-256 deduplication check
   - Per-tenant, per-channel rate limit check
7. If both pass, the notification is published to `notification.{channel}.deliver`
8. Delivery worker sends via the appropriate provider and updates MongoDB status

### Retry & DLQ

- On delivery failure, the worker publishes to `notification.{channel}.retry`
- After max retries, moves to `notification.{channel}.dlq`
- Status transitions: `PENDING` → `PENDING_RETRY` → `FAILED`

### Email Provider Fallover

- Primary: Gmail SMTP (`JavaMailSender` / `MimeMessageHelper`)
- Fallback: SendGrid API
- Resilience4j circuit breaker monitors Gmail; opens and routes to SendGrid on sustained failures

---

## Security

- **JWT Authentication** — `jjwt 0.12.6`, validated in `JwtAuthFilter`
- **API Key Authentication** — Hashed with Argon2id, scoped to tenant memberships
- **Tenant Isolation** — `TenantAuthFilter` populates `TenantContext` (ThreadLocal) from `X-Tenant-ID`
- **Filter Chain Order** — `JwtAuthFilter` → `TenantAuthFilter` → `NotificationAuthFilter`
- **Password Hashing** — Argon2id with parameters tuned for a 6GB RAM environment
- **Principal** — Custom `AimrPrincipal` record injected via `@AuthenticationPrincipal`
- **Row-Level Security** — PostgreSQL RLS policies for additional tenant data isolation

---

## Monitoring

- **Prometheus** scrapes Spring Boot Actuator metrics
- **Grafana** dashboards for:
  - Kafka consumer lag per topic
  - Notification delivery rates and failure rates
  - Redis cache hit/miss ratios
  - Circuit breaker state (Gmail → SendGrid)
  - JVM virtual thread pool metrics

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Apache Kafka 4.x (KRaft mode — **no ZooKeeper**)
- PostgreSQL 15+
- MongoDB 7+
- Redis 7+
- Firebase project (for FCM push)
- Gmail account with App Password OR SendGrid API key

### Kafka KRaft Setup

```bash
# Format storage (required for Kafka 4.x KRaft standalone)
kafka-storage.sh format --standalone -t $(kafka-storage.sh random-uuid) -c config/kraft/server.properties

# Start broker
kafka-server-start.sh config/kraft/server.properties
```

### Run the API Server

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=api
```

### Run the Worker

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=worker
```

---

## Configuration

Key properties in `application.yml`:

```yaml
spring:
  profiles:
    active: api  # or worker

  datasource:
    url: jdbc:postgresql://localhost:5432/aimr
    username: your_pg_user
    password: your_pg_password

  data:
    mongodb:
      uri: mongodb://localhost:27017/aimr

  kafka:
    bootstrap-servers: localhost:9092

  redis:
    host: localhost
    port: 6379

app:
  jwt:
    secret: your_jwt_secret
    expiration-ms: 86400000

  mail:
    primary:
      host: smtp.gmail.com
      port: 587
      username: your_email@gmail.com
      password: your_app_password

  sendgrid:
    api-key: your_sendgrid_key

  firebase:
    credentials-path: classpath:firebase-service-account.json
```

---

## API Reference

### Authentication

All API endpoints require either:
- `Authorization: Bearer <jwt_token>` — for user-facing endpoints
- `X-API-Key: <api_key>` + `X-Tenant-ID: <tenant_id>` — for programmatic/notification endpoints

### Core Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Authenticate and receive JWT |
| `POST` | `/tenants` | Create a new tenant |
| `POST` | `/tenants/{id}/invitations` | Invite a member to a tenant |
| `POST` | `/notifications` | Send a single notification |
| `POST` | `/broadcasts` | Send a broadcast notification to all recipients |
| `GET` | `/notifications` | List notifications (cursor-paginated) |
| `GET` | `/notifications/{id}` | Get a single notification |
| `POST` | `/templates` | Create a notification template |
| `GET` | `/templates/{id}` | Retrieve a template |
| `POST` | `/sender-identities` | Register a sender identity |
| `POST` | `/bindings` | Bind a sender identity to a channel |

---

## Project Structure

```
src/main/java/com/aimr/notify/
├── api/                    # Controllers, DTOs, request/response models
├── consumer/               # Kafka consumers (IngestConsumer, BroadcastConsumer, DeliveryWorkers)
├── domain/                 # Core domain models, interfaces, DAO contracts
│   ├── notification/
│   ├── tenant/
│   ├── user/
│   └── template/
├── infrastructure/         # Implementations: DB, Redis, Kafka, external providers
│   ├── persistence/        # PostgreSQL (JPA) + MongoDB (MongoTemplate) repos
│   ├── cache/              # Redis CacheServiceImpl, IdempotencyService
│   ├── email/              # GmailEmailProvider, SendGridEmailProvider
│   ├── push/               # FcmChannel
│   ├── sms/                # SmsChannel
│   └── kafka/              # KafkaTemplate config, topic definitions
├── security/               # JwtAuthFilter, TenantAuthFilter, NotificationAuthFilter
│                           # AimrPrincipal, SecurityConfig
└── shared/                 # NotificationGuardService, TemplateRenderer, EntityUpdater
                            # TenantDeletionSweeper, UUIDv7 generator
```

---

## Roadmap

- [ ] Webhook delivery channel
- [ ] Per-tenant usage dashboards (REST API)
- [ ] Notification scheduling (send-at)
- [ ] Full Kubernetes manifests with Helm chart
- [ ] SDK clients (Java, Node.js)
- [ ] Multi-region Kafka replication
- [ ] Tenant self-service portal (React frontend)

---

## Author

**Kevin Unachukwu**
B.Eng. Electrical and Electronics Engineering — Michael Okpara University of Agriculture, Umudike

GitHub: [@foxx-7](https://github.com/foxx-7) · Twitter/X: [@kevincoretto](https://twitter.com/kevincoretto)

---

> Built from scratch as a solo project. Every architectural decision — from Kafka topic design to Redis Lua atomicity to dual-provider email fallover — was made intentionally.
