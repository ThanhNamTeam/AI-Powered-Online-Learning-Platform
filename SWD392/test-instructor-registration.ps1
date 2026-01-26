# Test Registration with INSTRUCTOR Role
# After fixing database constraint

$baseUrl = "http://localhost:8080"

Write-Host "=== Testing INSTRUCTOR Registration After Fix ===" -ForegroundColor Cyan
Write-Host ""

# Test data
$instructorData = @{
    fullName = "minhkhoi"
    email = "minkoi0408@gmail.com"
    password = "123456"
    otpCode = "383152"
    role = "INSTRUCTOR"
} | ConvertTo-Json

Write-Host "Request Body:" -ForegroundColor Yellow
Write-Host $instructorData -ForegroundColor Gray
Write-Host ""

try {
    Write-Host "Sending request..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "$baseUrl/api/accounts" -Method Post -Body $instructorData -ContentType "application/json"
    
    Write-Host "✅ SUCCESS!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 10
    
} catch {
    Write-Host "❌ ERROR!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $errorBody = $reader.ReadToEnd()
    Write-Host "Error Response:" -ForegroundColor Red
    $errorBody | ConvertFrom-Json | ConvertTo-Json -Depth 10
}
