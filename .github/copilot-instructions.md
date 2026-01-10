# FenceWise Emergent - Copilot Instructions

## Project Overview

FenceWise Emergent is a fencing project management application with multiple components:
- **React Native/Expo Frontend**: Mobile app for iOS, Android, and web
- **FastAPI Python Backend**: RESTful API server with MongoDB
- **Android Native App**: Kotlin-based Android app with Jetpack Compose and Firebase

## Tech Stack

### Frontend (React Native/Expo)
- **Framework**: React Native 0.79.5 with Expo SDK 54
- **Routing**: Expo Router (file-based routing)
- **Language**: TypeScript with strict mode enabled
- **UI Libraries**: React Native Paper, Expo components
- **State Management**: Zustand
- **Networking**: Axios
- **Package Manager**: Yarn (specified in package.json)

### Backend (Python/FastAPI)
- **Framework**: FastAPI 0.110.1
- **Database**: MongoDB with Motor (async driver)
- **Authentication**: JWT tokens with bcrypt password hashing
- **Language**: Python 3.x
- **API Prefix**: All routes use `/api` prefix

### Android Native App (Kotlin)
- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material 3
- **Build Tool**: Gradle 8.7
- **Backend**: Firebase (Auth, Firestore, Storage, Analytics, Messaging)
- **Target SDK**: Android 14 (API 35)
- **Min SDK**: Android 7.0 (API 24)

## Project Structure

```
fencewise-emergent/
├── frontend/           # React Native/Expo mobile app
├── backend/            # FastAPI Python backend
├── FenceWiseApp/       # Android native app (Kotlin)
├── firebase/           # Firebase configuration and rules
└── tests/              # Shared test utilities
```

## Build and Test Commands

### Frontend
```bash
cd frontend
yarn install          # Install dependencies
yarn start            # Start Expo development server
yarn android          # Run on Android
yarn ios              # Run on iOS
yarn web              # Run on web
yarn lint             # Run ESLint
```

### Backend
```bash
cd backend
pip install -r requirements.txt  # Install dependencies
uvicorn server:app --reload      # Start development server
pytest                            # Run tests
black .                           # Format code
isort .                           # Sort imports
flake8                            # Lint code
mypy .                            # Type check
```

### Android Native App
```bash
cd FenceWiseApp
./gradlew assembleDebug    # Build debug APK
./gradlew test             # Run unit tests
./gradlew connectedAndroidTest  # Run instrumentation tests
```

## Code Conventions

### General
- Use descriptive variable and function names
- Write comments for complex logic or non-obvious code
- Follow existing patterns in the codebase
- Keep functions small and focused on a single responsibility

### TypeScript/JavaScript (Frontend)
- Use TypeScript strict mode - no implicit `any` types
- Prefer functional components with hooks over class components
- Use arrow functions for callbacks and inline functions
- Follow React hooks rules (hooks at top level, no conditional hooks)
- Use `const` by default, `let` only when reassignment is needed
- Path alias `@/*` maps to frontend root directory
- Follow Expo and React Native best practices
- Use Expo Router's file-based routing conventions

### Python (Backend)
- Follow PEP 8 style guide
- Use type hints for function parameters and return values
- Use async/await for database operations
- Use Pydantic models for request/response validation
- Format code with Black (line length 88)
- Sort imports with isort
- Use descriptive exception messages
- Never commit secrets - use environment variables

### Kotlin (Android Native)
- Follow Kotlin coding conventions
- Use Jetpack Compose for all UI components
- Follow Material 3 design guidelines
- Use descriptive function and variable names in camelCase
- Keep composables small and reusable
- Use Firebase SDK best practices

## Authentication & Security

### Backend
- Use JWT tokens with HS256 algorithm for authentication
- Hash passwords with bcrypt before storing
- Store secrets in environment variables, never in code
- Validate all user inputs with Pydantic models
- Use HTTPBearer for token authentication

### Frontend
- Store authentication tokens securely using AsyncStorage
- Include JWT token in Authorization header for API requests
- Handle token expiration gracefully

### Android Native
- Use Firebase Authentication for user management
- Implement proper security rules in Firestore and Storage
- Follow role-based access control (Admin, Supervisor, Worker)

## Database

### MongoDB (Backend)
- Database name from `DB_NAME` environment variable
- Connection URL from `MONGO_URL` environment variable
- Use Motor for async operations
- Use Pydantic models with custom `PyObjectId` for MongoDB ObjectIds

### Firebase (Android Native)
- Firestore collections: jobs, timesheets, users, messages
- Storage for photos and documents
- Security rules defined in firebase/ directory

## Environment Variables

### Backend (.env file in backend/)
- `MONGO_URL`: MongoDB connection string
- `DB_NAME`: Database name
- `SECRET_KEY`: JWT secret key (change in production)

### Frontend
- Use `react-native-dotenv` for environment variables
- API endpoint configuration

### Android Native
- Firebase configuration in `google-services.json`
- Required secret: `FIREBASE_GOOGLE_SERVICES_JSON_BASE64` (for CI/CD)

## Firebase Configuration

The Android app requires a valid `google-services.json` file:
- Package name must match: `com.fencewise.app`
- Place in `FenceWiseApp/app/google-services.json`
- For CI/CD, encode as base64 and store in GitHub secrets

## Testing

- Frontend: Write tests using existing test infrastructure if available
- Backend: Use pytest for unit tests
- Android: Use JUnit for unit tests, Espresso for UI tests
- Focus on testing business logic and critical paths

## CI/CD

- GitHub Actions workflow for Android builds (`.github/workflows/android.yml`)
- Requires `FIREBASE_GOOGLE_SERVICES_JSON_BASE64` secret for successful builds
- Builds triggered on push to main/master branches

## Important Notes

- Always run linters before committing code
- Test changes locally before pushing
- Keep dependencies up to date but verify compatibility
- Document breaking changes in commit messages
- Use meaningful commit messages following conventional commits pattern
- Never commit sensitive data like API keys, passwords, or Firebase configurations
