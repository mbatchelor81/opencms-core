---
trigger: model_decision
description: Use this rule when we are using the Jira MCP server to create tickets and Epics
---

# Jira Ticket Format: Servlet to Microservices Migration

Standard format for migration tickets. Use this structure to ensure consistency across the team.

---

## Ticket Metadata

| Field | Format |
| ----- | ------ |
| **Issue Type** | `Story`, `Task`, or `Bug` |
| **Summary** | `[Service:<target>] <component> – <action>`<br>Example: `[Service:Auth] org.opencms.main – Extract session init` |
| **Labels** | `migration`, `servlet-to-microservices`, `svc-<name>` |
| **Priority** | `P2` (default), `P1` (blocking), `P3`/`P4` (backlog) |

---

## Description Template

```
## Context
- Current servlet responsibility:
- Target microservice:
- Related tickets:

## Scope
- Functional changes:
- API contracts:

## Migration Approach
- Cutover strategy:
- Rollback plan:

## Acceptance Criteria
- [ ] API parity validated
- [ ] Performance benchmarked
- [ ] Rollout plan documented

## Dependencies
- Services:
- Teams:
```

---

## Definition of Done
- Acceptance criteria met
- Legacy path removed or feature-flagged
- Documentation updated
