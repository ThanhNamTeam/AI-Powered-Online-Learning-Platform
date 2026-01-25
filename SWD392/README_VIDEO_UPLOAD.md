# 🎥 Hướng Dẫn Test Chức Năng Upload Video

## 📋 Tổng Quan

Bạn đã có đầy đủ code để upload video lên Cloudinary và tạo transcript tự động. Mình đã tạo sẵn 3 công cụ test cho bạn:

1. **test-video-upload.html** - Giao diện web đẹp, dễ dùng nhất ⭐ (Khuyến nghị)
2. **test-video-upload.ps1** - Script PowerShell tự động
3. **Swagger UI** - API documentation tích hợp sẵn

---

## ⚙️ Bước 1: Cấu Hình Cloudinary

### Lấy thông tin từ Cloudinary:
1. Truy cập: https://cloudinary.com/console
2. Đăng nhập (hoặc tạo tài khoản miễn phí)
3. Copy 3 thông tin sau từ Dashboard:
   - **Cloud Name**
   - **API Key**
   - **API Secret**

### Cập nhật file `application.yaml`:

Mở file: `src/main/resources/application.yaml`

Tìm dòng 51-54 và thay thế:

```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME:TÊN_CLOUD_CỦA_BẠN}
  api-key: ${CLOUDINARY_API_KEY:API_KEY_CỦA_BẠN}
  api-secret: ${CLOUDINARY_API_SECRET:API_SECRET_CỦA_BẠN}
  folder: swd392-videos
```

**Ví dụ:**
```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME:demo-cloud}
  api-key: ${CLOUDINARY_API_KEY:123456789012345}
  api-secret: ${CLOUDINARY_API_SECRET:abcdefghijklmnopqrstuvwxyz123}
  folder: swd392-videos
```

> 💡 **Lưu ý:** Thay `TÊN_CLOUD_CỦA_BẠN`, `API_KEY_CỦA_BẠN`, `API_SECRET_CỦA_BẠN` bằng thông tin thực của bạn

---

## 🚀 Bước 2: Khởi Động Server

Mở terminal và chạy:

```bash
mvn spring-boot:run
```

Đợi cho đến khi thấy log:
```
Started SWD392Application in X.XXX seconds
```

Server sẽ chạy tại: http://localhost:8080

---

## 🎯 Bước 3: Test Upload Video

### Cách 1: Sử dụng HTML Page (Dễ nhất) ⭐

1. Mở file `test-video-upload.html` bằng trình duyệt (double-click)
2. Kéo thả video vào khung upload (hoặc click để chọn)
3. Chọn có muốn tạo transcript hay không
4. Click "Upload Video"
5. Đợi kết quả!

**Ưu điểm:**
- ✅ Giao diện đẹp, trực quan
- ✅ Drag & drop
- ✅ Hiển thị progress bar
- ✅ Xem kết quả ngay trên trang

### Cách 2: Sử dụng PowerShell Script

```powershell
# Upload với transcript
.\test-video-upload.ps1 -VideoPath "C:\path\to\video.mp4" -Transcribe $true

# Upload không có transcript
.\test-video-upload.ps1 -VideoPath "C:\path\to\video.mp4" -Transcribe $false
```

**Ưu điểm:**
- ✅ Tự động hóa
- ✅ Hiển thị chi tiết
- ✅ Lưu kết quả vào file JSON

### Cách 3: Sử dụng Swagger UI

1. Mở trình duyệt: http://localhost:8080/swagger-ui.html
2. Tìm section **"Video Management"**
3. Click **POST /api/videos/upload**
4. Click **"Try it out"**
5. Chọn file và click **"Execute"**

**Ưu điểm:**
- ✅ Tích hợp sẵn
- ✅ Test tất cả API
- ✅ Xem request/response

### Cách 4: Sử dụng cURL

```bash
curl -X POST http://localhost:8080/api/videos/upload \
  -F "file=@path/to/video.mp4" \
  -F "transcribe=true"
```

---

## 📊 Kết Quả Mong Đợi

### Response thành công:

```json
{
  "success": true,
  "videoUrl": "https://res.cloudinary.com/your-cloud/video/upload/v1234567890/swd392-videos/video_name.mp4",
  "publicId": "swd392-videos/video_name",
  "transcript": "Đây là nội dung transcript của video...",
  "duration": 120,
  "format": "mp4",
  "fileSize": 15728640,
  "message": "Video uploaded and transcribed successfully"
}
```

### Giải thích các trường:

