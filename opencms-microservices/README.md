# OpenCms Microservices

Microservice architecture for OpenCms CMS, designed for incremental migration from the monolith using the strangler-fig pattern.

## Architecture Overview

This repository contains 7 core microservices:

1. **API Gateway** - Request routing, authentication, rate limiting
2. **Identity Service** - User authentication, authorization, ACLs
3. **Content Service** - VFS operations, versioning, locking
4. **Publishing Service** - Publish workflow, job queue
5. **Module Service** - Module lifecycle, resource types
6. **Rendering Service** - Content transformation, caching
7. **Search Service** - Indexing, full-text search

## Repository Structure

```
opencms-microservices/
├── opencms-shared/              # Shared libraries
│   ├── opencms-common/          # Common utilities
│   ├── opencms-api-contracts/   # Service contracts (DTOs, Feign clients)
│   ├── opencms-event-bus/       # Event bus client
│   ├── opencms-security-common/ # Security utilities
│   └── opencms-legacy-adapter/  # Strangler-fig migration adapter
│
├── opencms-api-gateway/         # API Gateway service
├── opencms-identity-service/    # Identity & Access Management
├── opencms-content-service/     # Content Repository
├── opencms-publishing-service/  # Publishing Workflow
├── opencms-module-service/      # Module Management
├── opencms-rendering-service/   # Rendering Service
├── opencms-search-service/      # Search Service
│
└── opencms-migration-tools/     # Migration utilities
    ├── data-migration/          # Database migration scripts
    ├── dual-write-proxy/        # Dual-write coordinator
    └── compatibility-tests/     # Integration tests
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Kubernetes (optional, for production deployment)

## Quick Start

### 1. Build All Services

```bash
mvn clean install
```

### 2. Start Infrastructure (Postgres, Redis, Kafka)

```bash
docker-compose up -d postgres redis kafka
```

### 3. Run Identity Service (Example)

```bash
cd opencms-identity-service/identity-application
mvn spring-boot:run
```

### 4. Run API Gateway

```bash
cd opencms-api-gateway
mvn spring-boot:run
```

## Migration Strategy

### Phase 1: Identity Service Extraction

1. **Deploy Identity Service** alongside monolith
2. **Enable dual-write mode** - Write to both monolith and Identity Service
3. **Route authentication** - Gradually route auth requests to Identity Service
4. **Verify consistency** - Run compatibility tests
5. **Decommission monolith auth** - Remove auth code from monolith

**Configuration:**

```yaml
# application.yml
opencms:
  migration:
    identity-service:
      enabled: true
      rollout-percentage: 10  # Start with 10% of users
```

### Phase 2: Content Service Extraction

Similar approach for VFS operations.

### Phase 3: Publishing Service Extraction

Extract publish workflow after Content Service is stable.

## Shared Libraries

### opencms-common

Common utilities (exceptions, logging, date/time, validation).

### opencms-api-contracts

Service-to-service API contracts. Contains:
- DTOs (UserDto, ResourceDto, etc.)
- Feign clients (IdentityServiceClient, ContentServiceClient)
- Event definitions

### opencms-legacy-adapter

Strangler-fig migration adapter. Contains:
- `DualWriteCoordinator` - Coordinates writes to monolith and microservice
- `RoutingDecisionEngine` - Determines routing (monolith vs. microservice)
- Legacy adapters for CmsSecurityManager, CmsDriverManager

## Service Architecture

Each service follows a 4-layer architecture:

```
{service}-api/          # REST controllers, DTOs, OpenAPI spec
{service}-core/         # Business logic, domain models, port interfaces
{service}-persistence/  # JPA entities, repositories, adapters
{service}-application/  # Spring Boot main, configuration
```

**Benefits:**
- Clear separation of concerns
- Framework-agnostic core logic
- Easy to test each layer
- API module can be shared with clients

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify -P integration-tests
```

### Compatibility Tests (Monolith vs. Microservice)

```bash
cd opencms-migration-tools/compatibility-tests
mvn test
```

## Code Quality & Security Scanning

### SonarCloud Analysis

The project includes SonarCloud integration for continuous code quality and security scanning.

**Quick Start:**

```bash
# 1. Configure .env.sonar with your SonarCloud credentials
# 2. Run analysis
./run-sonar-analysis.sh
```

**What's Analyzed:**
- Security vulnerabilities (SQL injection, XSS, hardcoded credentials)
- Bugs and code smells
- Code coverage (target: 60%+)
- Code duplications
- Security hotspots

**Documentation:**
- [SonarCloud Quick Start](SONARCLOUD_QUICK_START.md) - 3-minute setup
- [SonarCloud Detailed Setup](SONARCLOUD_SETUP.md) - Complete guide
- [Local SonarQube Setup](SONARQUBE_SETUP.md) - For local server (alternative)

**View Results:**
https://sonarcloud.io/project/overview?id=opencms-microservices

## Deployment

### Local Development (Docker Compose)

```bash
docker-compose up
```

### Kubernetes

```bash
kubectl apply -f kubernetes/
```

## Monitoring & Observability

- **Metrics:** Micrometer + Prometheus
- **Tracing:** OpenTelemetry + Jaeger
- **Logging:** Logback + ELK Stack
- **Health Checks:** Spring Boot Actuator

## Configuration

### Environment Variables

```bash
# Identity Service
IDENTITY_DB_URL=jdbc:postgresql://localhost:5432/identity
IDENTITY_DB_USERNAME=opencms
IDENTITY_DB_PASSWORD=secret

# Redis (Session Storage)
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka (Event Bus)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Feature Flags

```yaml
opencms:
  migration:
    identity-service:
      enabled: true
      rollout-percentage: 50
    content-service:
      enabled: false
```

## Contributing

1. Create feature branch from `main`
2. Follow existing code structure and naming conventions
3. Add unit tests for new functionality
4. Run `mvn clean verify` before committing
5. Submit pull request

## Documentation

- [Architecture Proposal](../doc/microservice-architecture-proposal.md)
- [Repository Layout](../doc/microservice-repository-layout.md)
- [Migration Guide](../doc/migration-guide.md) (TODO)
- [API Documentation](../doc/api-docs/) (TODO)

## License

Same as OpenCms core (LGPL 2.1)
