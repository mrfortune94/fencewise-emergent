from fastapi import FastAPI, APIRouter, HTTPException, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
import os
import logging
from pathlib import Path
from pydantic import BaseModel, Field, EmailStr
from typing import List, Optional
from datetime import datetime, timedelta
from passlib.context import CryptContext
import jwt
from bson import ObjectId

ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / '.env')

# MongoDB connection
mongo_url = os.environ['MONGO_URL']
client = AsyncIOMotorClient(mongo_url)
db = client[os.environ['DB_NAME']]

# Password hashing
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# JWT settings
SECRET_KEY = os.environ.get("SECRET_KEY", "fencewise-secret-key-change-in-production")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7  # 7 days

# Security
security = HTTPBearer()

# Create the main app
app = FastAPI()
api_router = APIRouter(prefix="/api")

# Pydantic Models
class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid objectid")
        return ObjectId(v)

    @classmethod
    def __get_pydantic_json_schema__(cls, field_schema):
        field_schema.update(type="string")


class UserRegister(BaseModel):
    name: str
    email: EmailStr
    password: str
    role: str = "worker"  # worker, supervisor, admin


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class UserResponse(BaseModel):
    id: str
    name: str
    email: str
    role: str
    created_at: datetime


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse


class JobCreate(BaseModel):
    client_name: str
    address: str
    contact: str
    job_type: str  # Standard, Channel, Corner, Raked
    notes: Optional[str] = ""


class JobUpdate(BaseModel):
    client_name: Optional[str] = None
    address: Optional[str] = None
    contact: Optional[str] = None
    job_type: Optional[str] = None
    notes: Optional[str] = None
    status: Optional[str] = None
    signature_url: Optional[str] = None


class JobResponse(BaseModel):
    id: str
    client_name: str
    address: str
    contact: str
    job_type: str
    notes: str
    status: str
    created_by: str
    created_by_name: str
    created_at: datetime
    completed_at: Optional[datetime] = None
    signature_url: Optional[str] = None


class TimesheetCreate(BaseModel):
    date: str  # YYYY-MM-DD
    start_time: str
    finish_time: str
    break_time: str
    notes: Optional[str] = ""
    job_id: Optional[str] = None


class TimesheetResponse(BaseModel):
    id: str
    user_id: str
    user_name: str
    date: str
    start_time: str
    finish_time: str
    break_time: str
    notes: str
    total_hours: float
    job_id: Optional[str] = None
    approved: bool
    created_at: datetime


class MessageCreate(BaseModel):
    to: str  # user_id or 'team'
    message: str
    image_url: Optional[str] = None


class MessageResponse(BaseModel):
    id: str
    from_id: str
    from_name: str
    to: str
    message: str
    image_url: Optional[str] = None
    timestamp: datetime


class PhotoUpload(BaseModel):
    job_id: str
    image_base64: str  # base64 encoded image
    caption: Optional[str] = ""


class PhotoResponse(BaseModel):
    id: str
    job_id: str
    user_id: str
    user_name: str
    image_base64: str
    caption: str
    uploaded_at: datetime


# Helper Functions
def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt


async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    
    user = await db.users.find_one({"_id": ObjectId(user_id)})
    if user is None:
        raise HTTPException(status_code=401, detail="User not found")
    
    return user


def calculate_hours(start: str, finish: str, break_time: str) -> float:
    """Calculate total hours worked"""
    try:
        start_h, start_m = map(int, start.split(':'))
        finish_h, finish_m = map(int, finish.split(':'))
        break_h, break_m = map(int, break_time.split(':'))
        
        start_minutes = start_h * 60 + start_m
        finish_minutes = finish_h * 60 + finish_m
        break_minutes = break_h * 60 + break_m
        
        work_minutes = finish_minutes - start_minutes - break_minutes
        return round(work_minutes / 60, 2)
    except:
        return 0.0


# Auth Endpoints
@api_router.post("/auth/register", response_model=TokenResponse)
async def register(user_data: UserRegister):
    # Check if user exists
    existing_user = await db.users.find_one({"email": user_data.email})
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    # Create user
    user_dict = {
        "name": user_data.name,
        "email": user_data.email,
        "password": hash_password(user_data.password),
        "role": user_data.role,
        "created_at": datetime.utcnow(),
        "active": True
    }
    
    result = await db.users.insert_one(user_dict)
    user_id = str(result.inserted_id)
    
    # Create token
    access_token = create_access_token(data={"sub": user_id})
    
    user_response = UserResponse(
        id=user_id,
        name=user_data.name,
        email=user_data.email,
        role=user_data.role,
        created_at=user_dict["created_at"]
    )
    
    return TokenResponse(access_token=access_token, user=user_response)


