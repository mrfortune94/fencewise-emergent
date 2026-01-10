---
applyTo: ['frontend/**', '!frontend/node_modules/**', '!frontend/.expo/**', '!frontend/.metro-cache/**']
---

# Frontend Instructions (React Native/Expo)

## Overview

This is a React Native application built with Expo, targeting iOS, Android, and web platforms.

## Development Setup

### Starting the App
```bash
cd frontend
yarn install
yarn start
```

### Available Scripts
- `yarn start` - Start Expo development server
- `yarn android` - Run on Android emulator/device
- `yarn ios` - Run on iOS simulator/device
- `yarn web` - Run on web browser
- `yarn lint` - Run ESLint
- `yarn reset-project` - Reset to blank project

## Code Style

### TypeScript
- **Strict mode is enabled** - Always provide proper types
- **No implicit `any`** - Explicitly type all variables and functions
- Use type inference where clear, explicit types where needed
- Prefer interfaces for object shapes, types for unions/intersections

### React Components
- Use **functional components** with hooks
- Place hooks at the top of the component, before any conditional logic
- Use `useState` for local state, Zustand for global state
- Use `useEffect` for side effects, clean up when necessary
- Keep components small and focused on a single responsibility

### File Structure
- Use **Expo Router** file-based routing conventions
- Files in `app/` directory create routes automatically
- `_layout.tsx` files define layout wrappers
- `(group)` folders create route groups without affecting URL
- Index files (`index.tsx`) map to the root of their directory

### Imports
- Use path alias `@/*` for imports from the root directory
- Group imports: React/React Native first, then third-party, then local
- Use named exports for components and utilities

Example:
```typescript
import React, { useState, useEffect } from 'react';
import { View, Text } from 'react-native';
import axios from 'axios';
import { Button } from 'react-native-paper';

import { useAuthStore } from '@/contexts/authStore';
import { MyComponent } from '@/components/MyComponent';
```

## Styling

### Approach
- Use React Native's built-in `StyleSheet.create()` for component styles
- Use React Native Paper components for Material Design
- Leverage Expo components when available
- Keep styles at the bottom of the component file

### Best Practices
- Define styles as constants using `StyleSheet.create()`
- Use flexbox for layouts
- Avoid hardcoded colors - define theme colors if needed
- Use responsive units and dimensions

Example:
```typescript
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
});
```

## State Management

### Zustand (Global State)
- Store authentication state, user preferences, and shared data
- Keep stores in `contexts/` directory
- Use hooks to access and update state

Example:
```typescript
import { create } from 'zustand';

interface AuthState {
  token: string | null;
  setToken: (token: string | null) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  setToken: (token) => set({ token }),
}));
```

### Local State
- Use `useState` for component-local state
- Keep state as close to where it's used as possible

## API Communication

### Using Axios
- Use Axios for HTTP requests
- Include JWT token in Authorization header when authenticated
- Handle errors gracefully with try-catch
- Show loading states during requests

Example:
```typescript
import axios from 'axios';

const fetchData = async () => {
  try {
    const response = await axios.get('/api/endpoint', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching data:', error);
    throw error;
  }
};
```

## Navigation

### Expo Router
- Use file-based routing - files in `app/` create routes
- Use `useRouter()` hook for programmatic navigation
- Use `Link` component for declarative navigation
- Access route parameters with `useLocalSearchParams()`

Example:
```typescript
import { useRouter } from 'expo-router';
import { Link } from 'expo-router';

// Programmatic navigation
const router = useRouter();
router.push('/jobs/123');

// Declarative navigation
<Link href="/jobs/123">View Job</Link>
```

## Assets and Media

### Images
- Use `expo-image` for optimized image loading
- Store static images in `assets/` directory
- Use `expo-image-picker` for selecting photos

### Icons
- Use `@expo/vector-icons` for icons
- Prefer Material Icons or Ionicons for consistency

## Platform-Specific Code

Use `Platform` module for platform-specific logic:
```typescript
import { Platform } from 'react-native';

const styles = StyleSheet.create({
  container: {
    padding: Platform.OS === 'ios' ? 20 : 16,
  },
});
```

## Error Handling

- Always wrap async operations in try-catch blocks
- Provide user-friendly error messages
- Log errors for debugging: `console.error('Context:', error)`
- Handle network errors gracefully

## Performance

- Use `React.memo()` for expensive components
- Avoid inline function definitions in render (use `useCallback`)
- Optimize large lists with `FlatList` or `SectionList`
- Use `useMemo()` for expensive calculations

## Testing

- Follow existing test patterns if tests are present
- Test user interactions and critical paths
- Mock API calls in tests

## Common Patterns

### Async Data Fetching
```typescript
const [data, setData] = useState(null);
const [loading, setLoading] = useState(true);
const [error, setError] = useState(null);

useEffect(() => {
  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/data');
      setData(response.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  fetchData();
}, []);
```

### Form Handling
- Use controlled components with `useState`
- Validate inputs before submission
- Show loading state during submission
- Clear form after successful submission

## Don't

- Don't use `var` - use `const` or `let`
- Don't use class components - use functional components
- Don't mutate state directly - use setState or Zustand actions
- Don't use `any` type - provide proper types
- Don't commit API keys or secrets
- Don't hardcode URLs - use environment variables
- Don't ignore ESLint warnings - fix them
