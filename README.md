# 🍣 Sushi-Train Simulator

Welcome to **Sushi-Train**, a playful yet technically robust full-stack demo app built with Spring Boot, Angular, PostgreSQL, Docker, and optionally Kafka/Redpanda.  
It simulates a Japanese conveyor-belt sushi restaurant where plates of different value tiers circulate, customers pick them, and operators monitor the belt and orders.

---

## 🎯 Purpose

This project combines **fun and learning**:

- Practice and showcase technologies: **Spring Boot**, **Angular**, **PostgreSQL**, **Docker Compose**, optionally **Kafka** & **Kubernetes**.
- Learn architectural concepts: domain-driven design (DDD), event streaming, container orchestration.
- Build something visually engaging (Japanese _kawaii_ theme) while architecting it for scalability and reliability.
- Create a clean, documented **portfolio project** you can run locally or later host in the cloud.

---

## 🧩 Features (Phase 1: “Cute Demo”)

- Conveyor belt simulation with **rotating plates**.
- Multiple **plate tiers** (different price categories in Yen).
- **Seats and Orders** — customers pick plates, build orders, and check out.
- Backend uses **DDD + ports & adapters** pattern.
- Local setup via **Docker Compose** (backend, frontend, PostgreSQL).
- Built for future extensions like event streaming and monitoring.

---

## 🗂️ Project Structure

```
sushi-train/
 ├── backend/        → Spring Boot API & domain logic
 ├── frontend/       → Angular UI
 ├── docs/           → Architecture, domain events, ERDs, diagrams
 ├── docker-compose.yml
 ├── k8s/            → (future) Kubernetes manifests/Helm
 └── README.md
```

---

## 📖 Documentation

- 🗺️ **[Static Model](docs/domain-model.md)** – Business Objects, Use Cases, Entity Relationships
- 🧠 **[Dynamic Model](https://github.com/lorenzipsum/sushi-train/blob/main/docs/domain-events.md)** – Event structure, envelopes, and example flows
- ⚙️ **[Architecture Overview (planned)](docs/architecture.md)** – System components and deployment topology

---

## ⚙️ Getting Started (Local Development)

1. **Clone the repository**

   ```bash
   git clone https://github.com/lorenzipsum/sushi-train.git
   cd sushi-train
   ```

2. **Start the stack**

   ```bash
   docker-compose up --build
   ```

3. **Open in your browser**

   - Frontend (Angular): [http://localhost:4200](http://localhost:4200)
   - Backend API: [http://localhost:8088](http://localhost:8088)
   - Swagger UI (if enabled): [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)

4. **Play around!**
   - Use the Operator view to add plates and adjust the belt speed.
   - Use the Seat view to pick plates and watch your order grow.

---

## 🧭 Roadmap

| Phase | Title          | Description                                             |
| ----- | -------------- | ------------------------------------------------------- |
| **1** | Cute Demo      | Core simulation: belt, plates, seats, orders            |
| **2** | Real-Time Mode | Add Kafka/Redpanda, live belt rotation, event streaming |
| **3** | Stress Lab     | Load testing, autoscaling, performance dashboards       |
| **4** | Cloud Deploy   | Run on Kubernetes with Prometheus & Grafana monitoring  |

---

## 🧰 Technology Stack

| Layer            | Technology                       |
| ---------------- | -------------------------------- |
| Backend          | Spring Boot (Java 21), REST, DDD |
| Frontend         | Angular 18 +, TypeScript         |
| Database         | PostgreSQL                       |
| Messaging        | Kafka or Redpanda _(optional)_   |
| Containerization | Docker / Docker Compose          |
| Future Ops       | Kubernetes, Prometheus, Grafana  |

---

## 📊 Architecture Highlights

- Clear domain boundaries (entities, aggregates, services).
- Event-driven backbone (see **Domain Events**).
- Easily replaceable infrastructure adapters (database, broker).
- “Ports & Adapters” / Hexagonal structure → clean separation of core logic and I/O.
- Realistic but small enough for personal use and demos.

---

## 🧪 Testing

- **Unit tests:** domain services and aggregates.
- **Integration tests:** repository adapters and REST controllers.
- **Event tests:** verify domain events publish correctly.
- **Load tests:** (planned) simulate high-frequency plate picks.

---

## 📸 Screenshots (coming soon)

| Operator View                | Seat View                        |
| ---------------------------- | -------------------------------- |
| _(Add plates, control belt)_ | _(Pick plates, see order total)_ |

---

## 🧱 How to Contribute or Reuse

- Fork this repo and use it freely — licensed under **MIT**.
- Open an issue for ideas, bugs, or improvements.
- Contributions are welcome: design, docs, or code.
- Perfect base for learning event-driven microservice design.

---

## 👤 Author

**Lorenz Schmid**  
with a little help from _Janet_, your friendly AI assistant 🍀

---

## 📝 License

This project is released under the [MIT License](LICENSE).

---

Thank you for checking out **Sushi-Train** —  
may your code be clean, your plates delicious, and your CI/CD pipelines ever green! 🍥
