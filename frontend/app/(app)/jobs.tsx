import { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, TouchableOpacity, Alert } from 'react-native';
import { Text, Surface, Button, FAB, ActivityIndicator, Chip } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuth } from '../../contexts/AuthContext';
import { useRouter } from 'expo-router';
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
  created_by_name: string;
  created_at: string;
}

export default function JobsScreen() {
  const { token, user } = useAuth();
  const router = useRouter();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    try {
      const response = await axios.get(`${BACKEND_URL}/api/jobs`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setJobs(response.data);
    } catch (error) {
      console.error('Error fetching jobs:', error);
    } finally {
      setLoading(false);
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

  const JobCard = ({ job }: { job: Job }) => (
    <TouchableOpacity onPress={() => router.push(`/jobs/${job.id}`)}>
      <Surface style={styles.jobCard}>
        <View style={styles.jobHeader}>
          <View style={styles.jobHeaderLeft}>
            <MaterialCommunityIcons name="folder" size={24} color="#1E88E5" />
            <View style={styles.jobHeaderText}>
              <Text style={styles.jobTitle}>{job.client_name}</Text>
              <Text style={styles.jobSubtitle}>{job.job_type}</Text>
            </View>
          </View>
          <Chip
            mode="flat"
            style={[styles.statusChip, { backgroundColor: getStatusColor(job.status) + '20' }]}
            textStyle={{ color: getStatusColor(job.status), fontSize: 12 }}
          >
            {job.status}
          </Chip>
        </View>
        
        <View style={styles.jobDetails}>
          <View style={styles.jobDetailRow}>
            <MaterialCommunityIcons name="map-marker" size={16} color="#666" />
            <Text style={styles.jobDetailText}>{job.address}</Text>
          </View>
          <View style={styles.jobDetailRow}>
            <MaterialCommunityIcons name="phone" size={16} color="#666" />
            <Text style={styles.jobDetailText}>{job.contact}</Text>
          </View>
          <View style={styles.jobDetailRow}>
            <MaterialCommunityIcons name="account" size={16} color="#666" />
            <Text style={styles.jobDetailText}>{job.created_by_name}</Text>
          </View>
        </View>
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
        <Text style={styles.headerTitle}>Job Folders</Text>
        <Text style={styles.headerSubtitle}>{jobs.length} total jobs</Text>
      </View>

      <ScrollView style={styles.content}>
        {jobs.length === 0 ? (
          <View style={styles.emptyState}>
            <MaterialCommunityIcons name="folder-open" size={64} color="#CCC" />
            <Text style={styles.emptyText}>No jobs yet</Text>
            <Text style={styles.emptySubtext}>Tap the + button to create a new job</Text>
          </View>
        ) : (
          jobs.map((job) => <JobCard key={job.id} job={job} />)
        )}
      </ScrollView>

      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => router.push('/jobs/create')}
      />
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
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.9,
    marginTop: 4,
  },
  content: {
    flex: 1,
    padding: 16,
  },
  jobCard: {
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    elevation: 2,
  },
  jobHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  jobHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  jobHeaderText: {
    marginLeft: 12,
    flex: 1,
  },
  jobTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#212121',
  },
  jobSubtitle: {
    fontSize: 12,
    color: '#666',
    marginTop: 2,
  },
  statusChip: {
    height: 28,
  },
  jobDetails: {
    gap: 8,
  },
  jobDetailRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  jobDetailText: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#999',
    marginTop: 16,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#999',
    marginTop: 8,
    textAlign: 'center',
  },
  fab: {
    position: 'absolute',
    margin: 16,
    right: 0,
    bottom: 80,
    backgroundColor: '#1E88E5',
  },
});
