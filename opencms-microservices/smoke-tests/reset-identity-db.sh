#!/bin/bash

echo "Stopping Identity Service (if running)..."
# User should manually stop the Spring Boot app with Ctrl+C

echo "Dropping and recreating identity database..."
docker exec opencms-identity-db psql -U opencms -d postgres -c "DROP DATABASE IF EXISTS identity;"
docker exec opencms-identity-db psql -U opencms -d postgres -c "CREATE DATABASE identity;"

echo "✓ Database reset complete"
echo ""
echo "Now restart the Identity Service:"
echo "  cd opencms-identity-service/identity-application"
echo "  mvn spring-boot:run"
