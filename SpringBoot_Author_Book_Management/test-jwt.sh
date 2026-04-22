#!/usr/bin/env bash

echo "=== Testing JWT and Security Functionality ==="
echo ""

# Test 1: Register a user
echo "1. Testing user registration:"
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "role": "USER"
  }'
echo ""
echo ""

# Test 2: Login with the user
echo "2. Testing user login:"
TOKEN=$(curl -X POST "http://localhost:8081/auth/login?username=testuser&password=password123" -s)
echo "Token: $TOKEN"
echo ""

# Test 3: Try to access protected endpoint without token (should fail)
echo "3. Testing access without JWT token (should fail):"
curl -X GET http://localhost:8081/authors -s
echo ""
echo ""

# Test 4: Try to access protected endpoint with token (should work if user has permissions)
echo "4. Testing access with JWT token:"
curl -X GET http://localhost:8081/authors \
  -H "Authorization: Bearer $TOKEN" -s
echo ""
echo ""

# Test 5: Test admin login
echo "5. Testing admin login:"
ADMIN_TOKEN=$(curl -X POST "http://localhost:8081/auth/login?username=admin&password=admin123" -s)
echo "Admin Token: $ADMIN_TOKEN"
echo ""

echo "=== Tests completed ==="
