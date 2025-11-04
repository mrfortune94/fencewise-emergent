# FenceWise Android App

FenceWise is a native Android application built with Kotlin and Jetpack Compose for managing fencing projects, timesheets, and team communication.

## 🏗️ Tech Stack

- **Language:** Kotlin 2.0
- **UI Framework:** Jetpack Compose with Material 3
- **Build Tool:** Gradle 8.7
- **Target SDK:** Android 14 (API 35)
- **Min SDK:** Android 7.0 (API 24)
- **Backend:** Firebase (Auth, Firestore, Storage, Analytics, Messaging)

## 🎨 Branding

- **App Name:** FenceWise
- **Package:** com.fencewise.app
- **Colors:**
  - Primary Blue: #1E88E5
  - Charcoal: #212121
  - White: #FFFFFF
- **Theme:** Material 3 Day/Night with NoActionBar

## ✨ Features

### Current Features
- **Authentication:** Email/password sign-in with password reset functionality
- **Dashboard:** Quick access cards for all major features
- **Jobs:** Placeholder for job management (ready for Firestore integration)
- **Timesheets:** Time tracking interface with start/finish/break controls
- **Chat:** Placeholder for team messaging (ready for Firebase integration)
- **Bottom Navigation:** Easy navigation between main sections

### Firebase Integration
The app is configured with Firebase services:
- **Firebase Auth:** User authentication
- **Cloud Firestore:** Job and user data storage
- **Cloud Storage:** Photo and document uploads
- **Firebase Analytics:** Usage tracking
- **Firebase Cloud Messaging:** Push notifications (prepared)

## 🚀 Setup Instructions

### Prerequisites

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Version: Android Studio Ladybug or later recommended

2. **Install JDK 17**
   - Use Temurin JDK 17: https://adoptium.net/temurin/releases/
   - Set `JAVA_HOME` environment variable
   - Verify: `java -version` (should show version 17)

3. **Android SDK Setup**
   - Android Studio will prompt to install SDK
   - Ensure Android SDK 35 (Android 14) is installed
   - Accept SDK licenses: `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses`

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/mrfortune94/fencewise-emergent.git
   cd fencewise-emergent/FenceWiseApp
   ```

2. **Firebase Configuration (Optional for Development)**
   
   The app can build and run without Firebase configuration using a mock setup. For full Firebase functionality:
   
   a. Download `google-services.json` from Firebase Console:
      - Go to: https://console.firebase.google.com/
      - Select your project (fencewise-57f7b)
      - Go to Project Settings > Your apps
      - Download `google-services.json`
   
   b. Place the file:
      ```bash
      cp ~/Downloads/google-services.json FenceWiseApp/app/google-services.json
      ```
   
   **Note:** A mock `google-services.json` is automatically used if the real file is missing during Debug builds.

3. **Build the app**
   
   Using Android Studio:
   - Open the `FenceWiseApp` folder in Android Studio
   - Wait for Gradle sync to complete
   - Click "Run" or press Shift+F10
   
   Using command line:
   ```bash
   cd FenceWiseApp
   ./gradlew assembleDebug
   ```
   
   The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

## 🔐 GitHub Actions CI/CD Setup

### Setting up the Secret

To enable automatic APK builds via GitHub Actions with real Firebase configuration:

1. **Encode your `google-services.json` to Base64:**
   
   On Linux/Mac:
   ```bash
   base64 -w 0 google-services.json > google-services-base64.txt
   ```
   
   On Windows (PowerShell):
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) > google-services-base64.txt
   ```

2. **Add the secret to GitHub:**
   - Go to: https://github.com/mrfortune94/fencewise-emergent/settings/secrets/actions
   - Click "New repository secret"
   - Name: `FIREBASE_GOOGLE_SERVICES_JSON_BASE64`
   - Value: Paste the contents of `google-services-base64.txt`
   - Click "Add secret"

3. **Trigger a build:**
   - Push to `main` branch, or
   - Go to Actions tab → "Android APK Build" → "Run workflow"

### Downloading the APK Artifact

