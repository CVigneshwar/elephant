Running Scheduler Using DevContainer (VS Code Only)

Follow these steps to run both backend & frontend inside the DevContainer environment.

✅ 1. Prerequisites

Make sure you have installed:

Docker Desktop

Visual Studio Code

Dev Containers extension
(ms-vscode-remote.remote-containers)

✅ 2. Open the Project in DevContainer

Open VS Code

Click File → Open Folder

Select the Elephant project root folder

VS Code will show this prompt:

👉 “Reopen in Container”

Click Yes.

VS Code will now:

Build the DevContainer

Install Java, Node, Angular CLI

Load required extensions

Mount your project into the container

⚠ Important: Backend & Frontend do NOT auto-start

Because Angular CLI asks interactive questions and Java takes time to initialize inside DevContainer, auto-start is intentionally disabled.

👉 You must manually start both apps in terminal tabs.

This is the recommended, stable method for interview submissions.

✅ 3. Start Backend (Spring Boot)

Inside DevContainer terminal:

cd scheduler-api
./mvnw spring-boot:run


Backend will start on:

👉 http://localhost:8080

✅ 4. Start Frontend (Angular)

Open a new terminal tab in DevContainer:

cd scheduler-ui
npm install   # first time only
npm run start:docker


Frontend will start on:

👉 http://localhost:4200

API calls (via proxy) will automatically communicate with the backend.

✅ 5. Confirm Both Services Are Running

Check:

VSCode → Ports tab → You should see ports 4200 and 8080

Browser:

http://localhost:4200
 → UI

http://localhost:8080/api/schedule
 → API

Both should work inside DevContainer.

📦 Summary of Commands
Backend
cd scheduler-api
./mvnw spring-boot:run

Frontend
cd scheduler-ui
npm install
npm run start:docker

✅ 6. Refer TESTING DOCS folder

It contains a screen-recording of the application
Queries which you can execute and verify working of Challenge 1 & 2