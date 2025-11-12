# Running the project with Docker

# Dev Container (Recommended for reviewers)
Open VS Code →
Press F1 →
"Dev Containers: Reopen in Container"

This automatically prepares Java 17, Node, Angular CLI and VS Code extensions.

## Option 1 — Run everything using Docker Compose
docker-compose up --build

Backend → http://localhost:8080  
Frontend → http://localhost:4200

## Option 2 — Run only backend
cd backend
docker build -t scheduler-backend .
docker run -p 8080:8080 scheduler-backend

## Option 3 — Run only frontend
cd frontend
docker build -t scheduler-frontend .
docker run -p 4200:80 scheduler-frontend

