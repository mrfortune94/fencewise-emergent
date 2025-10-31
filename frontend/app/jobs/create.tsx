import { useState } from 'react';
import {
  View,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  TouchableOpacity,
  Alert,
} from 'react-native';
import { Text, TextInput, Button, Surface, SegmentedButtons } from 'react-native-paper';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import axios from 'axios';

const BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

const JOB_TYPES = [
  { value: 'Standard', label: 'Standard' },
  { value: 'Channel', label: 'Channel' },
  { value: 'Corner', label: 'Corner' },
  { value: 'Raked', label: 'Raked' },
];

export default function CreateJobScreen() {
  const { token } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    client_name: '',
    address: '',
    contact: '',
    job_type: 'Standard',
    notes: '',
  });

  const handleCreate = async () => {
    if (!formData.client_name || !formData.address || !formData.contact) {
      Alert.alert('Error', 'Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await axios.post(`${BACKEND_URL}/api/jobs`, formData, {
        headers: { Authorization: `Bearer ${token}` },
      });
      
      Alert.alert('Success', 'Job created successfully', [
        { text: 'OK', onPress: () => router.back() },
      ]);
    } catch (error) {
      Alert.alert('Error', 'Failed to create job');
      console.error('Error creating job:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#FFFFFF" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Create New Job</Text>
      </View>

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.formContainer}
      >
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <Surface style={styles.form}>
            <TextInput
              label="Client Name *"
              value={formData.client_name}
              onChangeText={(text) => setFormData({ ...formData, client_name: text })}
              mode="outlined"
              style={styles.input}
            />

            <TextInput
              label="Address *"
              value={formData.address}
              onChangeText={(text) => setFormData({ ...formData, address: text })}
              mode="outlined"
              multiline
              numberOfLines={2}
              style={styles.input}
            />

            <TextInput
              label="Contact Number *"
              value={formData.contact}
              onChangeText={(text) => setFormData({ ...formData, contact: text })}
              mode="outlined"
              keyboardType="phone-pad"
              style={styles.input}
            />

            <Text style={styles.label}>Job Type</Text>
            <SegmentedButtons
              value={formData.job_type}
              onValueChange={(value) => setFormData({ ...formData, job_type: value })}
              buttons={JOB_TYPES}
              style={styles.segmented}
            />

            <TextInput
              label="Notes"
              value={formData.notes}
              onChangeText={(text) => setFormData({ ...formData, notes: text })}
              mode="outlined"
              multiline
              numberOfLines={4}
              style={styles.input}
            />

            <Button
              mode="contained"
              onPress={handleCreate}
              loading={loading}
              disabled={loading}
              style={styles.button}
            >
              Create Job
            </Button>
          </Surface>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
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
  formContainer: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
  },
  form: {
    padding: 16,
    borderRadius: 12,
    elevation: 1,
  },
  input: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    marginBottom: 8,
    color: '#666',
  },
  segmented: {
    marginBottom: 16,
  },
  button: {
    marginTop: 8,
    paddingVertical: 6,
  },
});
