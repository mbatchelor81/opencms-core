# OpenCms Microservice Architecture Proposal

## Executive Summary

This document proposes a microservice decomposition of the current OpenCms monolithic architecture based on the identified business flows: Request Pipeline, Authentication, VFS Persistence, Publishing, and System Initialization.

---

## Current Architecture Analysis

### Monolithic Components (from Codemap)

1. **OpenCmsServlet** - HTTP ingress point
2. **OpenCmsCore** - Central orchestrator for all operations
3. **CmsSecurityManager** - Authentication and authorization
4. **CmsDriverManager** - Database abstraction layer
5. **CmsPublishEngine** - Publishing workflow coordinator
6. **CmsModuleManager** - Module lifecycle management
7. **CmsResourceManager** - Resource type and loader registry
8. **CmsSessionManager** - Session state management

### Identified Bounded Contexts

1. **API Gateway & Routing** - Request ingress, routing, session validation
2. **Identity & Access Management** - Authentication, authorization, user/group management
3. **Content Repository** - VFS resource CRUD, versioning, metadata
4. **Publishing Workflow** - Publish job orchestration, online/offline project management
5. **Module Management** - Module lifecycle, configuration, resource types
6. **Resource Rendering** - Content transformation, loader execution, MIME handling

---

## Proposed Microservice Architecture

### High-Level Topology

```
┌───────────────────────────────────────────────────────────┐
│                        API Gateway Layer                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Ingress    │  │   Session    │  │   Rate       │     │
│  │   Controller │  │   Validator  │  │   Limiter    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└───────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Identity   │      │   Content    │      │  Publishing  │
│   Service    │      │   Service    │      │   Service    │
│              │      │              │      │              │
│ - Auth       │      │ - VFS CRUD   │      │ - Job Queue  │
│ - Sessions   │      │ - Versioning │      │ - Workflow   │
│ - Users      │      │ - Metadata   │      │ - History    │
│ - Groups     │      │ - Locking    │      │              │
│ - ACLs       │      │              │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
        │                     │                     │
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Identity   │      │   Content    │      │  Publishing  │
│   Database   │      │   Database   │      │   Database   │
│  (Postgres)  │      │  (Postgres)  │      │  (Postgres)  │
└──────────────┘      └──────────────┘      └──────────────┘

        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Module     │      │   Rendering  │      │   Search     │
│   Service    │      │   Service    │      │   Service    │
│              │      │              │      │              │
│ - Registry   │      │ - Loaders    │      │ - Indexing   │
│ - Lifecycle  │      │ - Transform  │      │ - Query      │
│ - Config     │      │ - MIME       │      │ - Facets     │
│              │      │ - Cache      │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Module     │      │   Rendering  │      │ Elasticsearch│
│   Database   │      │   Cache      │      │   Cluster    │
│  (Postgres)  │      │   (Redis)    │      │              │
└──────────────┘      └──────────────┘      └──────────────┘

                ┌─────────────────────────────┐
                │   Cross-Cutting Services    │
                ├─────────────────────────────┤
                │ - Event Bus (Kafka/RabbitMQ)│
                │ - Distributed Tracing       │
                │ - Centralized Logging       │
                │ - Service Discovery         │
                │ - Configuration Service     │
                └─────────────────────────────┘
```

---

## Service Definitions

### 1. API Gateway Service

**Responsibilities:**
- HTTP request ingress and routing
- SSL termination
- Request/response transformation
- Rate limiting and throttling
- Session token validation (delegated to Identity Service)
- Request context propagation (site root, project, locale)

**Technology Stack:**
- Kong, Nginx, or Spring Cloud Gateway
- Redis for rate limiting state

**Key Endpoints:**
- `/*` - Route to appropriate backend service
- `/system/*` - System administration routes
- `/export/*` - Static resource export routes

**Mapping from Current Architecture:**
- Replaces: `OpenCmsServlet.doGet()` routing logic
- Delegates session restoration to Identity Service
- Maintains request stack context as distributed trace

---

