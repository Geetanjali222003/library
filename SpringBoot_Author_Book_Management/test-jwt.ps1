# PowerShell script to test JWT and Security functionality

Write-Host "=== Testing JWT and Security Functionality ===" -ForegroundColor Green
Write-Host ""

# Test 1: Register a user
Write-Host "1. Testing user registration:" -ForegroundColor Yellow
try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"username": "testuser", "password": "password123", "role": "USER"}'
    Write-Host "Registration Response: $registerResponse" -ForegroundColor Green
} catch {
    Write-Host "Registration Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Login with the user
Write-Host "2. Testing user login:" -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/auth/login?username=testuser&password=password123" -Method POST
    Write-Host "Token: $loginResponse" -ForegroundColor Green
    $TOKEN = $loginResponse
} catch {
    Write-Host "Login Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Try to access protected endpoint without token (should fail)
Write-Host "3. Testing access without JWT token (should fail):" -ForegroundColor Yellow
try {
    $authorsResponse = Invoke-RestMethod -Uri "http://localhost:8081/authors" -Method GET
    Write-Host "Unexpected success: $authorsResponse" -ForegroundColor Red
} catch {
    Write-Host "Expected failure: $($_.Exception.Message)" -ForegroundColor Green
}
Write-Host ""

# Test 4: Try to access protected endpoint with token
Write-Host "4. Testing access with JWT token:" -ForegroundColor Yellow
try {
    $headers = @{"Authorization" = "Bearer $TOKEN"}
    $authorsWithTokenResponse = Invoke-RestMethod -Uri "http://localhost:8081/authors" -Method GET -Headers $headers
    Write-Host "Authors Response: $authorsWithTokenResponse" -ForegroundColor Green
} catch {
    Write-Host "Access Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Test admin login
Write-Host "5. Testing admin login:" -ForegroundColor Yellow
try {
    $adminLoginResponse = Invoke-RestMethod -Uri "http://localhost:8081/auth/login?username=admin&password=admin123" -Method POST
    Write-Host "Admin Token: $adminLoginResponse" -ForegroundColor Green
} catch {
    Write-Host "Admin Login Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Tests completed ===" -ForegroundColor Green
