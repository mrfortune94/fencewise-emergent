import { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Text, Surface, ActivityIndicator } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuth } from '../../contexts/AuthContext';
import { useRouter } from 'expo-router';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

interface DashboardStats {
  total_jobs?: number;
  pending_jobs?: number;
  active_jobs?: number;
  completed_jobs?: number;
  total_users?: number;
  pending_timesheets?: number;
  my_jobs?: number;
  my_pending_jobs?: number;
  my_timesheets?: number;
}

export default function DashboardScreen() {
  const { user, token } = useAuth();
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStats>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await axios.get(`${BACKEND_URL}/api/dashboard/stats`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    } finally {
      setLoading(false);
    }
  };

  const DashboardCard = ({ icon, title, value, onPress, color = '#1E88E5' }: any) => (
    <TouchableOpacity onPress={onPress}>
      <Surface style={styles.card}>
        <View style={[styles.iconContainer, { backgroundColor: color + '20' }]}>
          <MaterialCommunityIcons name={icon} size={32} color={color} />
        </View>
        <Text style={styles.cardValue}>{value || 0}</Text>
        <Text style={styles.cardTitle}>{title}</Text>
      </Surface>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Hello, {user?.name}!</Text>
          <Text style={styles.role}>{user?.role?.toUpperCase()}</Text>
        </View>
        <Text style={styles.logo}>🏗️</Text>
      </View>

      <ScrollView style={styles.content}>
        <Text style={styles.sectionTitle}>Quick Actions</Text>
        
        <View style={styles.cardsGrid}>
          {user?.role === 'admin' ? (
            <>
              <DashboardCard
                icon="folder"
                title="Total Jobs"
                value={stats.total_jobs}
                onPress={() => router.push('/(app)/jobs')}
                color="#1E88E5"
              />
              <DashboardCard
                icon="alert-circle"
                title="Pending Jobs"
                value={stats.pending_jobs}
                onPress={() => router.push('/(app)/jobs')}
                color="#FF9800"
              />
              <DashboardCard
                icon="account-group"
                title="Total Users"
                value={stats.total_users}
                onPress={() => {}}
                color="#4CAF50"
              />
              <DashboardCard
                icon="clock-alert"
                title="Pending Timesheets"
                value={stats.pending_timesheets}
                onPress={() => router.push('/(app)/timesheets')}
                color="#E91E63"
              />
            </>
          ) : (
            <>
              <DashboardCard
                icon="folder"
                title="My Jobs"
                value={stats.my_jobs}
                onPress={() => router.push('/(app)/jobs')}
                color="#1E88E5"
              />
              <DashboardCard
                icon="alert-circle"
                title="Pending Jobs"
                value={stats.my_pending_jobs}
                onPress={() => router.push('/(app)/jobs')}
                color="#FF9800"
              />
              <DashboardCard
                icon="clock"
                title="My Timesheets"
                value={stats.my_timesheets}
                onPress={() => router.push('/(app)/timesheets')}
                color="#4CAF50"
              />
              <DashboardCard
                icon="message"
                title="Messages"
                value="New"
                onPress={() => router.push('/(app)/messages')}
                color="#9C27B0"
              />
            </>
          )}
        </View>

        <Text style={styles.sectionTitle}>Features</Text>
        
        <TouchableOpacity onPress={() => router.push('/(app)/jobs')}>
          <Surface style={styles.featureCard}>
            <MaterialCommunityIcons name="folder" size={24} color="#1E88E5" />
            <View style={styles.featureContent}>
              <Text style={styles.featureTitle}>Job Folders</Text>
              <Text style={styles.featureDescription}>Manage client jobs and projects</Text>
            </View>
            <MaterialCommunityIcons name="chevron-right" size={24} color="#666" />
          </Surface>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => router.push('/(app)/timesheets')}>
          <Surface style={styles.featureCard}>
            <MaterialCommunityIcons name="clock" size={24} color="#1E88E5" />
            <View style={styles.featureContent}>
              <Text style={styles.featureTitle}>Timesheets</Text>
              <Text style={styles.featureDescription}>Track daily work hours</Text>
            </View>
            <MaterialCommunityIcons name="chevron-right" size={24} color="#666" />
          </Surface>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => router.push('/(app)/messages')}>
          <Surface style={styles.featureCard}>
            <MaterialCommunityIcons name="message" size={24} color="#1E88E5" />
            <View style={styles.featureContent}>
              <Text style={styles.featureTitle}>Messaging</Text>
              <Text style={styles.featureDescription}>Team communication</Text>
            </View>
            <MaterialCommunityIcons name="chevron-right" size={24} color="#666" />
          </Surface>
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#1E88E5',
    padding: 24,
    paddingTop: 60,
  },
  greeting: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  role: {
    fontSize: 12,
    color: '#FFFFFF',
    opacity: 0.9,
    marginTop: 4,
  },
  logo: {
    fontSize: 40,
  },
  content: {
    flex: 1,
    padding: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#212121',
    marginTop: 16,
    marginBottom: 12,
  },
  cardsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  card: {
    width: '48%',
    padding: 16,
    borderRadius: 12,
    marginBottom: 16,
    elevation: 2,
  },
  iconContainer: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  cardValue: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#212121',
  },
  cardTitle: {
    fontSize: 14,
    color: '#666',
    marginTop: 4,
  },
  featureCard: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    elevation: 1,
  },
  featureContent: {
    flex: 1,
    marginLeft: 16,
  },
  featureTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#212121',
  },
  featureDescription: {
    fontSize: 12,
    color: '#666',
    marginTop: 2,
  },
});
