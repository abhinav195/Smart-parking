# Smart Parking – Spring Boot, PostgreSQL, Docker

Smart Parking is a production-style backend service for managing parking lots, built with Spring Boot 3, Java 21 and PostgreSQL.  
It focuses on clean domain modeling, observability (logging + metrics + health), solid test coverage (targeting ~80%), and container-friendly configuration.

---

## Features

- REST APIs for parking **check‑in** and **check‑out** by lot, entrance and vehicle plate.  
- Persistent storage in PostgreSQL with schema migrations handled by Flyway.  
- Health checks via Spring Boot Actuator, including:
  - `db`, `diskSpace`, `ping`, `ssl`
  - liveness and readiness health groups  
  - a custom `lotData` health indicator that verifies seeded lots exist.  
- Micrometer + Actuator metrics, including custom counters for:
  - total check‑ins
  - total check‑outs
  - (space for more business metrics later, such as rejections or conflicts).  
- Structured JSON logging with correlation id support for tracing end‑to‑end flows.  
- Comprehensive test suite (unit + integration) using JUnit 5, Spring Boot Test, Testcontainers and Mockito.  
- JaCoCo-based code coverage with the project structured and configured to reach **80%+ coverage** as tests are expanded.  
- Dockerfile and docker‑compose setup to run the app and PostgreSQL locally in containers.

---

## Tech Stack

- **Language:** Java 21  
- **Framework:** Spring Boot 3 (Web, Data JPA, Validation, Actuator)  
- **Database:** PostgreSQL 18 + Flyway migrations  
- **Build tool:** Maven (with Spring Boot Maven Plugin and JaCoCo)  
- **Testing:** JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL), Mockito, AssertJ  
- **Observability:** Spring Boot Actuator, Micrometer metrics, structured JSON logs  
- **Containerization:** Docker and Docker Compose  

---

## Architecture Overview

The project is organized into clear layers:

- `api` – Controllers, request/response DTOs, global exception handling (ProblemDetail), and API configuration.  
- `application` – Application services that implement use cases like parking session check‑in / check‑out.  
- `domain` – Entities, value objects, repositories, and domain logic (spots, vehicles, tickets, floors, lots, etc.).  
- `observability` – Metrics and custom health indicators for domain-level visibility.  

Flyway migrations in `db/migration` describe the database schema (lots, floors, spots, tickets, vehicles, payments…).  
Integration tests exercise real flows against a PostgreSQL instance (via Testcontainers or a local DB), keeping domain and persistence in sync.

---

## Getting Started

### Prerequisites

- Java 21 (JDK)  
- Maven 3.9+ (or the included `mvnw` wrapper)  
- PostgreSQL 18 (locally) **or** Docker to run the DB via containers  

### Database Setup (local, without Docker)

**Create the database and user:**
CREATE DATABASE parkinglot;
CREATE USER parking WITH PASSWORD 'parking';
GRANT ALL PRIVILEGES ON DATABASE parkinglot TO parking;


**Update `src/main/resources/application-local.yml` to match:**
spring:
datasource:
url: jdbc:postgresql://localhost:5432/parkinglot
username: parking
password: parkingspring:
profiles:
active: local


**Flyway will automatically validate and migrate the schema at application startup.**

---

## Running the Application

### Run with Maven

**From the project root:**
./mvnw spring-boot:run -Dspring-boot.run.profiles=local


The app will start on:

- `http://localhost:8080` (or another port if overridden via `server.port`).

### Run from IntelliJ IDEA

- Open `SmartParkingApplication`.  
- Create a Spring Boot run configuration.  
- Set **Active profiles** to `local` (or add `-Dspring.profiles.active=local` to VM options).  
- Run the configuration; the app should come up on port 8080/8081 with a live DB connection.

---

## Docker & Docker Compose

This project includes a `Dockerfile` for the app and a `docker-compose.yml` to run the app together with PostgreSQL.

### Build the Application Image
docker build -t smart-parking .

### Run with Docker Compose
docker compose up --build


**This will:**

- Start a PostgreSQL container (`smart-parking-db`) with database `parkinglot`, user `parking`, password `parking`.  
- Build and run `smart-parking-app` connecting to the DB at `jdbc:postgresql://db:5432/parkinglot`.  

