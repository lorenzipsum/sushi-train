# Sushi-Train — Architecture Overview

This document gives a high-level view of Sushi-Train’s architecture using a simple, C4-style progression:

1. **System Context** — who/what interacts with the system
2. **Containers** — major runtime pieces (frontend, backend, DB, broker)
3. **Key Flows** — how the most important interactions work
4. **Deployment (Local)** — how Docker Compose runs everything
5. **Evolution** — how this scales in later phases

Also see: [Domain Events](./domain-events.md) · [ERD / Domain Model](./domain-model.md)

---

## 1) System Context

```mermaid
flowchart LR
  U1[Guest - Seat UI]
  U2[Operator - Belt Control]

  FE[Angular Frontend]
  BE[Spring Boot Backend]
  PG[(PostgreSQL)]
  K[(Kafka / Redpanda - optional)]

  U1 --> FE
  U2 --> FE

  FE -- REST or WebSocket --> BE
  BE -- SQL --> PG
  BE -. Publish or Consume events .-> K
```

Intent: A playful sushi conveyor-belt simulation with a domain-driven core and optional event streaming.

---

## 2) Container View (runtime building blocks)

```mermaid
flowchart TB
  BROWSER[Browser - Angular SPA]

  API[Spring Boot API - Ports and Adapters]
  DB[(PostgreSQL)]
  BROKER[(Kafka or Redpanda - optional)]

  BROWSER -- HTTP JSON / WebSocket --> API
  API -- Responses --> BROWSER

  API -- JDBC --> DB
  API -. Events .-> BROKER
```

Key responsibilities:

- **Angular Frontend**: belt and seat UIs, kawaii visuals, realtime updates via WebSocket.
- **Spring Boot Backend**: REST and WS endpoints, domain logic, scheduled belt ticks, event publishing.
- **PostgreSQL**: persistence for menu items, plates, orders, order lines, belt slots.
- **Kafka/Redpanda (optional)**: event backbone for real-time mode and analytics.

---

## 3) Key Flows

### 3.1 Belt movement (rotation offset)

```mermaid
sequenceDiagram
  participant SCHED as BeltScheduler
  participant DOM as Domain - Belt
  participant API as Spring Service
  participant WS as WebSocket Hub
  participant UI as Angular UI

  SCHED->>DOM: advanceOffset()
  DOM-->>API: new rotation offset
  API->>WS: send BeltTicked
  WS-->>UI: receive BeltTicked
  UI->>UI: recompute visible slots and animate
```

Why offset? Slots stay fixed (0..N-1); movement is a single integer `rotation_offset`. O(1) to advance and easy to scale.

### 3.2 Seat picks a plate

```mermaid
sequenceDiagram
  participant UI as Seat UI
  participant API as Spring REST
  participant DOM as Domain - Order
  participant DB as Postgres
  participant WS as WebSocket Hub

  UI->>API: POST /orders/{id}/pick { plateId }
  API->>DOM: order.addLine(plateId, priceAtPick)
  DOM-->>API: order updated with new line
  API->>DB: save Order and OrderLine
  API->>WS: publish PlatePicked
  WS-->>UI: update order total and belt state
```

Event source: `PlatePicked` is the core business event; see [Domain Events](./domain-events.md).

---

## 4) Deployment — Local (Docker Compose)

```mermaid
flowchart LR
  FE[frontend : 4200]
  BE[backend : 8088]
  PG[(postgres : 5432)]
  KAFKA[(redpanda : 9092 - optional)]

  FE --> BE
  BE --> PG
  BE -. Events .-> KAFKA
```

Run locally:

```bash
docker-compose up --build
# Frontend: http://localhost:4200
# Backend:  http://localhost:8088
# Swagger (if enabled): http://localhost:8088/swagger-ui.html
```

Profiles:

- Phase 1: no broker; WebSocket provides live updates.
- Phase 2+: enable broker service and Spring profile `streaming`.

---

## 5) Evolution (Phases)

|             Phase | Focus                      | What changes                                |
| ----------------: | -------------------------- | ------------------------------------------- |
|  **1. Cute Demo** | CRUD + belt scheduler + WS | Minimal adapters; single-node Compose       |
|  **2. Real-Time** | Kafka or Redpanda events   | Outbox producer, WS consumer, dashboards    |
| **3. Stress Lab** | Load, scaling, reliability | Autoscaling, metrics, chaos, analytics sink |
|      **4. Cloud** | Kubernetes deployment      | Manifests or Helm, Prometheus and Grafana   |

Non-breaking design choices: ports and adapters, domain events, rotation offset — all allow incremental adoption of streaming and scaling without refactoring the core.

---

## 6) Technology Choices (why)

- **Hexagonal (Ports and Adapters)**: swap infrastructure (WebSocket vs Kafka) without touching domain.
- **PostgreSQL + Flyway**: robust SQL with migrations; easy to grow analytics later.
- **Angular**: component-driven UI; RxJS for realtime streams.
- **Kafka or Redpanda (optional)**: reliable, replayable event backbone; perfect for Stress Lab.

---

## 7) Cross-cutting Concerns

- **Observability**: Spring Actuator and Micrometer; later Prometheus and Grafana.
- **Testing**: domain unit tests; integration tests with Testcontainers; contract tests for events.
- **Idempotency**: events carry unique `id`; consumers dedupe; consider transactional outbox in Phase 2+.
- **Security (later)**: seat vs operator roles; rate limits on admin actions.

---

## 8) Traceability Map

- **Domain model**: see [ERD / Domain Model](./domain-model.md)
- **Event catalog**: see [Domain Events](./domain-events.md)
- **API surface**: (optional) link to Swagger/OpenAPI
- **Runbook**: (optional) `docs/runbook.md` for commands and gotchas

---

### How to update this page

- Keep diagrams in **Mermaid** for quick edits.
- If you draw in Draw.io or Excalidraw, commit both the **source** and exported **PNG/SVG** in `docs/`.
- Keep links and filenames consistent (`domain-events.md`, `domain-model.md`, etc.).
- Make small PRs: when architecture changes (new topic, new container), update the relevant diagram and add one sentence of context.

---