1. Go to: https://github.com/mrfortune94/fencewise-emergent/actions
2. Click on the latest successful workflow run
3. Scroll to "Artifacts" section
4. Download `FenceWise_v1.0_debug`
5. Extract and install the APK on your Android device

**Note:** If the `FIREBASE_GOOGLE_SERVICES_JSON_BASE64` secret is not set, the build will use the mock Firebase configuration automatically.

## 📱 App Structure

```
FenceWiseApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/fencewise/app/
│   │   │   ├── MainActivity.kt           # Main entry point
│   │   │   ├── navigation/
│   │   │   │   └── Screen.kt            # Navigation routes
│   │   │   ├── screens/
│   │   │   │   ├── AuthScreen.kt        # Login/password reset
│   │   │   │   ├── DashboardScreen.kt   # Main dashboard
│   │   │   │   ├── JobsScreen.kt        # Job management
│   │   │   │   ├── TimesheetsScreen.kt  # Time tracking
│   │   │   │   └── ChatScreen.kt        # Team chat
│   │   │   └── ui/theme/
│   │   │       ├── Color.kt             # Brand colors
│   │   │       ├── Theme.kt             # Material 3 theme
│   │   │       └── Type.kt              # Typography
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml          # App strings
│   │   │   │   ├── colors.xml           # Color resources
│   │   │   │   └── themes.xml           # Android themes
│   │   │   └── mipmap-*/                # App icons
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                  # App-level Gradle config
│   ├── google-services.mock.json         # Mock Firebase config
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/                          # Gradle wrapper files
├── build.gradle.kts                      # Project-level Gradle config
├── settings.gradle.kts                   # Project settings
├── gradle.properties                     # Gradle properties
└── gradlew / gradlew.bat                 # Gradle wrapper scripts
```

## 🔥 Firebase Data Structure

### Firestore Collections

**jobs/{jobId}**
```json
{
  "client": "String",
  "address": "String",
  "contact": "String",
  "jobType": "String",
  "panelPlan": {
    "height": "2.2m",
    "width": "6m",
    "thickness": "110mm"
  },
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

**timesheets/{timesheetId}**
```json
{
  "userId": "String",
  "jobId": "String",
  "startTime": "Timestamp",
  "endTime": "Timestamp",
  "breakDuration": "Number (minutes)",
  "notes": "String",
  "date": "Timestamp"
}
```

**users/{userId}**
```json
{
  "email": "String",
  "name": "String",
  "role": "Admin | Supervisor | Worker",
  "createdAt": "Timestamp"
}
```

**messages/{messageId}**
```json
{
  "userId": "String",
  "text": "String",
  "timestamp": "Timestamp",
  "jobId": "String (optional)"
}
```

### Security Rules

Role-based access control is implemented:
- **Admin:** Full access to all features
- **Supervisor:** Can manage jobs, view all timesheets
- **Worker:** Can log time, view assigned jobs, participate in chat

See `firebase/firestore.rules` and `firebase/storage.rules` for complete security rules.

## 🧪 Testing

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📦 Building Release APK

1. Generate signing key:
   ```bash
   keytool -genkey -v -keystore fencewise-release.keystore -alias fencewise -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Configure `app/build.gradle.kts` with signing config

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

### Troubleshooting

### Gradle Build Fails
- Ensure JDK 17 is installed and `JAVA_HOME` is set correctly
- Run: `./gradlew clean` then rebuild
- Delete `.gradle` folder and sync again

**Note:** If building in an environment with restricted network access (e.g., some CI systems or corporate networks), you may encounter issues accessing `dl.google.com` or `maven.google.com`. The GitHub Actions workflow should work correctly as GitHub's infrastructure has proper connectivity to these repositories.

### Firebase Not Working
- Verify `google-services.json` is in `app/` directory
- Check package name matches: `com.fencewise.app`
- Ensure Firebase project is active in Firebase Console

### App Crashes on Launch
- Check logcat in Android Studio
- Verify minimum SDK requirements (API 24+)
- Ensure all dependencies are downloaded

## 📄 License

Copyright © 2024 FenceWise. All rights reserved.

## 🤝 Contributing

For questions or issues, please contact the development team.

---

**Version:** 1.0  
**Last Updated:** November 2024
