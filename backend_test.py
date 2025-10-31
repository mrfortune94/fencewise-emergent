#!/usr/bin/env python3
"""
FenceWise Backend API Test Suite
Tests all backend endpoints for the FenceWise application
"""

import requests
import json
from datetime import datetime
import sys

# Use the backend URL from frontend .env
BACKEND_URL = "https://precast-tracker.preview.emergentagent.com/api"

class FenceWiseAPITester:
    def __init__(self):
        self.base_url = BACKEND_URL
        self.session = requests.Session()
        self.auth_token = None
        self.test_user_id = None
        self.test_job_id = None
        self.test_timesheet_id = None
        
        # Test data
        self.test_user = {
            "name": "Test Worker",
            "email": "worker@test.com",
            "password": "test123",
            "role": "worker"
        }
        
        self.test_job = {
            "client_name": "Test Client",
            "address": "123 Test St",
            "contact": "555-1234",
            "job_type": "Standard",
            "notes": "Test job"
        }
        
        self.test_timesheet = {
            "date": "2025-01-15",
            "start_time": "08:00",
            "finish_time": "17:00",
            "break_time": "01:00",
            "notes": "Test work"
        }
        
        self.results = {
            "passed": 0,
            "failed": 0,
            "errors": []
        }

    def log_result(self, test_name, success, message="", response=None):
        """Log test results"""
        status = "✅ PASS" if success else "❌ FAIL"
        print(f"{status}: {test_name}")
        
        if message:
            print(f"   {message}")
            
        if response and not success:
            print(f"   Status Code: {response.status_code}")
            try:
                print(f"   Response: {response.json()}")
            except:
                print(f"   Response: {response.text}")
        
        if success:
            self.results["passed"] += 1
        else:
            self.results["failed"] += 1
            self.results["errors"].append(f"{test_name}: {message}")
        
        print()

    def test_auth_register(self):
        """Test user registration"""
        try:
            response = self.session.post(
                f"{self.base_url}/auth/register",
                json=self.test_user,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "access_token" in data and "user" in data:
                    self.auth_token = data["access_token"]
                    self.test_user_id = data["user"]["id"]
                    self.session.headers.update({"Authorization": f"Bearer {self.auth_token}"})
                    self.log_result("POST /api/auth/register", True, "User registered successfully")
                    return True
                else:
                    self.log_result("POST /api/auth/register", False, "Missing token or user in response", response)
            else:
                # If user already exists, try to login instead
                if response.status_code == 400 and "already registered" in response.text:
                    return self.test_auth_login()
                else:
                    self.log_result("POST /api/auth/register", False, f"Registration failed", response)
            
        except Exception as e:
            self.log_result("POST /api/auth/register", False, f"Exception: {str(e)}")
        
        return False

    def test_auth_login(self):
        """Test user login"""
        try:
            login_data = {
                "email": self.test_user["email"],
                "password": self.test_user["password"]
            }
            
            response = self.session.post(
                f"{self.base_url}/auth/login",
                json=login_data,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "access_token" in data and "user" in data:
                    self.auth_token = data["access_token"]
                    self.test_user_id = data["user"]["id"]
                    self.session.headers.update({"Authorization": f"Bearer {self.auth_token}"})
                    self.log_result("POST /api/auth/login", True, "Login successful")
                    return True
                else:
                    self.log_result("POST /api/auth/login", False, "Missing token or user in response", response)
            else:
                self.log_result("POST /api/auth/login", False, "Login failed", response)
                
        except Exception as e:
            self.log_result("POST /api/auth/login", False, f"Exception: {str(e)}")
        
        return False

    def test_auth_me(self):
        """Test get current user"""
        try:
            response = self.session.get(f"{self.base_url}/auth/me")
            
            if response.status_code == 200:
                data = response.json()
                if "id" in data and "name" in data and "email" in data:
                    self.log_result("GET /api/auth/me", True, "User profile retrieved successfully")
                    return True
                else:
                    self.log_result("GET /api/auth/me", False, "Missing user data in response", response)
            else:
                self.log_result("GET /api/auth/me", False, "Failed to get user profile", response)
                
        except Exception as e:
            self.log_result("GET /api/auth/me", False, f"Exception: {str(e)}")
        
        return False

    def test_create_job(self):
        """Test job creation"""
        try:
            response = self.session.post(
                f"{self.base_url}/jobs",
                json=self.test_job,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "id" in data and "client_name" in data:
                    self.test_job_id = data["id"]
                    self.log_result("POST /api/jobs", True, f"Job created with ID: {self.test_job_id}")
                    return True
                else:
                    self.log_result("POST /api/jobs", False, "Missing job data in response", response)
            else:
                self.log_result("POST /api/jobs", False, "Job creation failed", response)
                
        except Exception as e:
            self.log_result("POST /api/jobs", False, f"Exception: {str(e)}")
        
        return False

    def test_get_jobs(self):
        """Test getting all jobs"""
        try:
            response = self.session.get(f"{self.base_url}/jobs")
            
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, list):
                    self.log_result("GET /api/jobs", True, f"Retrieved {len(data)} jobs")
                    return True
                else:
                    self.log_result("GET /api/jobs", False, "Response is not a list", response)
            else:
                self.log_result("GET /api/jobs", False, "Failed to get jobs", response)
                
        except Exception as e:
            self.log_result("GET /api/jobs", False, f"Exception: {str(e)}")
        
        return False

    def test_get_job_by_id(self):
        """Test getting specific job"""
        if not self.test_job_id:
            self.log_result("GET /api/jobs/{job_id}", False, "No job ID available for testing")
            return False
            
        try:
            response = self.session.get(f"{self.base_url}/jobs/{self.test_job_id}")
            
            if response.status_code == 200:
                data = response.json()
                if "id" in data and data["id"] == self.test_job_id:
                    self.log_result("GET /api/jobs/{job_id}", True, "Job retrieved successfully")
                    return True
                else:
                    self.log_result("GET /api/jobs/{job_id}", False, "Job ID mismatch", response)
            else:
                self.log_result("GET /api/jobs/{job_id}", False, "Failed to get job", response)
                
        except Exception as e:
            self.log_result("GET /api/jobs/{job_id}", False, f"Exception: {str(e)}")
        
        return False

    def test_update_job(self):
        """Test updating job status"""
        if not self.test_job_id:
            self.log_result("PUT /api/jobs/{job_id}", False, "No job ID available for testing")
            return False
            
        try:
            update_data = {"status": "active"}
            response = self.session.put(
                f"{self.base_url}/jobs/{self.test_job_id}",
                json=update_data,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get("status") == "active":
                    self.log_result("PUT /api/jobs/{job_id}", True, "Job status updated to active")
                    return True
                else:
                    self.log_result("PUT /api/jobs/{job_id}", False, "Status not updated correctly", response)
            else:
                self.log_result("PUT /api/jobs/{job_id}", False, "Failed to update job", response)
                
        except Exception as e:
            self.log_result("PUT /api/jobs/{job_id}", False, f"Exception: {str(e)}")
        
        return False

    def test_dashboard_stats(self):
        """Test dashboard statistics"""
        try:
            response = self.session.get(f"{self.base_url}/dashboard/stats")
            
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, dict) and len(data) > 0:
                    self.log_result("GET /api/dashboard/stats", True, f"Stats retrieved: {list(data.keys())}")
                    return True
                else:
                    self.log_result("GET /api/dashboard/stats", False, "Empty or invalid stats response", response)
            else:
                self.log_result("GET /api/dashboard/stats", False, "Failed to get dashboard stats", response)
                
        except Exception as e:
            self.log_result("GET /api/dashboard/stats", False, f"Exception: {str(e)}")
        
        return False

    def test_create_timesheet(self):
        """Test timesheet creation"""
        try:
            response = self.session.post(
                f"{self.base_url}/timesheets",
                json=self.test_timesheet,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if "id" in data and "total_hours" in data:
                    self.test_timesheet_id = data["id"]
                    self.log_result("POST /api/timesheets", True, f"Timesheet created with {data['total_hours']} hours")
                    return True
                else:
                    self.log_result("POST /api/timesheets", False, "Missing timesheet data in response", response)
            else:
                self.log_result("POST /api/timesheets", False, "Timesheet creation failed", response)
                
        except Exception as e:
            self.log_result("POST /api/timesheets", False, f"Exception: {str(e)}")
        
        return False

    def test_get_timesheets(self):
        """Test getting timesheets"""
        try:
            response = self.session.get(f"{self.base_url}/timesheets")
            
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, list):
                    self.log_result("GET /api/timesheets", True, f"Retrieved {len(data)} timesheets")
                    return True
                else:
                    self.log_result("GET /api/timesheets", False, "Response is not a list", response)
            else:
                self.log_result("GET /api/timesheets", False, "Failed to get timesheets", response)
                
        except Exception as e:
            self.log_result("GET /api/timesheets", False, f"Exception: {str(e)}")
        
        return False

    def run_all_tests(self):
        """Run all API tests"""
        print("=" * 60)
        print("FenceWise Backend API Test Suite")
        print("=" * 60)
        print(f"Testing backend at: {self.base_url}")
        print()
        
        # Authentication Flow
        print("🔐 AUTHENTICATION TESTS")
        print("-" * 30)
        auth_success = self.test_auth_register()
        if auth_success:
            self.test_auth_me()
        
        # Jobs CRUD Tests
        print("📋 JOBS CRUD TESTS")
        print("-" * 30)
        if auth_success:
            self.test_create_job()
            self.test_get_jobs()
            self.test_get_job_by_id()
            self.test_update_job()
        
        # Dashboard Tests
        print("📊 DASHBOARD TESTS")
        print("-" * 30)
        if auth_success:
            self.test_dashboard_stats()
        
        # Timesheet Tests
        print("⏰ TIMESHEET TESTS")
        print("-" * 30)
        if auth_success:
            self.test_create_timesheet()
            self.test_get_timesheets()
        
        # Summary
        print("=" * 60)
        print("TEST SUMMARY")
        print("=" * 60)
        print(f"✅ Passed: {self.results['passed']}")
        print(f"❌ Failed: {self.results['failed']}")
        print(f"📊 Total: {self.results['passed'] + self.results['failed']}")
        
        if self.results['errors']:
            print("\n🚨 FAILED TESTS:")
            for error in self.results['errors']:
                print(f"   • {error}")
        
        return self.results['failed'] == 0

if __name__ == "__main__":
    tester = FenceWiseAPITester()
    success = tester.run_all_tests()
    sys.exit(0 if success else 1)