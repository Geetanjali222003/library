# 🔧 POST Books Endpoint 403 Forbidden - SOLUTION GUIDE

## 🎯 Problem Identified
Your POST `/books` endpoint is returning **403 Forbidden** because it requires **ADMIN role authentication** with a valid JWT token.

## 📋 Current Security Configuration
```java
// From SecurityConfig.java (lines 50-51)
.requestMatchers(HttpMethod.POST, "/books/**", "/authors/**")
.hasRole("ADMIN")  // ← REQUIRES ADMIN ROLE
```

## 🔧 SOLUTION STEPS

### Step 1: Get Admin Credentials
Your system has a default admin user created automatically:
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `ADMIN`

### Step 2: Login to Get JWT Token

**POST Request to**: `http://localhost:8080/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Expected Response**: A JWT token string like:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTYxNjIzOTAyMn0.XYZ...
```

### Step 3: Use JWT Token for Book Creation

**POST Request to**: `http://localhost:8080/books`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Body** (raw JSON):
```json
{
    "title": "Sample Book",
    "genre": "Fiction",
    "authorId": 1
}
```

## 📝 Complete Postman Setup Guide

### 1. **Login Request**
```
Method: POST
URL: http://localhost:8080/auth/login
Headers:
  - Content-Type: application/json
Body (raw JSON):
{
    "username": "admin",
    "password": "admin123"
}
```

### 2. **Copy JWT Token from Response**

### 3. **Create Book Request**
```
Method: POST
URL: http://localhost:8080/books
Headers:
  - Content-Type: application/json
  - Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Body (raw JSON):
{
    "title": "My Test Book",
    "genre": "Fantasy",
    "authorId": 1
}
```

## ⚠️ Common Issues & Solutions

### Issue 1: "User not Found" during login
**Solution**: Make sure your application has started completely and the admin user is created automatically.

### Issue 2: Still getting 403 after adding token
**Possible causes**:
- Token format incorrect (must be `Bearer TOKEN`)
- Token expired
- Wrong role (must be ADMIN)
- AuthorId doesn't exist

### Issue 3: "Author not found" error
**Solution**: First create an author or use existing author ID:

**Create Author First**:
```
POST http://localhost:8080/authors
Headers:
  - Content-Type: application/json
  - Authorization: Bearer YOUR_JWT_TOKEN
Body:
{
    "name": "Test Author",
    "email": "author@example.com"
}
```

## 🚀 Quick Test Script for Postman

### Pre-request Script for Book Creation:
```javascript
// Auto-login and get token
pm.sendRequest({
    url: 'http://localhost:8080/auth/login',
    method: 'POST',
    header: {
        'Content-Type': 'application/json',
    },
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            username: "admin",
            password: "admin123"
        })
    }
}, function (err, response) {
    if (!err && response.code === 200) {
        const token = response.text();
        pm.globals.set("jwt_token", token);
        pm.request.headers.add({
            key: 'Authorization',
            value: 'Bearer ' + token
        });
    }
});
```

## 🔍 Debugging Steps

### 1. Check Application Startup
Make sure you see this log message:
```
Admin user created with username: admin
```

### 2. Test Login Endpoint First
Before testing books, ensure login works and returns a valid token.

### 3. Verify Token Format
Your Authorization header should look exactly like:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Check Author Exists
Make sure you have at least one author in the database before creating books.

## 📋 Alternative Solutions

### Option 1: Create Test User with ADMIN Role
```json
POST /auth/register
{
    "username": "testadmin",
    "password": "test123",
    "role": "ADMIN"
}
```

### Option 2: Temporarily Allow Unauthorized Access (Development Only)
Add this to SecurityConfig for testing:
```java
.requestMatchers(HttpMethod.POST, "/books").permitAll()
```

## ✅ Success Checklist
- [ ] Application started successfully
- [ ] Admin user exists (username: admin, password: admin123)
- [ ] Login endpoint returns JWT token
- [ ] Authorization header includes "Bearer " prefix
- [ ] At least one author exists in database
- [ ] Book request has valid authorId

## 🎯 Expected Successful Response
```json
{
    "id": 1,
    "title": "My Test Book",
    "genre": "Fantasy",
    "authorName": "Test Author"
}
```

**Status Code**: `200 OK`

Follow these steps and your POST books endpoint should work successfully! 🚀
