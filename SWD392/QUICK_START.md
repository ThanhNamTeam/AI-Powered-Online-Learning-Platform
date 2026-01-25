# 🚀 QUICK START - Test Upload Video

## Chỉ 3 Bước Đơn Giản!

### 1️⃣ Cấu hình Cloudinary (1 phút)

Mở `src/main/resources/application.yaml`, tìm dòng 51-54:

```yaml
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME:TÊN_CLOUD_CỦA_BẠN}    # ← Thay đổi ở đây
  api-key: ${CLOUDINARY_API_KEY:API_KEY_CỦA_BẠN}            # ← Thay đổi ở đây
  api-secret: ${CLOUDINARY_API_SECRET:API_SECRET_CỦA_BẠN}  # ← Thay đổi ở đây
  folder: swd392-videos
```

📌 Lấy thông tin từ: https://cloudinary.com/console

---

### 2️⃣ Khởi động Server

```bash
mvn spring-boot:run
```

Đợi thấy: `Started SWD392Application...`

---

### 3️⃣ Test Upload

**Cách dễ nhất:** Mở file `test-video-upload.html` → Kéo thả video → Upload!

**Hoặc dùng PowerShell:**
```powershell
.\test-video-upload.ps1 -VideoPath "path/to/video.mp4"
```

**Hoặc dùng Swagger:**
http://localhost:8080/swagger-ui.html

---

## ✅ Xong!

Nếu cần hướng dẫn chi tiết, xem file: `README_VIDEO_UPLOAD.md`

---

## 🎬 Không có video để test?

**Tải video mẫu:**
- https://sample-videos.com/
- https://www.pexels.com/videos/

**Hoặc tạo video test:**
```powershell
.\create-sample-video.ps1
```

---

## ❓ Gặp lỗi?

1. **Unauthorized** → Kiểm tra Cloudinary credentials
2. **Server không chạy** → Kiểm tra PostgreSQL và database
3. **CORS error** → Restart server (đã có CorsConfig.java)
4. **File quá lớn** → Chọn video < 100MB

Xem thêm: `TEST_VIDEO_UPLOAD.md`
