# 🎙️ HƯỚNG DẪN SETUP ASSEMBLYAI - TRANSCRIPT THỰC TẾ

## 📋 Tổng Quan

Bạn đã được cài đặt **AssemblyAI** - service transcription thực tế hỗ trợ trực tiếp Cloudinary URL!

### ✅ Ưu điểm:
- Hỗ trợ video URL trực tiếp từ Cloudinary
- Không cần upload lên Google Cloud
- Setup cực kỳ đơn giản
- Free tier: **5 giờ/tháng**
- Độ chính xác cao

---

## 🚀 SETUP NHANH (5 PHÚT)

### **Bước 1: Đăng Ký AssemblyAI**

1. Truy cập: https://www.assemblyai.com/
2. Click **"Sign Up"** (góc phải trên)
3. Đăng ký bằng email hoặc GitHub
4. Xác nhận email

### **Bước 2: Lấy API Key**

1. Sau khi đăng nhập, vào **Dashboard**
2. Bên trái menu, click **"API Keys"**
3. Copy **API Key** (dạng: `abc123def456...`)

### **Bước 3: Cấu Hình**

Mở file `src/main/resources/application.yaml` và thay đổi dòng cuối:

```yaml
assemblyai:
  api-key: ${ASSEMBLYAI_API_KEY:PASTE_API_KEY_CỦA_BẠN_VÀO_ĐÂY}
```

**Ví dụ:**
```yaml
assemblyai:
  api-key: ${ASSEMBLYAI_API_KEY:abc123def456ghi789jkl012mno345pqr678}
```

### **Bước 4: Restart Server**

```bash
# Dừng server hiện tại (Ctrl+C)
mvn clean compile
mvn spring-boot:run
```

### **Bước 5: Test!**

```json
POST http://localhost:8080/api/videos/transcript

{
  "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v.../video.mp4",
  "language": "vi"
}
```

**Kết quả:** Transcript THỰC TỰ từ video của bạn! 🎉

---

## 📊 Cách Hoạt Động

### Khi KHÔNG có API key:
```
Request → AssemblyAI Service → Phát hiện không có key → Trả về MOCK transcript
```

### Khi CÓ API key:
```
Request → AssemblyAI Service → Gửi video URL → AssemblyAI xử lý → Trả về TRANSCRIPT THỰC
```

---

## 🎯 Các Ngôn Ngữ Hỗ Trợ

AssemblyAI hỗ trợ **50+ ngôn ngữ**, bao gồm:

| Ngôn ngữ | Code | Ngôn ngữ | Code |
|----------|------|----------|------|
| Tiếng Việt | `vi` | Tiếng Anh | `en` |
| Tiếng Nhật | `ja` | Tiếng Hàn | `ko` |
| Tiếng Trung | `zh` | Tiếng Pháp | `fr` |
| Tiếng Đức | `de` | Tiếng Tây Ban Nha | `es` |
| Tiếng Bồ Đào Nha | `pt` | Tiếng Ý | `it` |
| Tiếng Hà Lan | `nl` | Tiếng Ba Lan | `pl` |
| Tiếng Nga | `ru` | Tiếng Thổ Nhĩ Kỳ | `tr` |

---

## 💰 Chi Phí

### Free Tier:
- ✅ **5 giờ transcription/tháng** (miễn phí)
- ✅ Tất cả tính năng
- ✅ Không cần thẻ tín dụng

### Paid Plan (nếu cần):
- $0.00025/giây = $0.015/phút = $0.90/giờ
- Ví dụ: Video 10 phút = $0.15

**Cho dự án học:** Free tier là quá đủ! 🎓

---

## 🧪 Test Cases

### Test 1: Video Tiếng Việt
```json
{
  "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v.../lecture_vi.mp4",
  "language": "vi"
}
```

### Test 2: Video Tiếng Anh
```json
{
  "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v.../tutorial_en.mp4",
  "language": "en"
}
```

### Test 3: Auto-detect
```json
{
  "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v.../mixed.mp4",
  "language": "auto"
}
```

---

## ⏱️ Thời Gian Xử Lý

| Độ dài video | Thời gian xử lý (ước tính) |
|--------------|----------------------------|
| 1 phút | ~15-30 giây |
| 5 phút | ~1-2 phút |
| 10 phút | ~2-3 phút |
| 30 phút | ~5-8 phút |

