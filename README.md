# JobsTracker

A Spring Boot application for tracking job applications with PostgreSQL database.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
    - [Running with Docker](#running-with-docker)
    - [Running Locally](#running-locally)

---

## Prerequisites

- Java 21
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL 16 (if running locally without Docker)

---

## Technology Stack

- **Backend Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, Spring Boot Test (H2 in-memory database)

---

## Getting Started

### Running on Docker
#### Start All Services
```bash
# Build and start PostgreSQL + Spring Boot application
docker-compose up --build

# Or run in detached mode (background)
docker-compose up -d --build
```
The application will be available at:

    Application: http://localhost:8080
    Health Check: http://localhost:8080/actuator/health
    Database: localhost:5432

#### Stop All Services

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (clears database data)
docker-compose down -v
```

#### View logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f postgres
```
### Running on Locally
```bash

# Build and start PostgreSQL
docker-compose up postgres
```

## Swagger

### Access Swagger
Swagger UI (Interactive Documentation)
```
http://localhost:8080/swagger-ui.html
```
OpenApi JSON
```
http://localhost:8080/api-docs
```
Alternative Swagger UI URL

```
http://localhost:8080/swagger-ui/index.html
```