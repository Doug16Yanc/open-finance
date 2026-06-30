# 🏦 consent-open-finance

A robust **Open Finance** backend service built with Java 21 and Quarkus, designed to manage financial consents in a secure, event-driven, and cloud-native architecture.

---

## Diagram 

<img width="1672" height="941" alt="Image" src="https://github.com/user-attachments/assets/84afcd10-50eb-4029-ac72-05cf2170412f" />

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Build & Package](#build--package)
- [Native Executable](#native-executable)
- [Testing](#testing)
- [Observability](#observability)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## Overview

`consent-open-finance` is a microservice that implements the consent management layer of an Open Finance platform. It exposes RESTful endpoints for creating, querying, and revoking financial data-sharing consents, while publishing domain events to Kafka topics for downstream consumers.

The service follows modern cloud-native principles: reactive where it counts, observable by default, and deployable as a native binary via GraalVM.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (preview features enabled) |
| Framework | Quarkus 3.27.0 |
| Persistence | PostgreSQL + Hibernate ORM Panache |
| Migrations | Flyway |
| Messaging | Apache Kafka (SmallRye Messaging) |
| Serialization | Jackson (REST) |
| Validation | Hibernate Validator |
| Security | SmallRye JWT |
| Observability | Micrometer + Prometheus, OpenTelemetry, SmallRye Health |
| Testing | JUnit 5, REST Assured |
| Build | Maven Wrapper (mvnw) |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        REST Clients                         │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP / JWT
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Quarkus REST (JAX-RS + Jackson)                │
│                 Hibernate Validator                          │
├─────────────────────────────────────────────────────────────┤
│              Service / Domain Layer                          │
├───────────────────────────┬─────────────────────────────────┤
│  Hibernate ORM Panache    │   SmallRye Messaging (Kafka)    │
│  Flyway Migrations        │   Event producers / consumers   │
├───────────────────────────┼─────────────────────────────────┤
│       PostgreSQL          │         Apache Kafka            │
└───────────────────────────┴─────────────────────────────────┘
                Micrometer · OpenTelemetry · Health
```

---

## Prerequisites

Make sure you have the following installed:

- **Java 21** ([Eclipse Temurin](https://adoptium.net/) or [GraalVM CE 21](https://www.graalvm.org/) for native builds)
- **Maven 3.9+** (or use the included `./mvnw` wrapper — no installation needed)
- **Docker & Docker Compose** (for local PostgreSQL and Kafka via Quarkus Dev Services)

> Quarkus Dev Services automatically spins up PostgreSQL and Kafka containers in dev/test mode — no manual setup required.

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/Doug16Yanc/open-finance.git
cd open-finance
```

---

## Running the Application

### Development mode (with live reload)

```bash
./mvnw quarkus:dev
```

The application starts at `http://localhost:8080`.  
The **Quarkus Dev UI** is available at `http://localhost:8080/q/dev/` — inspect extensions, health, Kafka topics, and more directly from the browser.

### Environment variables

Key configuration properties (set in `application.properties` or as environment variables):

| Property | Description | Default |
|---|---|---|
| `quarkus.datasource.jdbc.url` | PostgreSQL JDBC URL | Dev Services auto-configured |
| `quarkus.datasource.username` | DB username | Dev Services auto-configured |
| `quarkus.datasource.password` | DB password | Dev Services auto-configured |
| `kafka.bootstrap.servers` | Kafka broker address | Dev Services auto-configured |
| `mp.jwt.verify.publickey.location` | JWT public key location | — |
| `mp.jwt.verify.issuer` | Expected JWT issuer | — |

---

## Build & Package

### Standard JAR

```bash
./mvnw package
```

Produces `target/quarkus-app/quarkus-run.jar`. Run it with:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

> Dependencies are placed under `target/quarkus-app/lib/`. Keep the whole directory together when deploying.

### Über-JAR (fat JAR)

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

Run with:

```bash
java -jar target/*-runner.jar
```

---

## Native Executable

Build a native binary for minimal memory footprint and near-instant startup:

```bash
# Requires GraalVM with native-image
./mvnw package -Dnative
```

Or build inside a container (no local GraalVM needed):

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Run the native binary:

```bash
./target/consent-open-finance-1.0-SNAPSHOT-runner
```

> See the [Quarkus native guide](https://quarkus.io/guides/maven-tooling) for more details.

---

## Testing

```bash
./mvnw test
```

Tests use **JUnit 5** and **REST Assured**. Quarkus Dev Services automatically provides isolated PostgreSQL and Kafka instances during test execution.

---

## Observability

| Endpoint | Description |
|---|---|
| `GET /q/health` | Overall health (liveness + readiness) |
| `GET /q/health/live` | Liveness check |
| `GET /q/health/ready` | Readiness check |
| `GET /q/metrics` | Prometheus metrics (Micrometer) |

Distributed tracing is exported via **OpenTelemetry** — configure the OTLP exporter endpoint with `quarkus.otel.exporter.otlp.endpoint`.

---

## Project Structure

```
open-finance/
├── src/
│   ├── main/
│   │   ├── java/tech/          # Application source code
│   │   └── resources/
│   │       ├── application.properties # Quarkus configuration
│   │       └── db/migration/          # Flyway SQL migrations
│   └── test/
│       └── java/br/com/ofb/          # Unit & integration tests
├── .mvn/wrapper/                      # Maven wrapper files
├── mvnw / mvnw.cmd                    # Maven wrapper scripts
├── pom.xml                            # Project dependencies & build config
└── .dockerignore
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
4. Push and open a Pull Request

---

## Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [SmallRye Messaging (Kafka)](https://quarkus.io/guides/kafka)
- [Hibernate ORM Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [SmallRye JWT](https://quarkus.io/guides/security-jwt)
- [Flyway with Quarkus](https://quarkus.io/guides/flyway)
- [Open Finance Brasil](https://openfinancebrasil.org.br/)
