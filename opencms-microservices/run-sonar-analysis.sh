#!/bin/bash

# SonarQube/SonarCloud Analysis Script for OpenCms Microservices
# This script runs a complete SonarQube analysis including tests and coverage

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Load environment variables from .env.sonar if it exists
if [ -f .env.sonar ]; then
    echo -e "${GREEN}Loading configuration from .env.sonar${NC}"
    export $(grep -v '^#' .env.sonar | xargs)
fi

# Configuration with defaults
SONAR_HOST_URL="${SONAR_HOST_URL:-https://sonarcloud.io}"
SONAR_TOKEN="${SONAR_TOKEN:-}"
SONAR_ORGANIZATION="${SONAR_ORGANIZATION:-}"
SONAR_PROJECT_KEY="${SONAR_PROJECT_KEY:-opencms-microservices}"
SKIP_JACOCO_CHECK="${SKIP_JACOCO_CHECK:-true}"

# Parse optional arguments
for arg in "$@"; do
    case "$arg" in
        --skip-jacoco-check)
            SKIP_JACOCO_CHECK="true"
            shift
            ;;
    esac
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}OpenCms Microservices - SonarQube Analysis${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed or not in PATH${NC}"
    exit 1
fi

# Validate required configuration
if [ -z "$SONAR_TOKEN" ]; then
    echo -e "${RED}Error: SONAR_TOKEN is not set${NC}"
    echo -e "${YELLOW}Please set it in .env.sonar file or export it:${NC}"
    echo -e "${YELLOW}  export SONAR_TOKEN=your_token_here${NC}"
    exit 1
fi

if [ -z "$SONAR_ORGANIZATION" ]; then
    echo -e "${RED}Error: SONAR_ORGANIZATION is not set${NC}"
    echo -e "${YELLOW}Please set it in .env.sonar file${NC}"
    exit 1
fi

# Display configuration
echo -e "Configuration:"
echo -e "  Sonar URL: ${SONAR_HOST_URL}"
echo -e "  Organization: ${SONAR_ORGANIZATION}"
echo -e "  Project Key: ${SONAR_PROJECT_KEY}"
echo -e "  Analysis Mode: Main branch (free tier)"
echo -e "  Skip JaCoCo Check: ${SKIP_JACOCO_CHECK}"
echo ""

# Prompt for confirmation
read -p "Do you want to proceed with the analysis? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Analysis cancelled${NC}"
    exit 0
fi

echo -e "${GREEN}Step 1: Cleaning previous builds...${NC}"
mvn clean

echo ""
echo -e "${GREEN}Step 2: Building and running tests with coverage...${NC}"
mvn verify -Dskip.jacoco.check="${SKIP_JACOCO_CHECK}"

echo ""
echo -e "${GREEN}Step 3: Running SonarCloud analysis...${NC}"

mvn sonar:sonar \
    -Dsonar.host.url="${SONAR_HOST_URL}" \
    -Dsonar.organization="${SONAR_ORGANIZATION}" \
    -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
    -Dsonar.token="${SONAR_TOKEN}" \
    -Dskip.jacoco.check="${SKIP_JACOCO_CHECK}"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Analysis Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "View results at: ${SONAR_HOST_URL}/project/overview?id=${SONAR_PROJECT_KEY}"
echo ""