### 2. Identity & Access Management Service

**Responsibilities:**
- User authentication (session, basic auth, OAuth, SAML)
- Session lifecycle management
- User and group CRUD operations
- Permission and ACL evaluation
- Authorization handler plugin system
- Password hashing and credential storage

**Technology Stack:**
- Spring Boot with Spring Security
- Redis for session storage (distributed)
- Postgres for user/group/ACL persistence

**Key APIs:**
```
POST   /auth/login              - Authenticate user
POST   /auth/logout             - Invalidate session
GET    /auth/session/{token}    - Validate session token
GET    /auth/user/{id}          - Get user details
POST   /auth/authorize          - Check resource permissions
GET    /auth/groups/{userId}    - Get user groups
PUT    /auth/acl/{resourceId}   - Update resource ACL
```

**Data Ownership:**
- `CMS_USERS` table
- `CMS_GROUPS` table
- `CMS_GROUPUSERS` table
- `CMS_ACCESSCONTROL` table
- Session state (Redis)

**Mapping from Current Architecture:**
- Replaces: `CmsSecurityManager`, `CmsSessionManager`
- Implements: `CmsDefaultAuthorizationHandler`, `I_CmsAuthorizationHandler`
- Trace 2: Session restoration logic
- Trace 3: Authorization handler flow

**Integration Points:**
- Publishes `UserLoggedIn`, `UserLoggedOut`, `SessionExpired` events
- Subscribes to `ResourceCreated` for ACL initialization
- Calls Content Service for resource permission checks

---

### 3. Content Repository Service

**Responsibilities:**
- VFS resource CRUD operations
- Resource versioning and history
- Resource locking (pessimistic concurrency)
- Metadata and property management
- Offline/Online project management
- Resource type registry
- Permalink and alias resolution

**Technology Stack:**
- Spring Boot
- Postgres for VFS metadata
- S3/MinIO for binary content storage
- Redis for distributed locks

**Key APIs:**
```
GET    /vfs/resource/{path}           - Read resource
POST   /vfs/resource                  - Create resource
PUT    /vfs/resource/{path}           - Update resource
DELETE /vfs/resource/{path}           - Delete resource
POST   /vfs/resource/{path}/lock      - Lock resource
DELETE /vfs/resource/{path}/lock      - Unlock resource
GET    /vfs/resource/{path}/history   - Get version history
GET    /vfs/resource/{path}/properties - Get properties
PUT    /vfs/resource/{path}/properties - Update properties
GET    /vfs/permalink/{uuid}          - Resolve permalink
GET    /vfs/alias/{alias}             - Resolve alias
```

**Data Ownership:**
- `CMS_RESOURCES` table
- `CMS_CONTENTS` table
- `CMS_RESOURCE_HISTORY` table
- `CMS_RESOURCE_LOCKS` table
- `CMS_PROPERTIES` table
- `CMS_RELATIONS` table
- Binary content blobs (S3)

**Mapping from Current Architecture:**
- Replaces: `CmsDriverManager` (VFS operations), `CmsVfsDriver`
- Implements: `CmsObject.readResource()`, `CmsObject.writeResource()`
- Trace 4: VFS resource persistence flow
- Trace 7: Resource init handlers (permalink, alias)

**Integration Points:**
- Publishes `ResourceCreated`, `ResourceUpdated`, `ResourceDeleted`, `ResourceLocked` events
- Calls Identity Service for permission checks
- Calls Module Service for resource type validation
- Subscribes to `PublishJobCompleted` for online project updates

---

### 4. Publishing Workflow Service

**Responsibilities:**
- Publish job queue management
- Publish list generation and validation
- Offline → Online project synchronization
- Publish history tracking
- Publish job scheduling and retry logic
- Publish event notification

**Technology Stack:**
- Spring Boot
- Postgres for publish history
- RabbitMQ/Kafka for job queue
- Temporal.io or Camunda for workflow orchestration

