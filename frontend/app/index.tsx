import { useEffect } from 'react';
import { View, StyleSheet, Image } from 'react-native';
import { Text } from 'react-native-paper';
import { useRouter } from 'expo-router';
import { useAuth } from '../contexts/AuthContext';

export default function SplashScreen() {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading) {
      setTimeout(() => {
        if (user) {
          router.replace('/(app)/dashboard');
        } else {
          router.replace('/(auth)/login');
        }
      }, 1500);
    }
  }, [loading, user]);

  return (
    <View style={styles.container}>
      <View style={styles.logoContainer}>
        <Text style={styles.logo}>🏗️</Text>
        <Text style={styles.title}>FenceWise</Text>
        <Text style={styles.tagline}>Setting the Standard for</Text>
        <Text style={styles.tagline}>Pre-Cast Concrete Fencing</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1E88E5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoContainer: {
    alignItems: 'center',
  },
  logo: {
    fontSize: 80,
    marginBottom: 16,
  },
  title: {
    fontSize: 42,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 8,
  },
  tagline: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.9,
    textAlign: 'center',
  },
});
