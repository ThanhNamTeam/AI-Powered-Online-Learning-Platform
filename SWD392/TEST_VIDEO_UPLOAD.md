# Hướng Dẫn Test Chức Năng Upload Video

## Bước 1: Cấu hình Cloudinary

### 1.1. Lấy thông tin Cloudinary
1. Đăng nhập vào [Cloudinary Dashboard](https://cloudinary.com/console)
2. Copy các thông tin sau:
   - **Cloud Name**: Tên cloud của bạn
   - **API Key**: Key để xác thực
   - **API Secret**: Secret key

### 1.2. Cập nhật file application.yaml
Mở file `src/main/resources/application.yaml` và cập nhật:

```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME:your-cloud-name}  # Thay your-cloud-name
  api-key: ${CLOUDINARY_API_KEY:your-api-key}          # Thay your-api-key
  api-secret: ${CLOUDINARY_API_SECRET:your-api-secret}  # Thay your-api-secret
  folder: swd392-videos
```

**Hoặc** tạo file `.env` (khuyến nghị cho bảo mật):
```
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

## Bước 2: Khởi động ứng dụng

```bash
# Từ thư mục gốc của project
mvn spring-boot:run
```

Đợi cho đến khi thấy log: `Started SWD392Application in X seconds`

## Bước 3: Test Upload Video

### 3.1. Sử dụng Swagger UI (Dễ nhất)

1. Mở trình duyệt và truy cập: http://localhost:8080/swagger-ui.html
2. Tìm section **"Video Management"**
3. Click vào endpoint **POST /api/videos/upload**
4. Click nút **"Try it out"**
5. Chọn file video từ máy tính (file .mp4, .avi, .mov, etc.)
6. Chọn `transcribe`: 
   - `true` - Upload và tạo transcript (mất thời gian hơn)
   - `false` - Chỉ upload video
7. Click **"Execute"**

### 3.2. Sử dụng Postman

1. Tạo request mới với method **POST**
2. URL: `http://localhost:8080/api/videos/upload`
3. Vào tab **Body** → chọn **form-data**
4. Thêm 2 fields:
   - Key: `file` (type: File) → chọn file video
   - Key: `transcribe` (type: Text) → value: `true` hoặc `false`
5. Click **Send**

### 3.3. Sử dụng cURL (Command Line)

```bash
# Upload video với transcript
curl -X POST http://localhost:8080/api/videos/upload \
  -F "file=@path/to/your/video.mp4" \
  -F "transcribe=true"

# Upload video không có transcript
curl -X POST http://localhost:8080/api/videos/upload \
  -F "file=@path/to/your/video.mp4" \
  -F "transcribe=false"
```

### 3.4. Sử dụng PowerShell

```powershell
# Tạo form data
$filePath = "C:\path\to\your\video.mp4"
$uri = "http://localhost:8080/api/videos/upload?transcribe=true"

# Upload
$form = @{
    file = Get-Item -Path $filePath
}

Invoke-RestMethod -Uri $uri -Method Post -Form $form
```

## Bước 4: Kiểm tra kết quả

### Response thành công sẽ có dạng:
```json
{
  "success": true,
  "videoUrl": "https://res.cloudinary.com/your-cloud/video/upload/v1234567890/swd392-videos/video_name.mp4",
  "publicId": "swd392-videos/video_name",
  "transcript": "Nội dung transcript của video...",
  "duration": 120,
  "format": "mp4",
  "fileSize": 15728640,
  "message": "Video uploaded and transcribed successfully"
}
```

### Các trường trong response:
- **success**: `true` nếu upload thành công
- **videoUrl**: URL để xem/tải video
- **publicId**: ID để quản lý video trên Cloudinary
- **transcript**: Nội dung transcript (nếu `transcribe=true`)
- **duration**: Độ dài video (giây)
- **format**: Định dạng video
- **fileSize**: Kích thước file (bytes)
- **message**: Thông báo kết quả

## Bước 5: Test các API khác

### 5.1. Tạo transcript cho video đã upload
```bash
POST http://localhost:8080/api/videos/transcript
Content-Type: application/json

{
  "videoUrl": "https://res.cloudinary.com/your-cloud/video/upload/v1234567890/swd392-videos/video_name.mp4"
}
```

### 5.2. Xóa video
```bash
DELETE http://localhost:8080/api/videos/{publicId}

# Ví dụ:
DELETE http://localhost:8080/api/videos/swd392-videos/video_name
```

## Troubleshooting

### Lỗi: "Failed to upload video: Unauthorized"
- Kiểm tra lại Cloudinary credentials (cloud-name, api-key, api-secret)
- Đảm bảo không có khoảng trắng thừa

### Lỗi: "File size exceeds maximum limit of 100MB"
- Video quá lớn, chọn video nhỏ hơn 100MB
- Hoặc tăng limit trong `CloudinaryService.java` (line 88)

### Lỗi: "File must be a video"
- Đảm bảo file có định dạng video (.mp4, .avi, .mov, etc.)
- Kiểm tra MIME type của file

### Lỗi kết nối Cloudinary
- Kiểm tra internet connection
- Kiểm tra firewall/proxy settings
- Thử ping cloudinary.com

### Transcript không được tạo
- Kiểm tra Google AI API key trong application.yaml
- Xem log để biết lỗi cụ thể
- Thử upload với `transcribe=false` để test riêng upload

## Tips

1. **Test với video nhỏ trước**: Dùng video 5-10MB để test nhanh
2. **Kiểm tra log**: Xem console log để debug
3. **Verify trên Cloudinary**: Đăng nhập Cloudinary dashboard để xem video đã upload
4. **Lưu publicId**: Cần publicId để xóa video sau này

## Video mẫu để test

Bạn có thể tải video mẫu miễn phí từ:
- https://sample-videos.com/
- https://www.pexels.com/videos/
- Hoặc quay video ngắn bằng điện thoại (30 giây - 1 phút)
