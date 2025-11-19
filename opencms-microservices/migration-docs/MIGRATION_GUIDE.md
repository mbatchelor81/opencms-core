# OpenCms Microservices - Migration Guide

## Overview

This guide describes the incremental strangler-fig migration from the OpenCms monolith to microservices.

## Migration Phases

### Phase 1: Identity Service (Weeks 1-4)

**Goal:** Extract authentication and authorization to Identity Service

#### Week 1: Setup & Dual-Write

1. Deploy Identity Service alongside monolith
2. Configure dual-write in `opencms-legacy-adapter`
3. Start writing user data to both systems

```java
// In monolith, inject DualWriteCoordinator
@Autowired
private DualWriteCoordinator dualWriteCoordinator;

public CmsUser createUser(String username, String password) {
    CmsUser user = legacySecurityManager.createUser(username, password);
    
    // Shadow write to microservice
    dualWriteCoordinator.asyncWrite(() -> {
        identityServiceClient.createUser(mapToDto(user));
    });
    
    return user;
}
```

#### Week 2: Read Verification

1. Run compatibility tests to verify data consistency
2. Monitor dual-write success rate
3. Fix any data mapping issues

```bash
cd opencms-migration-tools/compatibility-tests
mvn test -Dtest=AuthenticationCompatibilityTest
```

#### Week 3: Gradual Rollout

1. Enable routing to Identity Service for 10% of users
2. Monitor authentication success rate
3. Gradually increase to 50%, then 100%

```yaml
# application.yml
opencms:
  migration:
    identity-service:
      enabled: true
      rollout-percentage: 10  # Increase gradually
```

#### Week 4: Decommission

1. Route 100% of authentication to Identity Service
2. Stop dual-write
3. Remove authentication code from monolith

### Phase 2: Content Service (Weeks 5-10)

**Goal:** Extract VFS operations to Content Service

#### Weeks 5-6: Setup & Dual-Write

Similar to Phase 1, but for VFS resource operations.

#### Weeks 7-8: Read Migration

1. Route read operations to Content Service
2. Keep writes in monolith
3. Verify read consistency

#### Weeks 9-10: Write Migration & Decommission

1. Route write operations to Content Service
2. Stop dual-write
3. Remove VFS driver code from monolith

### Phase 3: Publishing Service (Weeks 11-14)

**Goal:** Extract publish workflow to Publishing Service

#### Weeks 11-12: Setup

1. Deploy Publishing Service
2. Configure to call Content Service for resource operations

#### Weeks 13-14: Migration & Decommission

1. Route publish job creation to Publishing Service
2. Verify publish workflow
3. Remove publish engine from monolith

### Phase 4: Remaining Services (Weeks 15-20)

- Module Service (Weeks 15-16)
- Rendering Service (Weeks 17-18)
- Search Service (Weeks 19-20)

## Rollback Strategy

Each phase has a rollback plan:

1. **Disable routing** - Set `rollout-percentage: 0`
2. **Verify monolith** - Ensure monolith still has all data
3. **Monitor** - Check for any issues
4. **Re-enable gradually** - Fix issues and try again

## Monitoring During Migration

### Key Metrics

- **Dual-write success rate** - Should be > 99%
- **Authentication latency** - Compare monolith vs. microservice
- **Data consistency** - Run hourly consistency checks
- **Error rate** - Monitor for increased errors

### Alerts

```yaml
# Prometheus alerts
- alert: DualWriteFailureRate
  expr: dual_write_failure_rate > 0.01
  for: 5m
  annotations:
    summary: "Dual-write failure rate above 1%"

- alert: DataInconsistency
  expr: consistency_check_failures > 0
  for: 1m
  annotations:
    summary: "Data inconsistency detected"
```

## Data Migration Scripts

### Export Users from Monolith

```sql
-- Export users to CSV for verification
COPY (
  SELECT user_id, user_name, user_email, user_firstname, user_lastname
  FROM CMS_USERS
) TO '/tmp/users_export.csv' WITH CSV HEADER;
```

### Verify Identity Service Data

```sql
-- Compare counts
SELECT COUNT(*) FROM CMS_USERS;  -- Monolith
SELECT COUNT(*) FROM users;      -- Identity Service

-- Should match
```

## Testing Strategy

### Unit Tests

Test each service independently.

### Integration Tests

Test service-to-service communication.

### Compatibility Tests

Verify monolith and microservice produce identical results:

```java
@Test
public void testAuthenticationCompatibility() {
    // Authenticate via monolith
    CmsObject monolithResult = monolithAuth.authenticate("admin", "password");
    
    // Authenticate via Identity Service
    LoginResponse microserviceResult = identityService.authenticate("admin", "password");
    
    // Verify identical results
    assertEquals(monolithResult.getUserId(), microserviceResult.getUserId());
}
```

### Load Tests

Verify performance under load:

```bash
# JMeter or Gatling
gatling:test -Dgatling.simulationClass=IdentityServiceLoadTest
```

## Troubleshooting

### Dual-Write Failures

**Symptom:** High dual-write failure rate

**Solution:**
1. Check network connectivity to microservice
2. Verify database schema matches
3. Check for data mapping errors in logs

### Data Inconsistency

**Symptom:** Consistency checks failing

**Solution:**
1. Run data reconciliation script
2. Identify root cause (timing, mapping, etc.)
3. Fix and re-sync

### Performance Degradation

**Symptom:** Increased latency after migration

**Solution:**
1. Check database query performance
2. Enable caching (Redis)
3. Optimize network calls (batching, connection pooling)

## Success Criteria

### Phase 1 (Identity Service)

- ✅ 100% of authentication via Identity Service
- ✅ Zero data inconsistencies for 7 days
- ✅ Authentication latency < 100ms (p95)
- ✅ Zero authentication errors for 24 hours

### Phase 2 (Content Service)

- ✅ 100% of VFS operations via Content Service
- ✅ Zero data inconsistencies for 7 days
- ✅ Read latency < 50ms (p95)
- ✅ Write latency < 200ms (p95)

### Phase 3 (Publishing Service)

- ✅ 100% of publish jobs via Publishing Service
- ✅ Publish workflow completes successfully
- ✅ Zero publish failures for 7 days

## Post-Migration

### Decommission Monolith

1. Verify all services running in production
2. Archive monolith database
3. Shut down monolith application
4. Redirect all traffic to API Gateway

### Optimization

1. Enable CDN for Rendering Service
2. Tune database connection pools
3. Optimize cache TTLs
4. Enable auto-scaling for high-traffic services

## Support

For issues during migration:
- Check logs in ELK Stack
- Review metrics in Grafana
- Consult architecture documentation
- Contact microservices team
