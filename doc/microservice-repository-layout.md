# OpenCms Microservices - Repository Layout & Migration Skeleton

## Repository Structure Overview

```
opencms-microservices/
├── pom.xml                                    # Parent POM
├── README.md
├── docker-compose.yml                         # Local development environment
├── kubernetes/                                # K8s deployment manifests
│
├── opencms-shared/                            # Shared libraries
│   ├── opencms-common/                        # Common utilities
│   ├── opencms-api-contracts/                 # Service API contracts
│   ├── opencms-event-bus/                     # Event bus client
│   ├── opencms-security-common/               # Security utilities
│   └── opencms-legacy-adapter/                # Strangler-fig adapter
│
├── opencms-api-gateway/                       # API Gateway Service
├── opencms-identity-service/                  # Identity & Access Management
├── opencms-content-service/                   # Content Repository
├── opencms-publishing-service/                # Publishing Workflow
├── opencms-module-service/                    # Module Management
├── opencms-rendering-service/                 # Rendering Service
├── opencms-search-service/                    # Search Service
│
└── opencms-migration-tools/                   # Migration utilities
    ├── data-migration/
    ├── dual-write-proxy/
    └── compatibility-tests/
```

## Module Architecture Pattern

Each service follows a 4-layer architecture:

```
opencms-{service}-service/
├── {service}-api/                 # REST controllers, DTOs
├── {service}-core/                # Business logic, domain models
├── {service}-persistence/         # JPA entities, repositories
├── {service}-application/         # Spring Boot main
└── pom.xml
```

## Detailed Service Structures

See full document for complete implementation details.
