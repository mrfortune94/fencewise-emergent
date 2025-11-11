# Firebase Setup for APK Production

This project requires a real Firebase configuration to build the APK and enable all features.

## ⚠️ Important
The mock Firebase configuration has been removed. You **must** use a real Firebase project for the app to function properly.

## Prerequisites

1. A Firebase project set up at https://console.firebase.google.com/
2. Your `google-services.json` file downloaded from Firebase Console

## Steps to Enable APK Build in GitHub Actions

### 1. Get your google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **fencewise-57f7b** (or your project)
3. Click on the gear icon ⚙️ → **Project Settings**
4. Scroll down to **Your apps** section
5. Click on your Android app (com.fencewise.app)
6. Click **Download google-services.json**

### 2. Encode the file to Base64

**On Linux/Mac:**
```bash
base64 -w 0 google-services.json > google-services-base64.txt
```

**On Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) | Out-File -Encoding ASCII google-services-base64.txt
```

### 3. Add the Secret to GitHub

1. Go to your repository: https://github.com/mrfortune94/fencewise-emergent
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Set:
   - **Name:** `FIREBASE_GOOGLE_SERVICES_JSON_BASE64`
   - **Value:** Paste the entire contents of `google-services-base64.txt`
5. Click **Add secret**

### 4. Trigger a Build

Once the secret is added, the GitHub Actions workflow will automatically:
- Decode the Firebase configuration
- Create the google-services.json file
- Build the APK with real Firebase connectivity

You can trigger a build by:
- Pushing to `main` or `master` branch
- Going to **Actions** tab → **Build Android APK** → **Run workflow**

### 5. Download the APK

1. Go to the **Actions** tab
2. Click on the successful workflow run
3. Scroll to **Artifacts** section
4. Download **Fencewise-Emergent-apk**
5. Extract and install the APK on your Android device

## Firebase Features Enabled

With real Firebase configuration, the following features will work:

✅ **Firebase Authentication** - User sign-in and password reset
✅ **Cloud Firestore** - Job data, timesheets, and user storage
✅ **Cloud Storage** - Photo and document uploads
✅ **Firebase Analytics** - Usage tracking
✅ **Firebase Cloud Messaging** - Push notifications

## Local Development

For local development, you also need the real `google-services.json`:

1. Download `google-services.json` as described above
2. Place it in: `FenceWiseApp/app/google-services.json`
3. Build the app in Android Studio or via command line:
   ```bash
   cd FenceWiseApp
   ./gradlew assembleDebug
   ```

## Security Notes

⚠️ **Never commit `google-services.json` to the repository**
- It's already in `.gitignore`
- Only store it as an encrypted GitHub Secret
- The base64 encoding is NOT encryption, just a transport format

## Troubleshooting

### Build fails with "FIREBASE_GOOGLE_SERVICES_JSON_BASE64 secret is not set"
- The secret hasn't been added to GitHub repository secrets
- Follow steps above to add it

### Build succeeds but Firebase features don't work
- Verify the package name in google-services.json matches: `com.fencewise.app`
- Check Firebase Console to ensure the Android app is properly registered
- Verify Firebase services are enabled in Firebase Console

### Invalid google-services.json error
- Re-download the file from Firebase Console
- Ensure you're using the correct Firebase project
- Verify the base64 encoding was done correctly

## Need Help?

Refer to:
- [Firebase Android Setup Guide](https://firebase.google.com/docs/android/setup)
- [GitHub Actions Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
