---
applyTo: ['FenceWiseApp/**', '!FenceWiseApp/build/**', '!FenceWiseApp/.gradle/**', '!FenceWiseApp/app/build/**']
---

# Android Native App Instructions (Kotlin/Jetpack Compose)

## Overview

This is a native Android application built with Kotlin and Jetpack Compose, using Firebase for backend services.

## Development Setup

### Prerequisites
- Android Studio (Ladybug or later)
- JDK 17 (Temurin recommended)
- Android SDK 35 (Android 14)

### Building the App
```bash
cd FenceWiseApp
./gradlew assembleDebug       # Build debug APK
./gradlew installDebug        # Install on device/emulator
./gradlew test                # Run unit tests
./gradlew connectedAndroidTest  # Run instrumentation tests
```

### Firebase Configuration
- Valid `google-services.json` file required
- Place in `FenceWiseApp/app/google-services.json`
- Package name must match: `com.fencewise.app`

## Code Style

### Kotlin Conventions
- Follow official Kotlin coding conventions
- Use camelCase for function and variable names
- Use PascalCase for class names
- Use descriptive names that convey intent
- Prefer `val` over `var` for immutability
- Use data classes for simple data holders
- Use sealed classes for state representations

### Code Formatting
- Use 4 spaces for indentation
- Keep lines under 120 characters when possible
- Use trailing commas in multi-line declarations
- Group imports: Android/Kotlin first, then third-party, then project

Example:
```kotlin
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth

import com.fencewise.app.navigation.Screen
```

## Jetpack Compose

### Composable Functions
- Use `@Composable` annotation for all composable functions
- Name composables with PascalCase (like classes)
- Keep composables small and focused
- Extract reusable UI into separate composables
- Use preview annotations for development

Example:
```kotlin
@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = user.name)
    }
}

@Preview
@Composable
fun UserCardPreview() {
    UserCard(
        user = User("John Doe"),
        onClick = {}
    )
}
```

### State Management
- Use `remember` for composable-local state
- Use `rememberSaveable` for state that survives configuration changes
- Hoist state when needed by parent composables
- Use ViewModel for complex state and business logic

Example:
```kotlin
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

### Modifiers
- Always provide a `modifier` parameter with default value `Modifier`
- Apply modifier as the first parameter to the root composable
- Chain modifiers in logical order: size, then padding, then behavior

Example:
```kotlin
@Composable
fun CustomComponent(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { /* action */ }
    )
}
```

## Material 3 Design

### Theme
- Use Material 3 theming system
- Colors defined in `ui/theme/Color.kt`:
  - Primary: #1E88E5 (FenceWise Blue)
  - Charcoal: #212121
  - Surface: #FFFFFF
- Typography defined in `ui/theme/Type.kt`
- Theme defined in `ui/theme/Theme.kt`

### Components
- Use Material 3 components from `androidx.compose.material3`
- Prefer built-in components over custom implementations
- Follow Material Design guidelines for spacing, elevation, and typography
- Use dynamic colors when appropriate

Example:
```kotlin
import androidx.compose.material3.*

@Composable
fun MaterialExample() {
    Button(
        onClick = { /* action */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text("Click Me")
    }
}
```

## Navigation

### Navigation Component
- Define routes in `navigation/Screen.kt` as sealed class
- Use NavController for navigation
- Pass required data through navigation arguments

Example:
```kotlin
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Jobs : Screen("jobs")
    data class JobDetail(val jobId: String) : Screen("jobs/$jobId")
}

// Usage
navController.navigate(Screen.Jobs.route)
```

## Firebase Integration

### Firebase Authentication
- Use `FirebaseAuth` for user authentication
- Handle authentication state changes
- Implement sign-in, sign-out, and password reset

Example:
```kotlin
val auth = FirebaseAuth.getInstance()

// Sign in
auth.signInWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        // Handle success
    }
    .addOnFailureListener { exception ->
        // Handle error
    }
```

### Cloud Firestore
- Use `FirebaseFirestore` for database operations
- Collections: jobs, timesheets, users, messages
- Implement proper error handling
- Use data classes for Firestore models

Example:
```kotlin
data class Job(
    val id: String = "",
    val client: String = "",
    val address: String = "",
    val contact: String = "",
    val jobType: String = ""
)

val db = FirebaseFirestore.getInstance()

