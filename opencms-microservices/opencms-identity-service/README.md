# Identity Service

Authentication and authorization microservice for OpenCms.

## Overview

The Identity Service handles:
- User authentication (username/password)
- Session management
- User CRUD operations
- Password hashing (BCrypt)

Part of **Phase 1** migration from OpenCms monolith.

## Architecture

```
identity-api/          # REST controllers, DTOs
identity-core/         # Business logic, domain models
identity-persistence/  # JPA entities, repositories
identity-application/  # Spring Boot main, configuration
```

## Quick Start

### 1. Start PostgreSQL Database

```bash
cd ../
docker-compose up -d postgres-identity
```

### 2. Run the Service

```bash
cd identity-application
mvn spring-boot:run
```

The service will start on **http://localhost:8081**

### 3. Verify Health

```bash
curl http://localhost:8081/actuator/health
```

## API Endpoints

### Create User
```bash
POST /api/identity/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Authenticate User
```bash
POST /api/identity/authenticate
Content-Type: application/json

{
  "username": "johndoe",
  "password": "secret123"
}
```

### Get User by ID
```bash
GET /api/identity/users/{id}
```

### Update User
```bash
PUT /api/identity/users/{id}
Content-Type: application/json

{
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Smith",
  "enabled": true
}
```

## Configuration

### Database
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/identity
    username: opencms
    password: opencms123
```

### Actuator Endpoints
- `/actuator/health` - Health check
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics

## Database Schema

Flyway migration: `V1__create_users_table.sql`

**users** table:
- `id` (UUID) - Primary key
- `username` (VARCHAR) - Unique
- `email` (VARCHAR) - Unique
- `password_hash` (VARCHAR) - BCrypt hashed
- `first_name`, `last_name` (VARCHAR)
- `enabled` (BOOLEAN)
- `created_date`, `last_modified` (TIMESTAMP)
- `version` (BIGINT) - Optimistic locking

## Monitoring

### Prometheus Metrics
```bash
curl http://localhost:8081/actuator/prometheus
```

### Grafana Dashboard
Import `monitoring/grafana/identity-service-dashboard.json`

Key metrics:
- Request rate
- Response time (p95)
- Error rate
- JVM memory usage
- Database connection pool

## Docker Deployment

### Build Image
```bash
docker build -t opencms-identity-service:latest .
```

### Run with Docker Compose
```bash
cd ../
docker-compose up identity-service
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

## Development

### Build
```bash
mvn clean package
```

### Run with Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Phase 1 Implementation Status

✅ **Week 1 - Completed:**
- [x] Spring Boot application
- [x] REST API endpoints
- [x] PostgreSQL database with Flyway
- [x] Health check endpoints
- [x] Docker deployment
- [x] Prometheus/Grafana monitoring

🔄 **Week 1 - In Progress:**
- [ ] Dual-write coordinator
- [ ] Identity service client
- [ ] Circuit breaker pattern
- [ ] Feature flags
- [ ] Comprehensive tests

## Next Steps

1. Implement `DualWriteCoordinator` in `opencms-legacy-adapter`
2. Create `IdentityServiceClient` for monolith integration
3. Add circuit breaker with Resilience4j
4. Implement feature flag control
5. Write comprehensive integration tests

## Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres-identity

# View logs
docker logs opencms-identity-db
```

### Service Won't Start
```bash
# Check application logs
tail -f logs/identity-service.log

# Verify port 8081 is available
lsof -i :8081
```

## Related Documentation

- [Phase 1 Jira Tickets](../PHASE1_JIRA_TICKETS.md)
- [Migration Guide](../migration-docs/MIGRATION_GUIDE.md)
- [Architecture Proposal](../../doc/microservice-architecture-proposal.md)
