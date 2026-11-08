#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"

echo "=== FlowOps Smoke Test ==="

echo "1. Checking Backend Health..."
curl -s -f "http://localhost:8080/actuator/health" | grep "UP" && echo " Backend is UP"

echo "2. Testing Login (admin)..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo "$LOGIN_RES" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
  echo " Login successful, access token acquired."
else
  echo " Login failed: $LOGIN_RES"
  exit 1
fi

echo "3. Testing /api/v1/me..."
curl -s -f -H "Authorization: Bearer $TOKEN" "$BASE_URL/me" | grep "admin" && echo " /me passed"

echo "4. Testing /api/v1/workflows/available..."
curl -s -f -H "Authorization: Bearer $TOKEN" "$BASE_URL/workflows/available" && echo " /workflows/available passed"

echo "=== Smoke Test Complete! All basic flows operating as designed. ==="
