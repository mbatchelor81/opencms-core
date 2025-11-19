#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8081/api/identity/users"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Identity Service API Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. Create User
echo -e "${YELLOW}[1/5] Creating new user...${NC}"
CREATE_RESPONSE=$(curl -s -X POST ${BASE_URL} \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser_'$(date +%s)'",
    "email": "testuser_'$(date +%s)'@example.com",
    "firstName": "Test",
    "lastName": "User",
    "password": "SecurePass123!"
  }')

echo -e "${GREEN}Response:${NC}"
echo "$CREATE_RESPONSE" | jq '.'

# Extract user ID
USER_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')

if [ "$USER_ID" == "null" ] || [ -z "$USER_ID" ]; then
    echo -e "${RED}❌ Failed to create user${NC}"
    exit 1
fi

echo -e "${GREEN}✓ User created with ID: ${USER_ID}${NC}\n"
sleep 1

# 2. Get User by ID
echo -e "${YELLOW}[2/5] Getting user by ID...${NC}"
GET_RESPONSE=$(curl -s -X GET ${BASE_URL}/${USER_ID})

echo -e "${GREEN}Response:${NC}"
echo "$GET_RESPONSE" | jq '.'

if echo "$GET_RESPONSE" | jq -e '.id' > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Successfully retrieved user${NC}\n"
else
    echo -e "${RED}❌ Failed to retrieve user${NC}\n"
fi
sleep 1

# 3. Update User
echo -e "${YELLOW}[3/5] Updating user...${NC}"
UPDATE_RESPONSE=$(curl -s -X PUT ${BASE_URL}/${USER_ID} \
  -H "Content-Type: application/json" \
  -d '{
    "email": "updated.email@example.com",
    "firstName": "Updated",
    "lastName": "TestUser",
    "enabled": true
  }')

echo -e "${GREEN}Response:${NC}"
echo "$UPDATE_RESPONSE" | jq '.'

if echo "$UPDATE_RESPONSE" | jq -e '.email' | grep -q "updated.email@example.com"; then
    echo -e "${GREEN}✓ User successfully updated${NC}\n"
else
    echo -e "${RED}❌ Failed to update user${NC}\n"
fi
sleep 1

# 4. Verify Update
echo -e "${YELLOW}[4/5] Verifying update...${NC}"
VERIFY_RESPONSE=$(curl -s -X GET ${BASE_URL}/${USER_ID})

echo -e "${GREEN}Response:${NC}"
echo "$VERIFY_RESPONSE" | jq '.'

if echo "$VERIFY_RESPONSE" | jq -e '.email' | grep -q "updated.email@example.com"; then
    echo -e "${GREEN}✓ Update verified${NC}\n"
else
    echo -e "${RED}❌ Update verification failed${NC}\n"
fi
sleep 1

# 5. Delete User
echo -e "${YELLOW}[5/5] Deleting user...${NC}"
DELETE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X DELETE ${BASE_URL}/${USER_ID})

HTTP_STATUS=$(echo "$DELETE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)

if [ "$HTTP_STATUS" == "204" ]; then
    echo -e "${GREEN}✓ User successfully deleted (HTTP 204)${NC}\n"
else
    echo -e "${RED}❌ Failed to delete user (HTTP ${HTTP_STATUS})${NC}\n"
fi
sleep 1

# 6. Verify Deletion
echo -e "${YELLOW}[Bonus] Verifying deletion...${NC}"
VERIFY_DELETE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET ${BASE_URL}/${USER_ID})

HTTP_STATUS=$(echo "$VERIFY_DELETE" | grep "HTTP_STATUS" | cut -d: -f2)

if [ "$HTTP_STATUS" == "404" ]; then
    echo -e "${GREEN}✓ Deletion verified - user not found (HTTP 404)${NC}\n"
else
    echo -e "${RED}❌ User still exists (HTTP ${HTTP_STATUS})${NC}\n"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Test Suite Complete${NC}"
echo -e "${BLUE}========================================${NC}"