@api_router.post("/auth/login", response_model=TokenResponse)
async def login(credentials: UserLogin):
    user = await db.users.find_one({"email": credentials.email})
    if not user or not verify_password(credentials.password, user["password"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")
    
    if not user.get("active", True):
        raise HTTPException(status_code=403, detail="Account is suspended")
    
    access_token = create_access_token(data={"sub": str(user["_id"])})
    
    user_response = UserResponse(
        id=str(user["_id"]),
        name=user["name"],
        email=user["email"],
        role=user["role"],
        created_at=user["created_at"]
    )
    
    return TokenResponse(access_token=access_token, user=user_response)


@api_router.get("/auth/me", response_model=UserResponse)
async def get_me(current_user: dict = Depends(get_current_user)):
    return UserResponse(
        id=str(current_user["_id"]),
        name=current_user["name"],
        email=current_user["email"],
        role=current_user["role"],
        created_at=current_user["created_at"]
    )


# Job Endpoints
@api_router.post("/jobs", response_model=JobResponse)
async def create_job(job_data: JobCreate, current_user: dict = Depends(get_current_user)):
    job_dict = {
        **job_data.dict(),
        "status": "pending",
        "created_by": str(current_user["_id"]),
        "created_by_name": current_user["name"],
        "created_at": datetime.utcnow(),
        "completed_at": None,
        "signature_url": None
    }
    
    result = await db.jobs.insert_one(job_dict)
    job_dict["id"] = str(result.inserted_id)
    
    return JobResponse(**job_dict)


@api_router.get("/jobs", response_model=List[JobResponse])
async def get_jobs(current_user: dict = Depends(get_current_user)):
    # Admin/Supervisor see all jobs, workers see their own
    if current_user["role"] in ["admin", "supervisor"]:
        query = {}
    else:
        query = {"created_by": str(current_user["_id"])}
    
    jobs = await db.jobs.find(query).sort("created_at", -1).to_list(1000)
    
    result = []
    for job in jobs:
        job["id"] = str(job.pop("_id"))
        result.append(JobResponse(**job))
    
    return result


@api_router.get("/jobs/{job_id}", response_model=JobResponse)
async def get_job(job_id: str, current_user: dict = Depends(get_current_user)):
    job = await db.jobs.find_one({"_id": ObjectId(job_id)})
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job["id"] = str(job.pop("_id"))
    return JobResponse(**job)


@api_router.put("/jobs/{job_id}", response_model=JobResponse)
async def update_job(job_id: str, job_data: JobUpdate, current_user: dict = Depends(get_current_user)):
    job = await db.jobs.find_one({"_id": ObjectId(job_id)})
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    
    # Only creator, supervisor, or admin can update
    if current_user["role"] not in ["admin", "supervisor"] and job["created_by"] != str(current_user["_id"]):
        raise HTTPException(status_code=403, detail="Not authorized")
    
    update_data = {k: v for k, v in job_data.dict().items() if v is not None}
    
    if update_data.get("status") == "completed" and not job.get("completed_at"):
        update_data["completed_at"] = datetime.utcnow()
    
    await db.jobs.update_one({"_id": ObjectId(job_id)}, {"$set": update_data})
    
    updated_job = await db.jobs.find_one({"_id": ObjectId(job_id)})
    updated_job["id"] = str(updated_job.pop("_id"))
    
    return JobResponse(**updated_job)


@api_router.delete("/jobs/{job_id}")
async def delete_job(job_id: str, current_user: dict = Depends(get_current_user)):
    # Only admin can delete
    if current_user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Not authorized")
    
    result = await db.jobs.delete_one({"_id": ObjectId(job_id)})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Job not found")
    
    return {"message": "Job deleted successfully"}


# Timesheet Endpoints
@api_router.post("/timesheets", response_model=TimesheetResponse)
async def create_timesheet(timesheet_data: TimesheetCreate, current_user: dict = Depends(get_current_user)):
    total_hours = calculate_hours(
        timesheet_data.start_time,
        timesheet_data.finish_time,
        timesheet_data.break_time
    )
    
    timesheet_dict = {
        **timesheet_data.dict(),
        "user_id": str(current_user["_id"]),
        "user_name": current_user["name"],
        "total_hours": total_hours,
        "approved": False,
        "created_at": datetime.utcnow()
    }
    
    result = await db.timesheets.insert_one(timesheet_dict)
    timesheet_dict["id"] = str(result.inserted_id)
    
    return TimesheetResponse(**timesheet_dict)


@api_router.get("/timesheets", response_model=List[TimesheetResponse])
async def get_timesheets(current_user: dict = Depends(get_current_user)):
    # Admin/Supervisor see all timesheets, workers see their own
    if current_user["role"] in ["admin", "supervisor"]:
        query = {}
    else:
        query = {"user_id": str(current_user["_id"])}
    
    timesheets = await db.timesheets.find(query).sort("date", -1).to_list(1000)
    
    result = []
    for ts in timesheets:
        ts["id"] = str(ts.pop("_id"))
        result.append(TimesheetResponse(**ts))
    
    return result


@api_router.put("/timesheets/{timesheet_id}/approve")
async def approve_timesheet(timesheet_id: str, current_user: dict = Depends(get_current_user)):
    if current_user["role"] not in ["admin", "supervisor"]:
        raise HTTPException(status_code=403, detail="Not authorized")
    
    result = await db.timesheets.update_one(
        {"_id": ObjectId(timesheet_id)},
        {"$set": {"approved": True}}
    )
    
    if result.modified_count == 0:
        raise HTTPException(status_code=404, detail="Timesheet not found")
    
    return {"message": "Timesheet approved"}


# Message Endpoints
@api_router.post("/messages", response_model=MessageResponse)
async def send_message(message_data: MessageCreate, current_user: dict = Depends(get_current_user)):
    message_dict = {
        "from_id": str(current_user["_id"]),
        "from_name": current_user["name"],
        "to": message_data.to,
        "message": message_data.message,
        "image_url": message_data.image_url,
        "timestamp": datetime.utcnow()
    }
    
    result = await db.messages.insert_one(message_dict)
    message_dict["id"] = str(result.inserted_id)
    
    return MessageResponse(**message_dict)


@api_router.get("/messages/{channel}", response_model=List[MessageResponse])
async def get_messages(channel: str, current_user: dict = Depends(get_current_user)):
    # For team channel or private messages
    if channel == "team":
        query = {"to": "team"}
    else:
        # Private messages between two users
        user_id = str(current_user["_id"])
        query = {
            "$or": [
                {"from_id": user_id, "to": channel},
                {"from_id": channel, "to": user_id}
            ]
        }
    
    messages = await db.messages.find(query).sort("timestamp", 1).to_list(1000)
    
    result = []
    for msg in messages:
        msg["id"] = str(msg.pop("_id"))
        result.append(MessageResponse(**msg))
    
    return result


# Photo Endpoints
@api_router.post("/photos", response_model=PhotoResponse)
async def upload_photo(photo_data: PhotoUpload, current_user: dict = Depends(get_current_user)):
    # Verify job exists
    job = await db.jobs.find_one({"_id": ObjectId(photo_data.job_id)})
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    
    photo_dict = {
        "job_id": photo_data.job_id,
        "user_id": str(current_user["_id"]),
        "user_name": current_user["name"],
        "image_base64": photo_data.image_base64,
        "caption": photo_data.caption,
        "uploaded_at": datetime.utcnow()
    }
    
    result = await db.photos.insert_one(photo_dict)
    photo_dict["id"] = str(result.inserted_id)
    
    return PhotoResponse(**photo_dict)


@api_router.get("/photos/job/{job_id}", response_model=List[PhotoResponse])
async def get_job_photos(job_id: str, current_user: dict = Depends(get_current_user)):
    photos = await db.photos.find({"job_id": job_id}).sort("uploaded_at", -1).to_list(1000)
    
    result = []
    for photo in photos:
        photo["id"] = str(photo.pop("_id"))
        result.append(PhotoResponse(**photo))
    
    return result


# Admin User Management
@api_router.get("/users", response_model=List[UserResponse])
async def get_users(current_user: dict = Depends(get_current_user)):
    if current_user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Admin only")
    
    users = await db.users.find().to_list(1000)
    result = []
    for user in users:
        result.append(UserResponse(
            id=str(user["_id"]),
            name=user["name"],
            email=user["email"],
            role=user["role"],
            created_at=user["created_at"]
        ))
    
    return result


@api_router.put("/users/{user_id}/suspend")
async def suspend_user(user_id: str, current_user: dict = Depends(get_current_user)):
    if current_user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Admin only")
    
    await db.users.update_one(
        {"_id": ObjectId(user_id)},
        {"$set": {"active": False}}
    )
    
    return {"message": "User suspended"}


@api_router.put("/users/{user_id}/activate")
async def activate_user(user_id: str, current_user: dict = Depends(get_current_user)):
    if current_user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Admin only")
    
    await db.users.update_one(
        {"_id": ObjectId(user_id)},
        {"$set": {"active": True}}
    )
    
    return {"message": "User activated"}


# Dashboard Stats
@api_router.get("/dashboard/stats")
async def get_dashboard_stats(current_user: dict = Depends(get_current_user)):
    stats = {}
    
    if current_user["role"] == "admin":
        stats["total_jobs"] = await db.jobs.count_documents({})
        stats["pending_jobs"] = await db.jobs.count_documents({"status": "pending"})
        stats["active_jobs"] = await db.jobs.count_documents({"status": "active"})
        stats["completed_jobs"] = await db.jobs.count_documents({"status": "completed"})
        stats["total_users"] = await db.users.count_documents({})
        stats["pending_timesheets"] = await db.timesheets.count_documents({"approved": False})
    else:
        user_id = str(current_user["_id"])
        stats["my_jobs"] = await db.jobs.count_documents({"created_by": user_id})
        stats["my_pending_jobs"] = await db.jobs.count_documents({"created_by": user_id, "status": "pending"})
        stats["my_timesheets"] = await db.timesheets.count_documents({"user_id": user_id})
    
    return stats


# Include router
app.include_router(api_router)

app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

@app.on_event("shutdown")
async def shutdown_db_client():
    client.close()
