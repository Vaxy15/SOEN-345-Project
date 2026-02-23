# SOEN 345 Ticketing App (Firebase + React + Spring Boot)

This repo contains a simple ticket reservation web application built with:

- **Frontend:** React (Vite) in `/frontend`, deployed to **Firebase Hosting**
- **Backend:** Spring Boot (Java) in `/backend`, runs locally on port **8080**
- **Auth:** **Firebase Authentication** (Email/Password)
- **Database:** **Firestore** (Firebase)
- **Token verification:** Backend uses **Firebase Admin SDK** to verify Firebase ID tokens

---

## Repository structure
# SOEN 345 Ticketing App (Firebase + React + Spring Boot)

This repo contains a simple ticket reservation web application built with:

- **Frontend:** React (Vite) in `/frontend`, deployed to **Firebase Hosting**
- **Backend:** Spring Boot (Java) in `/backend`, runs locally on port **8080**
- **Auth:** **Firebase Authentication** (Email/Password)
- **Database:** **Firestore** (Firebase)
- **Token verification:** Backend uses **Firebase Admin SDK** to verify Firebase ID tokens

---

## Repository structure
---

## Prerequisites (developer machine)

- **Git**
- **Node.js** (recommended: 18 or 20 LTS; Node 24 worked for us but LTS is safest)
- **Java 17** (Temurin recommended)
- **Firebase CLI**
- **Google Cloud CLI (gcloud)** (optional until we deploy backend to Cloud Run)

Verify installs:
```bash
node -v
npm -v
java -version
firebase --version
gcloud --version
```
 
### Firebase project info
- Firebase project id: soen345-ticketing
- Hosting URL: https://soen345-ticketing.web.app

### Firebase console
- Use the Firebase Console to manage:
- Authentication providers
- Firestore database and rules
- Hosting deployments 
 
 ---

## Local setup (first time)
1. Clone repo
```bash
git clone <your-repo-url>
cd SOEN-345-Project
```

2. Frontend environment variables  
Create frontend/.env (this file is ignored by git):
```bash
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=soen345-ticketing.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=soen345-ticketing
VITE_FIREBASE_APP_ID=...
VITE_BACKEND_URL=http://localhost:8080
```  
Where to get Firebase web config:  
Firebase Console → Project settings → “Your apps” → Web app config.

3. Backend service account key (local dev only)  
Download a Firebase Admin SDK service account key:  
Firebase Console → Project settings → Service accounts → Generate new private key  
Save it as:  
```bash
backend/serviceAccountKey.json
```  
This file is ignored by git. Never commit it.  

4. Install frontend dependencies  
```bash
cd backend
# Windows PowerShell:
$env:GOOGLE_APPLICATION_CREDENTIALS = "$(pwd)\serviceAccountKey.json"
.\mvnw.cmd spring-boot:run
```  
Backend should start on: **http://localhost:8080**  
Health check: **Health check:**  

6. Run frontend (React)
```bash
cd frontend
npm run dev
```  
Frontend runs at: **http://localhost:5173**  

## How auth works (important)
1. User signs in on the frontend using Firebase Auth.
2. Frontend gets a Firebase ID token (user.getIdToken()).
3. Frontend calls the backend with:
   * Authorization: Bearer \<token>
4. Backend verifies the token using Firebase Admin SDK.
5. Example endpoint: **GET /api/me returns the authenticated user’s UID.**

### Test flow (already implemented)
- Register/Login in the frontend
- Click “Call /api/me”
- Expect: Status 200: \<uid\>

## Deployments
### Deploy frontend to Firebase Hosting
From repo root:
```bash
firebase deploy
```
Note: Deploying Firestore rules/indexes updates project settings. Coordinate with the team before changing rules.

## Development workflow (suggested)
Create a feature branch:  
```bash
git checkout -b feature/<name>
```
Make changes, run locally, then:  
```bash
git add .
git commit -m "Describe change"
git push -u origin feature/<name>
```
Open a PR to main.

## Security notes
Do not commit:  
- backend/serviceAccountKey.json
- frontend/.env
- Firebase web API keys are not treated as secrets, but still avoid posting them unnecessarily.