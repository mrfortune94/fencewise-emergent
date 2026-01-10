---
applyTo: ['backend/**', '!backend/__pycache__/**', '!backend/.pytest_cache/**', '!backend/*.pyc']
---

# Backend Instructions (Python/FastAPI)

## Overview

This is a FastAPI backend service with MongoDB database, providing RESTful APIs for the FenceWise application.

## Development Setup

### Starting the Server
```bash
cd backend
pip install -r requirements.txt
uvicorn server:app --reload
```

The server will run on `http://localhost:8000` by default.

### Environment Variables
Create a `.env` file in the `backend/` directory:
```
MONGO_URL=mongodb://localhost:27017
DB_NAME=fencewise
SECRET_KEY=your-secret-key-change-in-production
```

## Code Style

### Python Style Guide
- Follow **PEP 8** style guide strictly
- Use **Black** for code formatting (line length 88)
- Use **isort** for import sorting
- Use **flake8** for linting
- Use **mypy** for type checking

### Formatting and Linting
```bash
black .                # Format code
isort .                # Sort imports
flake8                 # Check style
mypy .                 # Type check
```

### Type Hints
- **Always use type hints** for function parameters and return values
- Use `Optional[T]` for nullable values
- Use `List[T]`, `Dict[K, V]` for collections
- Use Pydantic models for structured data

Example:
```python
from typing import List, Optional
from pydantic import BaseModel

async def get_user(user_id: str) -> Optional[dict]:
    user = await db.users.find_one({"_id": ObjectId(user_id)})
    return user

def process_items(items: List[str]) -> List[dict]:
    return [{"name": item} for item in items]
```

## API Design

### Route Organization
- All routes use `/api` prefix via `api_router`
- Group related endpoints together
- Use appropriate HTTP methods:
  - `GET` for retrieving data
  - `POST` for creating resources
  - `PUT/PATCH` for updating resources
  - `DELETE` for deleting resources

Example:
```python
from fastapi import APIRouter

api_router = APIRouter(prefix="/api")

@api_router.get("/users")
async def list_users():
    pass

@api_router.post("/users")
async def create_user():
    pass

@api_router.get("/users/{user_id}")
async def get_user(user_id: str):
    pass
```

### Request/Response Models
- Use **Pydantic models** for request validation and response serialization
- Define clear model names: `UserCreate`, `UserResponse`, `UserUpdate`
- Use `Field()` for validation and documentation

Example:
```python
from pydantic import BaseModel, Field, EmailStr

class UserCreate(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=8)
    name: str

class UserResponse(BaseModel):
    id: str
    email: EmailStr
    name: str
    created_at: datetime
```

## Database

### MongoDB with Motor
- Use **Motor** (async MongoDB driver) for all database operations
- Always use `await` for database operations
- Use proper error handling for database operations

Example:
```python
from motor.motor_asyncio import AsyncIOMotorClient
from bson import ObjectId

# Find one document
user = await db.users.find_one({"_id": ObjectId(user_id)})

# Find multiple documents
users = await db.users.find({"role": "admin"}).to_list(length=100)

# Insert document
result = await db.users.insert_one(user_dict)
user_id = str(result.inserted_id)

# Update document
await db.users.update_one(
    {"_id": ObjectId(user_id)},
    {"$set": {"name": "New Name"}}
)

# Delete document
await db.users.delete_one({"_id": ObjectId(user_id)})
```

### ObjectId Handling
- Use custom `PyObjectId` class for Pydantic models
- Convert ObjectId to string when returning to client
- Validate ObjectId format before queries

Example:
```python
from bson import ObjectId

class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid objectid")
        return ObjectId(v)
```

## Authentication & Security

### JWT Tokens
- Use **JWT** for authentication with HS256 algorithm
- Token expiration: 7 days (configurable via `ACCESS_TOKEN_EXPIRE_MINUTES`)
- Secret key stored in environment variable `SECRET_KEY`

### Password Hashing
- Use **bcrypt** via passlib for password hashing
- Never store plain text passwords
- Hash passwords before storing, verify during login

Example:
```python
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Hash password
hashed_password = pwd_context.hash(plain_password)

# Verify password
is_valid = pwd_context.verify(plain_password, hashed_password)
```

### Protected Routes
- Use `HTTPBearer` for token authentication
- Create dependency for current user extraction
- Return 401 for invalid/missing tokens

Example:
```python
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

security = HTTPBearer()

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> dict:
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload.get("sub")
        if user_id is None:
            raise HTTPException(status_code=401, detail="Invalid token")
        user = await db.users.find_one({"_id": ObjectId(user_id)})
        if user is None:
            raise HTTPException(status_code=401, detail="User not found")
        return user
    except jwt.JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

@api_router.get("/protected")
async def protected_route(current_user: dict = Depends(get_current_user)):
    return {"message": f"Hello {current_user['name']}"}
```

## Error Handling

### HTTPException
- Use `HTTPException` for API errors
- Provide clear error messages
- Use appropriate status codes

Example:
```python
from fastapi import HTTPException, status

# 404 Not Found
if not user:
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="User not found"
    )

# 400 Bad Request
if not is_valid_email(email):
    raise HTTPException(
        status_code=status.HTTP_400_BAD_REQUEST,
        detail="Invalid email format"
    )

# 403 Forbidden
if not user.is_admin:
    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Admin access required"
    )
```

### Try-Catch for Database Operations
```python
try:
    result = await db.users.insert_one(user_dict)
except Exception as e:
    logging.error(f"Database error: {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail="Database operation failed"
    )
```

## CORS Configuration

- Configure CORS via `CORSMiddleware`
- Allow appropriate origins for development and production
- Be restrictive in production

Example:
```python
from starlette.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:19006"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

## Logging

- Use Python's `logging` module
- Log errors and important events
- Include context in log messages

Example:
```python
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

logger.info(f"User {user_id} logged in")
logger.error(f"Failed to create user: {error}")
logger.warning(f"Invalid login attempt for {email}")
```

## Testing

### Pytest
- Write tests in files starting with `test_`
- Use pytest fixtures for setup/teardown
- Test happy paths and error cases
- Mock database operations in tests

Example:
```python
import pytest
from fastapi.testclient import TestClient
from server import app

client = TestClient(app)

def test_create_user():
    response = client.post(
        "/api/users",
        json={
            "email": "test@example.com",
            "password": "password123",
            "name": "Test User"
        }
    )
    assert response.status_code == 201
    assert response.json()["email"] == "test@example.com"
```

## Common Patterns

### List Endpoint with Pagination
```python
from typing import List

@api_router.get("/items", response_model=List[ItemResponse])
async def list_items(skip: int = 0, limit: int = 10):
    items = await db.items.find().skip(skip).limit(limit).to_list(length=limit)
    return items
```

### Create Endpoint
```python
@api_router.post("/items", response_model=ItemResponse, status_code=status.HTTP_201_CREATED)
async def create_item(item: ItemCreate):
    item_dict = item.dict()
    item_dict["created_at"] = datetime.utcnow()
    result = await db.items.insert_one(item_dict)
    item_dict["id"] = str(result.inserted_id)
    return item_dict
```

## Don't

- Don't commit secrets or API keys - use environment variables
- Don't use blocking I/O - use async operations
- Don't skip type hints - always provide them
- Don't ignore exceptions - handle them properly
- Don't store plain text passwords - always hash
- Don't use print() for logging - use logging module
- Don't skip validation - use Pydantic models
- Don't hardcode configuration - use environment variables
