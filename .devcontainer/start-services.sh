#!/bin/bash

ROOT="/workspaces/$(basename $PWD)"

echo "Starting Backend (Spring Boot)..."
cd "$ROOT/scheduler"
./mvnw spring-boot:run > /tmp/backend.log 2>&1 &

echo "Starting Frontend (Angular)..."
cd "$ROOT/scheduler-ui"
npm start > /tmp/frontend.log 2>&1 &

echo "Backend and Frontend started!"
echo "Logs:"
echo "  Backend log:  /tmp/backend.log"
echo "  Frontend log: /tmp/frontend.log"