**Key APIs:**
```
POST   /publish/job                - Enqueue publish job
GET    /publish/job/{id}           - Get job status
GET    /publish/job/{id}/progress  - Get job progress
DELETE /publish/job/{id}           - Cancel job
GET    /publish/history            - Query publish history
GET    /publish/queue              - Get pending jobs
```

**Data Ownership:**
- `CMS_PUBLISH_JOBS` table
- `CMS_PUBLISH_HISTORY` table
- `CMS_PUBLISH_RESOURCES` table (job → resource mapping)

**Mapping from Current Architecture:**
- Replaces: `CmsPublishEngine`, `CmsPublishThread`, `CmsPublishManager`
- Trace 5: Publishing workflow execution

**Integration Points:**
- Publishes `PublishJobEnqueued`, `PublishJobStarted`, `PublishJobCompleted`, `PublishJobFailed` events
- Calls Content Service to copy resources from offline → online project
- Calls Identity Service for user context
- Subscribes to `ResourceUpdated` for publish list generation

---

### 5. Module Management Service

**Responsibilities:**
- Module registry and metadata
- Module lifecycle (install, update, uninstall)
- Resource type registration
- Explorer type configuration
- Module dependency resolution
- Module export/import

**Technology Stack:**
- Spring Boot
- Postgres for module metadata
- S3 for module packages (.zip)

**Key APIs:**
```
GET    /modules                    - List installed modules
GET    /modules/{name}             - Get module details
POST   /modules/install            - Install module
DELETE /modules/{name}             - Uninstall module
PUT    /modules/{name}             - Update module
GET    /modules/{name}/resources   - Get module resources
GET    /resource-types             - List resource types
POST   /resource-types             - Register resource type
```

**Data Ownership:**
- `CMS_MODULES` table
- `CMS_MODULE_RESOURCES` table
- `CMS_RESOURCE_TYPES` table
- Module packages (S3)

**Mapping from Current Architecture:**
- Replaces: `CmsModuleManager`, `CmsModuleConfiguration`
- Trace 6: Module manager initialization

**Integration Points:**
- Publishes `ModuleInstalled`, `ModuleUninstalled`, `ResourceTypeRegistered` events
- Calls Content Service for module resource installation
- Subscribes to system initialization events

---

### 6. Rendering Service

**Responsibilities:**
- Resource loader execution (JSP, dump, image, etc.)
- Content transformation and templating
- MIME type resolution
- Response caching
- Static resource serving
- Protected file access control

**Technology Stack:**
- Spring Boot
- Redis for response cache
- CDN integration (CloudFront, Cloudflare)

**Key APIs:**
```
GET    /render/{path}              - Render resource
GET    /render/{path}?preview=true - Preview unpublished
POST   /render/invalidate          - Invalidate cache
GET    /static/{path}              - Serve static resource
```

**Data Ownership:**
- Rendered content cache (Redis)
- No persistent database

**Mapping from Current Architecture:**
- Replaces: `CmsResourceManager.loadResource()`, `I_CmsResourceLoader` implementations
- Trace 1: Resource rendering delegation
- Trace 7: Protected static file handler

**Integration Points:**
- Calls Content Service to read resource content
- Calls Identity Service for access control
- Subscribes to `ResourceUpdated`, `PublishJobCompleted` for cache invalidation

---

### 7. Search Service

**Responsibilities:**
- Content indexing
- Full-text search
- Faceted search
- Search result ranking
- Index management

**Technology Stack:**
- Elasticsearch or Solr
- Spring Boot for API layer
- Kafka for indexing events

**Key APIs:**
```
POST   /search/query               - Execute search query
POST   /search/index/{resourceId}  - Index resource
DELETE /search/index/{resourceId}  - Remove from index
POST   /search/reindex             - Full reindex
```

**Data Ownership:**
- Elasticsearch indices

**Mapping from Current Architecture:**
- Replaces: `CmsSearchManager`, `CmsSearchIndex`

**Integration Points:**
- Subscribes to `ResourceCreated`, `ResourceUpdated`, `ResourceDeleted`, `PublishJobCompleted` events
- Calls Content Service to read resource content for indexing

