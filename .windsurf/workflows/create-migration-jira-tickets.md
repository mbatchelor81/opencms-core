---
description: Create well-scoped Jira tickets for microservices migration phases
auto_execution_mode: 3
---

# Create Migration Jira Tickets

**Usage:** "Create the Jira tickets for phase X" → Tag this workflow

---

## Workflow Steps

### 1. Read Phase from Migration Guide
- Read `opencms-microservices/migration-docs/MIGRATION_GUIDE.md`
- Extract the specified phase section (e.g., Phase 2: Content Service)
- Identify: goal, duration, week breakdown, components, success criteria

### 2. Consolidate into 4-6 Tickets
**Pattern:** 1 Epic + 3-5 Stories (one per week or logical grouping)

**Consolidation Rules:**
- ✅ Combine: deployment + dual-write + monitoring → 1 ticket
- ✅ Combine: all rollout stages (10%→50%→100%) → 1 ticket  
- ✅ Combine: decommission steps (disable + remove + archive) → 1 ticket
- ❌ Don't split: monitoring from feature, API endpoints into separate tickets
- ❌ Don't reference: API Gateway or Service Discovery if not yet implemented

### 3. Create Epic in Jira

**Summary:** `[Phase X] <Service> Migration – <Goal>`

**Description Template:**
```markdown
## Context
**Current State:** <Monolith functionality>
**Target State:** <Microservice functionality>
**Related Business Flows:** <Codemap references if available>

## Timeline
**Duration:** X weeks
- Week Y-Z: <Phase name>

## Success Criteria
- ✅ <Measurable metric with number>
- ✅ <Performance target>
- ✅ <Stability target>

## Risk Mitigation
- <Rollback strategy>
- <Monitoring approach>

## Dependencies
- <Only existing services from docker-compose.yml>
```

**Jira Command:**
```
mcp1_createJiraIssue(cloudId, projectKey="OC", issueTypeName="Epic", summary, description)
```

### 4. Create Story Tickets (One Per Week)

**Summary:** `[Phase X - Week Y] <Concise Work Description>`

**Description Template:**
```markdown
## Context
- **Current servlet responsibility:** <Monolith component>
- **Target microservice:** <Service name>
- **Related tickets:** <Epic key>
- **Business Flow:** <Codemap trace if available>

## Scope
**This ticket covers Week Y: <Phase Name>**

### 1. <Work Stream Name>
- <Deliverable>
- <Deliverable>

### 2. <Work Stream Name>
- <Deliverable>

### 3. <Work Stream Name>
- <Deliverable>

## API Contracts
- POST /api/<endpoint> - <Description>
- GET /api/<endpoint> - <Description>

## Migration Approach
- **Cutover strategy:** <Specific steps, percentages, configs>
- **Rollback plan:** <Fast rollback method, time estimate>

## Acceptance Criteria
- [ ] <Specific deliverable with metric>
- [ ] <Performance requirement with number>
- [ ] <Test coverage requirement>
- [ ] <Monitoring requirement>

## Dependencies
- **Services:** <Only from docker-compose.yml>
- **Teams:** <Who's involved>

## Notes
- <Architecture decisions>
- <What to avoid>
```

**Jira Command:**
```
mcp1_createJiraIssue(cloudId, projectKey="OC", issueTypeName="Task", summary, description, parent=epicKey)
```

### 5. Create Documentation Ticket (Parallel)

**Summary:** `[Phase X] Update Documentation and Runbooks`

**Scope:** Architecture docs, API docs, runbooks, troubleshooting guides

**Note:** This runs in parallel with all other tickets

---

## Quality Checklist

**Before creating tickets, validate:**
- [ ] Epic has measurable success criteria with numbers
- [ ] Each story covers ~1 week of work
- [ ] Dependencies only reference existing infrastructure
- [ ] Rollback plans are fast and specific
- [ ] Acceptance criteria are measurable (not "works well")
- [ ] No references to unimplemented API Gateway/Service Discovery
- [ ] Monitoring/observability included in each ticket

---

## Execution Commands

```bash
# 1. Get Atlassian access
mcp1_getAccessibleAtlassianResources()

# 2. Get project
mcp1_getVisibleJiraProjects(cloudId, searchString="opencms")

# 3. Create Epic
epicKey = mcp1_createJiraIssue(cloudId, "OC", "Epic", summary, description)

# 4. Create Stories (repeat for each week)
mcp1_createJiraIssue(cloudId, "OC", "Task", summary, description, parent=epicKey)

# 5. Create doc ticket
mcp1_createJiraIssue(cloudId, "OC", "Task", docSummary, docDescription, parent=epicKey)
```

---

## Example Output (Phase 2)

**Epic:** `[Phase 2] Content Service Migration – VFS Operations Extraction`

**Stories:**
1. `[Phase 2 - Weeks 5-6] Deploy Content Service and Enable Dual-Write`
2. `[Phase 2 - Weeks 7-8] Migrate Read Operations to Content Service`  
3. `[Phase 2 - Weeks 9-10] Migrate Write Operations and Decommission VFS`
4. `[Phase 2] Update Documentation and Runbooks`

---

## References

- **Ticket Format Rules:** `.windsurf/rules/jira-ticket-format.md`
- **Migration Guide:** `opencms-microservices/migration-docs/MIGRATION_GUIDE.md`