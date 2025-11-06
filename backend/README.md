# 🍣 Sushi-Train Backend

Spring Boot application providing the REST and WebSocket API for the Sushi-Train simulator.

---

## 🚀 Overview

This service implements the **domain logic**, **persistence**, and **real-time updates** for the Sushi-Train system.  
It powers the Angular frontend via:
- REST endpoints for managing belts, plates, and orders
- WebSocket messages for live updates (belt rotation, plate picked, etc.)

---

## 🧱 Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **PostgreSQL**
- **Flyway** (DB migrations)
- **WebSocket**
- **Docker / Docker Compose**
- *(Phase 2+)* Kafka / Redpanda (event streaming)

---

## ⚙️ Running Locally

### 1. Run with Docker Compose (recommended)
From the repo root:
```bash
docker-compose up --build
```

### 2. Or run manually
```bash
./mvnw spring-boot:run
```

Backend runs on:  
👉 [http://localhost:8088](http://localhost:8088)

Swagger UI (if enabled):  
👉 [http://localhost:8088/swagger-ui.html](http://localhost:8088)

---

## 🧩 Project Layout

```
src/main/java/com/lorenzipsum/sushitrain/backend
 ├── domain/           # Core business logic (DDD entities, value objects)
 ├── application/      # Services, use cases, domain events
 ├── infrastructure/   # Persistence, configuration, adapters
 └── interfaces/       # REST + WebSocket controllers
```

Configuration:
```
src/main/resources/
 ├── application.yml
 └── db/migration/     # Flyway SQL scripts
```

---

## 🧠 Related Docs

- [Architecture Overview](../docs/architecture.md)
- [Domain Events](../docs/domain-events.md)
- [Domain Model / ERD](../docs/domain-model.md)

---

## 🧹 Common Commands

```bash
./mvnw clean test      # Run unit tests
./mvnw verify          # Full build with checks
./mvnw spring-boot:run # Start the service locally
```

---

## 📜 License

MIT — feel free to use and adapt for learning or demos.
