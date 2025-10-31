import { Stack } from 'expo-router';
import { PaperProvider } from 'react-native-paper';
import { AuthProvider } from '../contexts/AuthContext';
import { MD3LightTheme } from 'react-native-paper';

const theme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: '#1E88E5',
    secondary: '#212121',
    background: '#FFFFFF',
    surface: '#F5F5F5',
  },
};

export default function RootLayout() {
  return (
    <PaperProvider theme={theme}>
      <AuthProvider>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="(auth)" />
          <Stack.Screen name="(app)" />
        </Stack>
      </AuthProvider>
    </PaperProvider>
  );
}