| Trường | Mô tả |
|--------|-------|
| `success` | `true` nếu upload thành công |
| `videoUrl` | URL để xem/tải video |
| `publicId` | ID để quản lý video (dùng khi xóa) |
| `transcript` | Nội dung transcript (nếu có) |
| `duration` | Độ dài video (giây) |
| `format` | Định dạng video |
| `fileSize` | Kích thước file (bytes) |
| `message` | Thông báo kết quả |

---

## 🔧 Các API Khác

### 1. Tạo Transcript cho Video Đã Upload

```bash
POST http://localhost:8080/api/videos/transcript
Content-Type: application/json

{
  "videoUrl": "https://res.cloudinary.com/your-cloud/video/upload/v1234567890/swd392-videos/video_name.mp4"
}
```

### 2. Xóa Video

```bash
DELETE http://localhost:8080/api/videos/{publicId}

# Ví dụ:
DELETE http://localhost:8080/api/videos/swd392-videos/video_name
```

---

## ❗ Troubleshooting

### Lỗi: "Failed to upload video: Unauthorized"

**Nguyên nhân:** Cloudinary credentials sai

**Giải pháp:**
1. Kiểm tra lại cloud-name, api-key, api-secret
2. Đảm bảo không có khoảng trắng thừa
3. Restart server sau khi sửa

### Lỗi: "File size exceeds maximum limit of 100MB"

**Nguyên nhân:** Video quá lớn

**Giải pháp:**
1. Chọn video nhỏ hơn 100MB
2. Hoặc tăng limit trong `CloudinaryService.java` (line 88)

### Lỗi: "File must be a video"

**Nguyên nhân:** File không phải video

**Giải pháp:**
1. Đảm bảo file có định dạng .mp4, .avi, .mov, etc.
2. Kiểm tra MIME type của file

### Lỗi: CORS khi dùng HTML page

**Nguyên nhân:** CORS chưa được cấu hình

**Giải pháp:**
- Đã tạo sẵn `CorsConfig.java`, restart server là được

### Server không khởi động

**Giải pháp:**
1. Kiểm tra PostgreSQL đã chạy chưa
2. Kiểm tra database `swd392_db` đã tạo chưa
3. Kiểm tra port 8080 có bị chiếm không

### Transcript không được tạo

**Nguyên nhân:** Google AI API key chưa cấu hình

**Giải pháp:**
1. Kiểm tra `google-ai.api-key` trong `application.yaml`
2. Hoặc upload với `transcribe=false` để test riêng upload

---

## 📝 Tips

1. **Test với video nhỏ trước** (5-10MB) để nhanh
2. **Kiểm tra log** trong console để debug
3. **Verify trên Cloudinary Dashboard** để xem video đã upload
4. **Lưu publicId** để xóa video sau này
5. **Dùng HTML page** cho trải nghiệm tốt nhất

---

## 🎬 Video Mẫu Để Test

Tải video mẫu miễn phí từ:
- https://sample-videos.com/
- https://www.pexels.com/videos/
- Hoặc quay video ngắn bằng điện thoại (30 giây - 1 phút)

---

## 📂 Cấu Trúc Files

```
SWD392/
├── src/main/java/com/minhkhoi/swd392/
│   ├── config/
│   │   ├── CloudinaryConfig.java      # Cấu hình Cloudinary
│   │   └── CorsConfig.java            # Cấu hình CORS (MỚI)
│   ├── controller/
│   │   └── VideoController.java       # API endpoints
│   ├── service/
│   │   ├── CloudinaryService.java     # Upload/delete video
│   │   ├── VideoService.java          # Orchestration
│   │   └── AITranscriptionService.java # Tạo transcript
│   └── dto/
│       └── VideoUploadResponse.java   # Response model
├── src/main/resources/
│   └── application.yaml               # Cấu hình app
├── test-video-upload.html             # HTML test page (MỚI)
├── test-video-upload.ps1              # PowerShell script (MỚI)
└── TEST_VIDEO_UPLOAD.md               # Hướng dẫn chi tiết (MỚI)
```

---

## 🎉 Bắt Đầu Ngay!

1. ✅ Cấu hình Cloudinary credentials
2. ✅ Khởi động server: `mvn spring-boot:run`
3. ✅ Mở `test-video-upload.html` và upload video
4. ✅ Enjoy! 🚀

---

**Chúc bạn test thành công! 🎊**

Nếu gặp vấn đề gì, hãy kiểm tra:
- Console log của server
- Browser console (F12)
- File `TEST_VIDEO_UPLOAD.md` để biết thêm chi tiết