**Lưu ý:** API sẽ poll mỗi 5 giây để kiểm tra kết quả

---

## 🔍 Kiểm Tra Logs

Khi test, xem logs để biết trạng thái:

```
INFO  - Starting AssemblyAI transcription for URL: https://...
INFO  - Transcription job submitted. ID: abc123
INFO  - Transcription status: processing (attempt 1/60)
INFO  - Transcription status: processing (attempt 2/60)
INFO  - Transcription status: completed (attempt 3/60)
INFO  - Transcription completed. Length: 1234 characters
```

---

## ❗ Troubleshooting

### Lỗi: "AssemblyAI API key not configured"
**Nguyên nhân:** API key chưa được cấu hình hoặc sai

**Giải pháp:**
1. Kiểm tra file `application.yaml`
2. Đảm bảo API key đúng (không có khoảng trắng)
3. Restart server

### Lỗi: "AssemblyAI API error: 401"
**Nguyên nhân:** API key không hợp lệ

**Giải pháp:**
1. Lấy API key mới từ AssemblyAI dashboard
2. Cập nhật vào `application.yaml`
3. Restart server

### Lỗi: "Transcription timeout"
**Nguyên nhân:** Video quá dài (>30 phút)

**Giải pháp:**
1. Tăng `maxAttempts` trong `AssemblyAITranscriptionService.java`
2. Hoặc chia video thành các phần nhỏ hơn

### Transcript trống hoặc sai
**Nguyên nhân:** Video không có âm thanh hoặc chất lượng kém

**Giải pháp:**
1. Kiểm tra video có âm thanh không
2. Đảm bảo chất lượng audio tốt
3. Thử chỉ định `language` cụ thể thay vì `auto`

---

## 📈 Theo Dõi Usage

1. Đăng nhập vào AssemblyAI Dashboard
2. Vào **"Usage"** để xem:
   - Số giờ đã sử dụng
   - Số request
   - Chi phí (nếu có)

---

## 🎓 Best Practices

### 1. Cache Transcript
Lưu transcript vào database sau khi tạo để không phải tạo lại:

```java
// Trong VideoService
if (transcriptExistsInDB(videoUrl)) {
    return getTranscriptFromDB(videoUrl);
} else {
    String transcript = assemblyAIService.transcribeVideo(videoUrl, language);
    saveTranscriptToDB(videoUrl, transcript);
    return transcript;
}
```

### 2. Xử Lý Async
Với video dài, nên xử lý async:

```java
@Async
public CompletableFuture<String> transcribeVideoAsync(String videoUrl, String language) {
    String transcript = assemblyAIService.transcribeVideo(videoUrl, language);
    return CompletableFuture.completedFuture(transcript);
}
```

### 3. Error Handling
Luôn có fallback khi transcription fail:

```java
try {
    return assemblyAIService.transcribeVideo(videoUrl, language);
} catch (Exception e) {
    log.error("Transcription failed", e);
    return "Transcript not available";
}
```

---

## 🔗 Tài Liệu Tham Khảo

- **AssemblyAI Docs:** https://www.assemblyai.com/docs
- **API Reference:** https://www.assemblyai.com/docs/api-reference
- **Language Support:** https://www.assemblyai.com/docs/concepts/supported-languages
- **Pricing:** https://www.assemblyai.com/pricing

---

## ✅ Checklist Setup

- [ ] Đăng ký AssemblyAI account
- [ ] Lấy API key
- [ ] Cập nhật `application.yaml`
- [ ] Restart server
- [ ] Test với video mẫu
- [ ] Verify transcript chính xác
- [ ] Setup database để cache transcript (optional)

---

## 🎉 Kết Luận

Bạn đã có **TRANSCRIPT THỰC TẾ** từ video!

**Không cần:**
- ❌ Upload lên Google Cloud
- ❌ Setup phức tạp
- ❌ Trả tiền (với free tier)

**Chỉ cần:**
- ✅ API key từ AssemblyAI
- ✅ 5 phút setup
- ✅ Enjoy! 🚀

---

**Nếu gặp vấn đề, hãy kiểm tra logs hoặc liên hệ support!**
