---
trigger: model_decision
description: Standards for updating any Java code or configurations in the microservice target architecture
---

# Java 21 + Spring Boot 3.3 Agent Rules

You are an expert in **Java 21**, **Spring Boot 3.3+**, **Spring Framework 6+**, **JUnit 5**, and **Maven 3.9+**.  
All code you generate must follow the standards and practices below.

---

## Versions & Tooling
- Java **21** for all source/target levels.  
- Spring Boot **3.3+** with Spring Framework 6+.  
- Maven **3.9+** with dependency management via Spring Boot BOM.  
- Use multi-module Maven structure for microservices when appropriate.

---

## Code Style & Structure
- Use **PascalCase** for classes, **camelCase** for methods/fields, **ALL_CAPS** for constants.  
- Organize code by feature/layer: `controller`, `service`, `repository`, `domain`, `config`.  
- Keep controllers thin, services cohesive, repositories focused on data access.  
- Apply SOLID principles; prefer high cohesion and low coupling.

---

## Spring Boot Best Practices
- Use `@SpringBootApplication` entrypoints and constructor injection for all beans.  
- Use `@RestController`, `@Service`, `@Repository`, and `@Configuration` appropriately.  
- Leverage auto-configuration and Spring Boot starters; minimize manual wiring.  
- Centralize exception handling via `@ControllerAdvice`.  
- Apply Jakarta Bean Validation (`@Valid`, constraints) at API boundaries.

---

## REST API Design
- Follow REST conventions: meaningful URIs, correct HTTP verbs, proper status codes.  
- Separate API DTOs from internal domain entities.  
- Document APIs using **Springdoc OpenAPI**.

---

## Data & Persistence
- Use Spring Data JPA (or other Spring Data modules) for repository layers.  
- Define clear entity ownership aligned with microservice boundaries.  
- Use Flyway/Liquibase for database migrations.  
- Keep business logic out of entities and repositories.

---

## Configuration & Environments
- Use `application.yml` and Spring Profiles (`dev`, `test`, `prod`).  
- Use `@ConfigurationProperties` for type-safe configuration.  
- No hard-coded config values; keep secrets out of source control.

---

## Dependency Injection & Microservices
- Always use **constructor injection**.  
- Each microservice is a standalone Spring Boot application with its own data store.  
- Use event-driven communication (Kafka) where asynchronous flows are required.  
- Share cross-cutting concerns via shared libraries, not duplicated code.

---

## Java 21 Usage
- Use modern features where they improve clarity: `record`, pattern matching, switch expressions.  
- Avoid overusing features that reduce readability.

---

## Testing (JUnit 5 / Spring Test)
- Prefer **unit tests** without Spring context where possible.  
- Use slice testing:
  - `@WebMvcTest` for controllers  
  - `@DataJpaTest` for repositories  
- Use `@SpringBootTest` for integration tests only.  
- Use MockMvc for HTTP layer tests; Testcontainers for DB integration tests.

---

## Security & Observability
- Use Spring Security with stateless authentication (JWT or opaque tokens).  
- Use BCrypt for password hashing.  
- Add Spring Boot Actuator for health, metrics, and tracing.  
- Use SLF4J with appropriate log levels; never log sensitive data.

---

## Build & Deployment (Maven)
- Use Spring Boot BOM for version alignment.  
- Organize microservices as Maven modules when appropriate.  
- Produce executable Spring Boot fat jars.  
- Use Maven profiles for environment-specific builds.

---

This ruleset defines the coding, architectural, and operational standards for all Java services in this modernization effort.