---

## Cross-Cutting Concerns

### Event Bus (Kafka/RabbitMQ)

**Purpose:** Asynchronous event-driven communication between services

**Key Event Topics:**
- `identity.user.logged-in`
- `identity.user.logged-out`
- `identity.session.expired`
- `content.resource.created`
- `content.resource.updated`
- `content.resource.deleted`
- `content.resource.locked`
- `publish.job.enqueued`
- `publish.job.completed`
- `publish.job.failed`
- `module.installed`
- `module.uninstalled`

### Service Discovery (Consul/Eureka)

**Purpose:** Dynamic service registration and discovery

### Distributed Tracing (Jaeger/Zipkin)

**Purpose:** Request flow visualization across services

**Mapping from Current Architecture:**
- Replaces: `OpenCmsServlet` request stack

### Centralized Logging (ELK Stack)

**Purpose:** Aggregated log collection and analysis

### Configuration Service (Spring Cloud Config)

**Purpose:** Externalized configuration management

---

## Data Architecture

### Database Per Service Pattern

Each service owns its database schema:

1. **Identity DB** - Users, groups, ACLs, sessions
2. **Content DB** - Resources, versions, properties, locks
3. **Publishing DB** - Publish jobs, history
4. **Module DB** - Modules, resource types

### Shared Data Challenges

**Challenge:** Resource permissions require both Identity and Content data

**Solution:** 
- Identity Service provides `POST /auth/authorize` endpoint
- Content Service calls Identity Service for permission checks
- Cache permission results in Redis with TTL

**Challenge:** Publishing requires reading offline resources and writing to online project

**Solution:**
- Content Service exposes project-scoped APIs: `/vfs/resource/{path}?project=offline`
- Publishing Service orchestrates copy operations via Content Service API

---

## Migration Strategy

### Phase 1: Strangler Fig Pattern - Extract Identity Service

1. Deploy Identity Service alongside monolith
2. Dual-write user/session data to both monolith DB and Identity DB
3. Route authentication requests to Identity Service
4. Gradually migrate authorization checks to Identity Service
5. Remove authentication code from monolith

### Phase 2: Extract Content Service

1. Deploy Content Service with read-only access to monolith DB
2. Route read operations to Content Service
3. Enable write operations in Content Service
4. Dual-write to both services
5. Migrate to Content Service as primary

### Phase 3: Extract Publishing Service

1. Deploy Publishing Service
2. Route publish job creation to Publishing Service
3. Publishing Service calls Content Service APIs
4. Decommission monolith publish engine

### Phase 4: Extract Module and Rendering Services

1. Deploy Module Service
2. Deploy Rendering Service with CDN
3. Route rendering requests through API Gateway
4. Decommission monolith

---

## Service Communication Patterns

### Synchronous (REST/gRPC)

- API Gateway → All Services
- Rendering Service → Content Service (read resources)
- Content Service → Identity Service (permission checks)
- Publishing Service → Content Service (resource copy)

### Asynchronous (Event-Driven)

- Content Service → Search Service (indexing)
- Publishing Service → Rendering Service (cache invalidation)
- Identity Service → All Services (session expiration)

### Request-Response with Timeout

- All synchronous calls use circuit breakers (Resilience4j)
- Fallback strategies for degraded service scenarios

---

## Scalability Considerations

### Horizontal Scaling

- **Identity Service:** Scale based on authentication load
- **Content Service:** Scale based on VFS read/write operations
- **Rendering Service:** Scale based on page view traffic
- **Publishing Service:** Fixed pool of workers (2-4 instances)

### Caching Strategy

- **Identity Service:** Cache user/group data in Redis (5 min TTL)
- **Content Service:** Cache resource metadata in Redis (1 min TTL)
- **Rendering Service:** Cache rendered pages in Redis + CDN (configurable TTL)

### Database Optimization

