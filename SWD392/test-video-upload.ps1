# Script test upload video lên Cloudinary
# Sử dụng: .\test-video-upload.ps1 -VideoPath "path/to/video.mp4" -Transcribe $true

param(
    [Parameter(Mandatory=$true)]
    [string]$VideoPath,
    
    [Parameter(Mandatory=$false)]
    [bool]$Transcribe = $true,
    
    [Parameter(Mandatory=$false)]
    [string]$ApiUrl = "http://localhost:8080/api/videos/upload"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST VIDEO UPLOAD TO CLOUDINARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra file tồn tại
if (-not (Test-Path $VideoPath)) {
    Write-Host "ERROR: File không tồn tại: $VideoPath" -ForegroundColor Red
    exit 1
}

# Lấy thông tin file
$fileInfo = Get-Item $VideoPath
$fileSizeMB = [math]::Round($fileInfo.Length / 1MB, 2)

Write-Host "File video: $($fileInfo.Name)" -ForegroundColor Green
Write-Host "Kích thước: $fileSizeMB MB" -ForegroundColor Green
Write-Host "Transcribe: $Transcribe" -ForegroundColor Green
Write-Host ""

# Kiểm tra server có chạy không
Write-Host "Kiểm tra server..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction SilentlyContinue
    Write-Host "✓ Server đang chạy" -ForegroundColor Green
} catch {
    Write-Host "✗ Server chưa chạy hoặc không truy cập được" -ForegroundColor Red
    Write-Host "  Hãy chạy: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Đang upload video..." -ForegroundColor Yellow
Write-Host ""

# Tạo form data
$uri = "$ApiUrl`?transcribe=$($Transcribe.ToString().ToLower())"
$form = @{
    file = Get-Item -Path $VideoPath
}

# Upload
try {
    $startTime = Get-Date
    
    $response = Invoke-RestMethod -Uri $uri -Method Post -Form $form
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  UPLOAD THÀNH CÔNG!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Thời gian upload: $([math]::Round($duration, 2)) giây" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Kết quả:" -ForegroundColor Cyan
    Write-Host "--------" -ForegroundColor Cyan
    Write-Host "Video URL: $($response.videoUrl)" -ForegroundColor White
    Write-Host "Public ID: $($response.publicId)" -ForegroundColor White
    
    if ($response.duration) {
        Write-Host "Độ dài video: $($response.duration) giây" -ForegroundColor White
    }
    
    if ($response.format) {
        Write-Host "Format: $($response.format)" -ForegroundColor White
    }
    
    if ($response.fileSize) {
        $uploadedSizeMB = [math]::Round($response.fileSize / 1MB, 2)
        Write-Host "Kích thước: $uploadedSizeMB MB" -ForegroundColor White
    }
    
    Write-Host ""
    
    if ($response.transcript) {
        Write-Host "Transcript:" -ForegroundColor Cyan
        Write-Host "----------" -ForegroundColor Cyan
        $transcriptPreview = if ($response.transcript.Length -gt 500) {
            $response.transcript.Substring(0, 500) + "..."
        } else {
            $response.transcript
        }
        Write-Host $transcriptPreview -ForegroundColor White
        Write-Host ""
        Write-Host "(Độ dài transcript: $($response.transcript.Length) ký tự)" -ForegroundColor Gray
    }
    
    Write-Host ""
    Write-Host "Message: $($response.message)" -ForegroundColor Green
    Write-Host ""
    
    # Lưu response vào file
    $outputFile = "upload-result-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $response | ConvertTo-Json -Depth 10 | Out-File $outputFile
    Write-Host "✓ Kết quả đã được lưu vào: $outputFile" -ForegroundColor Green
    
    # Mở video URL trong browser
    Write-Host ""
    $openBrowser = Read-Host "Bạn có muốn mở video trong browser? (y/n)"
    if ($openBrowser -eq 'y' -or $openBrowser -eq 'Y') {
        Start-Process $response.videoUrl
    }
    
} catch {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  UPLOAD THẤT BẠI!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    
    if ($_.ErrorDetails.Message) {
        Write-Host "Chi tiết:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Gợi ý khắc phục:" -ForegroundColor Cyan
    Write-Host "1. Kiểm tra Cloudinary credentials trong application.yaml" -ForegroundColor White
    Write-Host "2. Đảm bảo file là video hợp lệ (.mp4, .avi, .mov, etc.)" -ForegroundColor White
    Write-Host "3. Kiểm tra kích thước file < 100MB" -ForegroundColor White
    Write-Host "4. Xem log của server để biết thêm chi tiết" -ForegroundColor White
    
    exit 1
}
