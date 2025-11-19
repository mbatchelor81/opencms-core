---
description: Run a SonarQube scan and retrieve the results with MCP Server to provide overview
auto_execution_mode: 3
---

# SonarQube Analysis Workflow

## Prerequisites
- ✅ SonarQube/SonarCloud account with organization access
- ✅ Maven installed
- ✅ `.env.sonar` file configured with credentials
- ✅ SonarQube MCP server available

## Workflow Steps

### 1. Execute Scan
Run the analysis script:
```bash
cd opencms-microservices
./run-sonar-analysis.sh
```

The script will:
- Clean previous builds
- Run tests with coverage (`mvn verify`)
- Upload results to SonarQube (`mvn sonar:sonar`)

### 2. Retrieve Analysis Results via MCP

#### 2.1 Search for Issues
```
Use SonarQube MCP: search_sonar_issues_in_projects
- projects: ["mbatchelor81_opencms-core"]
- severities: "HIGH,BLOCKER"
- ps: 100 (page size)
```

#### 2.2 Get Quality Gate Status
```
Use SonarQube MCP: get_project_quality_gate_status
- projectKey: "mbatchelor81_opencms-core"
```

#### 2.3 Get Component Measures
```
Use SonarQube MCP: get_component_measures
- component: "mbatchelor81_opencms-core"
- metricKeys: ["bugs", "vulnerabilities", "code_smells", "coverage", "duplicated_lines_density"]
```

#### 2.4 View Specific Issue Details
```
Use SonarQube MCP: change_sonar_issue_status (if needed)
- key: "issue_key_from_search"
- status: ["accept", "falsepositive", "reopen"]
```

### 3. Review and Action
- Analyze retrieved metrics and issues
- Prioritize high/blocker severity items
- Update issue statuses as needed
- Track quality gate compliance

## Quick Reference

**Project Key**: `mbatchelor81_opencms-core`  
**Scan Command**: `./run-sonar-analysis.sh`  
**Key Metrics**: bugs, vulnerabilities, code_smells, coverage, duplicated_lines_density