**Access the API at**:

- `http://localhost:8080`

**Stop containers:**
docker compose down

---

## Health Checks & Actuator Endpoints

Spring Boot Actuator is enabled; key endpoints:

- `GET /actuator/health` – overall health with:
  - `db`, `diskSpace`, `ping`, `ssl`  
  - `livenessState`, `readinessState`  
  - `lotData` (custom health indicator checking lot seed data)  

- `GET /actuator/health/liveness` – liveness probe  
- `GET /actuator/health/readiness` – readiness probe  
- `GET /actuator/metrics` – list of all Micrometer meters  
- `GET /actuator/metrics/parking.checkins.total` – total check‑ins  
- `GET /actuator/metrics/parking.checkouts.total` – total check‑outs  

These endpoints are particularly useful when running in Docker/Kubernetes with liveness/readiness probes and metrics scraping.

---

## Logging

Logging is configured for **structured JSON output**, suitable for ingestion by log aggregation systems.

Highlights:

- Logs include metadata such as application name, environment, Correlation‑Id, and other MDC fields.  
- HTTP requests in the parking flow share a correlation id, which is injected via a servlet filter and included in each log line.  
- Console logs are JSON-formatted; file logs can be configured separately if needed.

You can tune the logging behavior in `src/main/resources/application-local.yml` (and other profile-specific files).

---

## Testing & Code Coverage

### Running Tests

**From the project root:**
./mvnw -q test


This runs all unit and integration tests.

### Running Tests with Coverage (JaCoCo)
./mvnw clean verify

This will:
- Execute the full test suite (unit + integration).  
- Generate a JaCoCo code coverage report under:

target/site/jacoco/index.html

Open `index.html` in a browser to explore coverage by package, class and line.

The project is structured and configured with the goal of reaching **around 80%+ code coverage** as more tests are added over time.

---

## API Overview

High-level behavior (detailed endpoints may be documented via OpenAPI later):

- **Check‑in**  
  - Inputs: `lotId`, `entranceId`, `licensePlate`, `vehicleSize`, optional `reservationId`.  
  - Behavior: validates lot, finds compatible available spot, creates/open Ticket, marks Spot as occupied, publishes availability event, returns ticket and spot details.  

- **Check‑out**  
  - Inputs: `lotId`, `ticketId`.  
  - Behavior: finds OPEN ticket, computes fee (currently simplified), records Payment, closes Ticket, releases Spot, publishes availability event, returns payment and session summary.  

- **Errors**  
  - Standardized using Spring Boot 3’s `ProblemDetail` for HTTP APIs (400/404/409/422/etc.), with machine-readable `type` and `errorCode` and human-readable `detail`.  

Concrete endpoint paths and bodies live in the controller classes under `com.example.smartparking.api`.

---

## Project Structure
smart-parking/
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/smartparking/
│  │  │  ├─ api/              # REST controllers, DTOs, global exception handler
│  │  │  ├─ application/      # Application services (parking session flow, etc.)
│  │  │  ├─ domain/           # Entities, value objects, repositories, domain logic
│  │  │  └─ observability/    # Metrics, health indicators, logging helpers
│  │  └─ resources/
│  │     ├─ db/migration/     # Flyway migrations (V1__, V2__, ...)
│  │     ├─ application.yml
│  │     └─ application-local.yml
│  └─ test/
│     └─ java/com/example/smartparking/
│        ├─ api/              # API/integration tests
│        ├─ application/      # Service-level tests
│        └─ domain/           # Unit tests for domain objects
├─ pom.xml
├─ Dockerfile
└─ docker-compose.yml

---

## Roadmap

- Expand test coverage toward and beyond the 80% goal, especially for:
  - edge cases, error paths, and business rule violations.  
- Implement hardened flows for:
  - lost tickets, vehicle/spot size mismatches, maintenance windows, and payment edge cases with clear `ProblemDetail` responses.  
- Add OpenAPI/Swagger documentation and examples for all public endpoints.  
- Integrate metrics with Prometheus/Grafana or another observability stack.  
- Add CI pipeline (e.g. GitHub Actions) to run tests, coverage, and Docker builds on each push.

---
