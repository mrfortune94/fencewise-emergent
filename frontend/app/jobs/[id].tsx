import { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { Text, Surface, Button, Chip, Divider } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

interface Job {
  id: string;
  client_name: string;
  address: string;
  contact: string;
  job_type: string;
  notes: string;
  status: string;
  created_by: string;
  created_by_name: string;
  created_at: string;
  completed_at?: string;
  signature_url?: string;
}

export default function JobDetailScreen() {
  const { id } = useLocalSearchParams();
  const { token, user } = useAuth();
  const router = useRouter();
  const [job, setJob] = useState<Job | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    fetchJob();
  }, []);

  const fetchJob = async () => {
    try {
      const response = await axios.get(`${BACKEND_URL}/api/jobs/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setJob(response.data);
    } catch (error) {
      Alert.alert('Error', 'Failed to load job details');
      console.error('Error fetching job:', error);
    } finally {
      setLoading(false);
    }
  };

  const updateJobStatus = async (newStatus: string) => {
    setUpdating(true);
    try {
      await axios.put(
        `${BACKEND_URL}/api/jobs/${id}`,
        { status: newStatus },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setJob({ ...job!, status: newStatus });
      Alert.alert('Success', `Job status updated to ${newStatus}`);
    } catch (error) {
      Alert.alert('Error', 'Failed to update job status');
      console.error('Error updating job:', error);
    } finally {
      setUpdating(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'pending':
        return '#FF9800';
      case 'active':
        return '#2196F3';
      case 'completed':
        return '#4CAF50';
      default:
        return '#666';
    }
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  if (!job) {
    return (
      <View style={styles.container}>
        <Text>Job not found</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#FFFFFF" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Job Details</Text>
      </View>

      <ScrollView style={styles.content}>
        <Surface style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Client Information</Text>
            <Chip
              mode="flat"
              style={[styles.statusChip, { backgroundColor: getStatusColor(job.status) + '20' }]}
              textStyle={{ color: getStatusColor(job.status), fontSize: 12 }}
            >
              {job.status}
            </Chip>
          </View>

          <View style={styles.infoRow}>
            <MaterialCommunityIcons name="account" size={20} color="#1E88E5" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Client Name</Text>
              <Text style={styles.infoValue}>{job.client_name}</Text>
            </View>
          </View>

          <View style={styles.infoRow}>
            <MaterialCommunityIcons name="map-marker" size={20} color="#1E88E5" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Address</Text>
              <Text style={styles.infoValue}>{job.address}</Text>
            </View>
          </View>

          <View style={styles.infoRow}>
            <MaterialCommunityIcons name="phone" size={20} color="#1E88E5" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Contact</Text>
              <Text style={styles.infoValue}>{job.contact}</Text>
            </View>
          </View>

          <View style={styles.infoRow}>
            <MaterialCommunityIcons name="tag" size={20} color="#1E88E5" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Job Type</Text>
              <Text style={styles.infoValue}>{job.job_type}</Text>
            </View>
          </View>

          <View style={styles.infoRow}>
            <MaterialCommunityIcons name="account-circle" size={20} color="#1E88E5" />
            <View style={styles.infoContent}>
              <Text style={styles.infoLabel}>Created By</Text>
              <Text style={styles.infoValue}>{job.created_by_name}</Text>
            </View>
          </View>
        </Surface>

        {job.notes ? (
          <Surface style={styles.section}>
            <Text style={styles.sectionTitle}>Notes</Text>
            <Text style={styles.notesText}>{job.notes}</Text>
          </Surface>
        ) : null}

        {(user?.role === 'admin' || user?.role === 'supervisor' || job.created_by === user?.id) && (
          <Surface style={styles.section}>
            <Text style={styles.sectionTitle}>Actions</Text>
            
            {job.status === 'pending' && (
              <Button
                mode="contained"
                onPress={() => updateJobStatus('active')}
                loading={updating}
                disabled={updating}
                style={styles.actionButton}
                buttonColor="#2196F3"
                icon="play"
              >
                Start Job
              </Button>
            )}

            {job.status === 'active' && (
              <Button
                mode="contained"
                onPress={() => updateJobStatus('completed')}
                loading={updating}
                disabled={updating}
                style={styles.actionButton}
                buttonColor="#4CAF50"
                icon="check"
              >
                Complete Job
              </Button>
            )}

            {job.status === 'completed' && (
              <View style={styles.completedBadge}>
                <MaterialCommunityIcons name="check-circle" size={24} color="#4CAF50" />
                <Text style={styles.completedText}>Job Completed</Text>
              </View>
            )}
          </Surface>
        )}
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
    backgroundColor: '#1E88E5',
    padding: 24,
    paddingTop: 60,
    flexDirection: 'row',
    alignItems: 'center',
  },
  backButton: {
    marginRight: 16,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  content: {
    flex: 1,
    padding: 16,
  },
  section: {
    padding: 16,
    borderRadius: 12,
    marginBottom: 16,
    elevation: 1,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#212121',
  },
  statusChip: {
    height: 28,
  },
  infoRow: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  infoContent: {
    marginLeft: 12,
    flex: 1,
  },
  infoLabel: {
    fontSize: 12,
    color: '#666',
    marginBottom: 2,
  },
  infoValue: {
    fontSize: 16,
    color: '#212121',
  },
  notesText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
    marginTop: 8,
  },
  actionButton: {
    marginTop: 8,
    paddingVertical: 6,
  },
  completedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
    backgroundColor: '#4CAF5010',
    borderRadius: 8,
  },
  completedText: {
    marginLeft: 8,
    fontSize: 16,
    fontWeight: 'bold',
    color: '#4CAF50',
  },
});