- **Content DB:** Partition `CMS_RESOURCES` by project (offline/online)
- **Identity DB:** Index `CMS_USERS.username`, `CMS_ACCESSCONTROL.resource_id`
- **Publishing DB:** Archive old publish history to cold storage

---

## Security Architecture

### Authentication Flow

```
1. Client → API Gateway (with credentials)
2. API Gateway → Identity Service (authenticate)
3. Identity Service → API Gateway (JWT token)
4. API Gateway → Client (JWT token)
5. Client → API Gateway (JWT in header)
6. API Gateway validates JWT signature
7. API Gateway → Backend Service (with user context)
```

### Authorization Flow

```
1. Content Service receives request with user context
2. Content Service → Identity Service (check permission)
3. Identity Service evaluates ACL
4. Identity Service → Content Service (allow/deny)
5. Content Service proceeds or returns 403
```

### Secrets Management

- Use HashiCorp Vault or AWS Secrets Manager
- Rotate database credentials automatically
- Store API keys and JWT signing keys in Vault

---

## Monitoring & Observability

### Key Metrics

**Identity Service:**
- Authentication success/failure rate
- Session creation rate
- Active session count
- Permission check latency

**Content Service:**
- VFS read/write throughput
- Resource lock contention
- Database connection pool utilization
- S3 upload/download latency

**Publishing Service:**
- Publish job queue depth
- Publish job duration (p50, p95, p99)
- Publish failure rate

**Rendering Service:**
- Cache hit rate
- Page render latency
- CDN hit rate

### Health Checks

- `/actuator/health` endpoint on all services
- Database connectivity check
- Dependent service connectivity check
- Disk space check (for services with local storage)

### Alerting

- Authentication failure rate > 10%
- Publish job queue depth > 100
- Content Service database connection pool exhausted
- Rendering Service cache hit rate < 50%

---

## Cost Optimization

### Compute

- Use Kubernetes with Horizontal Pod Autoscaler
- Scale down non-critical services during off-peak hours
- Use spot instances for Publishing Service workers

### Storage

- Use S3 Intelligent-Tiering for binary content
- Archive old publish history to Glacier
- Compress rendered content in cache

### Network

- Use CDN for static resources and rendered pages
- Enable gzip compression on API Gateway
- Use VPC peering to avoid inter-region data transfer costs

---

## Comparison: Monolith vs. Microservices

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Deployment** | Single WAR file | 7+ independent services |
| **Scaling** | Vertical only | Horizontal per service |
| **Technology** | Java/JSP only | Polyglot (Java, Node.js, Go) |
| **Database** | Single Postgres | 4+ databases + Redis + S3 |
| **Failure Isolation** | Entire system down | Service-level isolation |
| **Development** | Single codebase | Distributed teams |
| **Complexity** | Low operational | High operational |
| **Performance** | In-process calls | Network latency |

---

## Recommended Next Steps

1. **Proof of Concept:** Extract Identity Service as standalone microservice
2. **Load Testing:** Benchmark authentication performance (monolith vs. microservice)
3. **Cost Analysis:** Estimate infrastructure costs for full microservice deployment
4. **Team Training:** Upskill team on Kubernetes, service mesh, distributed tracing
5. **Pilot Deployment:** Deploy Identity + Content services for single customer
6. **Gradual Rollout:** Migrate customers incrementally using feature flags

---

## Conclusion

This microservice architecture provides:

- **Independent Scalability:** Scale rendering and content services independently
- **Technology Flexibility:** Use optimal tech stack per service (e.g., Go for high-throughput rendering)
- **Fault Isolation:** Identity service failure doesn't impact content reads for authenticated users
- **Team Autonomy:** Separate teams can own Identity, Content, Publishing services
- **Deployment Velocity:** Deploy rendering service updates without touching content service

**Trade-offs:**

- Increased operational complexity (monitoring, deployment, debugging)
- Network latency for inter-service calls
- Distributed transaction challenges (eventual consistency)
- Higher infrastructure costs (7+ services vs. 1 monolith)

**Recommendation:** Start with Identity Service extraction to validate the approach before committing to full microservice migration.