// Read
db.collection("jobs")
    .get()
    .addOnSuccessListener { documents ->
        val jobs = documents.map { it.toObject<Job>() }
    }
    .addOnFailureListener { exception ->
        // Handle error
    }

// Write
db.collection("jobs")
    .add(job)
    .addOnSuccessListener { documentReference ->
        // Handle success
    }
```

### Cloud Storage
- Use `FirebaseStorage` for file uploads
- Store photos and documents
- Implement progress tracking for uploads

## Architecture

### Project Structure
```
app/src/main/java/com/fencewise/app/
├── MainActivity.kt              # Entry point
├── navigation/
│   └── Screen.kt               # Navigation routes
├── screens/
│   ├── AuthScreen.kt           # Authentication
│   ├── DashboardScreen.kt      # Main dashboard
│   ├── JobsScreen.kt           # Job management
│   ├── TimesheetsScreen.kt     # Time tracking
│   └── ChatScreen.kt           # Team messaging
└── ui/theme/
    ├── Color.kt                # Brand colors
    ├── Theme.kt                # Material 3 theme
    └── Type.kt                 # Typography
```

### Screen Composables
- Each screen is a top-level composable
- Handle UI state and user interactions
- Delegate business logic to ViewModels when complex
- Show loading states and errors appropriately

## Security

### Role-Based Access Control
- Three roles: Admin, Supervisor, Worker
- Admin: Full access to all features
- Supervisor: Manage jobs, view all timesheets
- Worker: Log time, view assigned jobs, chat

### Firestore Security Rules
- Implement in `firebase/firestore.rules`
- Validate user authentication
- Enforce role-based permissions

### Storage Security Rules
- Implement in `firebase/storage.rules`
- Restrict file access based on authentication
- Validate file types and sizes

## Error Handling

- Always handle Firebase operation failures
- Show user-friendly error messages
- Log errors for debugging
- Provide fallback UI for error states

Example:
```kotlin
db.collection("jobs")
    .get()
    .addOnSuccessListener { documents ->
        // Handle success
    }
    .addOnFailureListener { exception ->
        Log.e("Firestore", "Error getting jobs", exception)
        // Show error UI
    }
```

## Resources

### Strings
- Define all user-facing text in `res/values/strings.xml`
- Use string resources in composables: `stringResource(R.string.app_name)`
- Support internationalization

### Colors
- Define colors in `res/values/colors.xml`
- Use theme colors in composables

### Icons
- Use Material Icons when possible
- Custom icons in `res/drawable`

## Testing

### Unit Tests
- Test business logic and data models
- Use JUnit for unit tests
- Mock Firebase dependencies

### Instrumentation Tests
- Test UI with Compose testing framework
- Use `composeTestRule` for UI assertions
- Test user interactions

Example:
```kotlin
@Test
fun loginButton_isDisplayed() {
    composeTestRule.setContent {
        AuthScreen()
    }
    composeTestRule
        .onNodeWithText("Sign In")
        .assertIsDisplayed()
}
```

## Gradle

### Dependencies
- Keep dependencies up to date
- Use version catalogs when possible
- Minimize dependency conflicts

### Build Configuration
- Target SDK: 35 (Android 14)
- Min SDK: 24 (Android 7.0)
- Kotlin version: 2.0
- Compose compiler version aligned with Kotlin

## CI/CD

### GitHub Actions
- Workflow: `.github/workflows/android.yml`
- Requires `FIREBASE_GOOGLE_SERVICES_JSON_BASE64` secret
- Builds debug and release APKs
- Uploads APK artifacts

## Common Patterns

### Loading State
```kotlin
@Composable
fun DataScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<List<Item>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        // Fetch data
        isLoading = false
    }
    
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        DataList(data)
    }
}
```

### Form Input
```kotlin
@Composable
fun LoginForm() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = { /* submit */ }) {
            Text("Sign In")
        }
    }
}
```

## Don't

- Don't use XML layouts - use Jetpack Compose
- Don't ignore Firebase security rules
- Don't hardcode strings - use string resources
- Don't commit `google-services.json` with sensitive data
- Don't use deprecated APIs - use modern alternatives
- Don't skip error handling for Firebase operations
- Don't use blocking operations on main thread
- Don't ignore Android lifecycle - use proper lifecycle-aware components
