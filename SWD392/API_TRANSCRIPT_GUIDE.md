# 🎙️ API Transcript - Hướng Dẫn Sử Dụng

## 📋 Tổng Quan

API `/api/videos/transcript` sử dụng **Google Gemini AI** (model `gemini-1.5-flash`) để tạo transcript (phiên âm/chuyển đổi giọng nói thành text) từ video.

---

## 🔧 Endpoint

```
POST /api/videos/transcript
```

---

## 📝 Request Body

```json
{
  "videoUrl": "https://res.cloudinary.com/your-cloud/video/upload/v1234567890/swd392-videos/video_name.mp4",
  "language": "vi"
}
```

### Các trường:

| Trường | Bắt buộc | Mô tả | Ví dụ |
|--------|----------|-------|-------|
| `videoUrl` | ✅ Có | URL công khai của video cần tạo transcript | `https://res.cloudinary.com/...` |
| `language` | ❌ Không | Mã ngôn ngữ của video | `"vi"`, `"en"`, `"ja"`, `"auto"` |

### Các mã ngôn ngữ được hỗ trợ:

| Mã | Ngôn ngữ |
|----|----------|
| `vi` | Tiếng Việt |
| `en` | Tiếng Anh |
| `ja` | Tiếng Nhật |
| `ko` | Tiếng Hàn |
| `zh` | Tiếng Trung |
| `fr` | Tiếng Pháp |
| `de` | Tiếng Đức |
| `es` | Tiếng Tây Ban Nha |
| `auto` | Tự động nhận diện (mặc định) |

> 💡 **Lưu ý:** Nếu không điền `language` hoặc điền `"auto"`, AI sẽ tự động nhận diện ngôn ngữ trong video.

---

## ✅ Response Thành Công

```json
{
  "success": true,
  "transcript": "Xin chào các bạn, hôm nay chúng ta sẽ học về...",
  "message": "Transcript generated successfully"
}
```

### Các trường trong response:

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `success` | boolean | `true` nếu thành công |
| `transcript` | string | Nội dung transcript đầy đủ |
| `message` | string | Thông báo kết quả |

---

## ❌ Response Lỗi

```json
{
  "success": false,
  "transcript": null,
  "message": "Failed to generate transcript: API_KEY_INVALID"
}
```

---

## 🧪 Ví Dụ Sử Dụng

### 1. Với Ngôn Ngữ Tiếng Việt

```bash
curl -X POST http://localhost:8080/api/videos/transcript \
  -H "Content-Type: application/json" \
  -d '{
    "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v1234567890/swd392-videos/lecture.mp4",
    "language": "vi"
  }'
```

### 2. Với Ngôn Ngữ Tiếng Anh

```bash
curl -X POST http://localhost:8080/api/videos/transcript \
  -H "Content-Type: application/json" \
  -d '{
    "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v1234567890/swd392-videos/tutorial.mp4",
    "language": "en"
  }'
```

### 3. Tự Động Nhận Diện Ngôn Ngữ

```bash
curl -X POST http://localhost:8080/api/videos/transcript \
  -H "Content-Type: application/json" \
  -d '{
    "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v1234567890/swd392-videos/video.mp4",
    "language": "auto"
  }'
```

### 4. Không Điền Language (Mặc Định Auto)

```bash
curl -X POST http://localhost:8080/api/videos/transcript \
  -H "Content-Type: application/json" \
  -d '{
    "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v1234567890/swd392-videos/video.mp4"
  }'
```

---

## 🔄 Quy Trình Hoạt Động

```
1. Client gửi request với videoUrl và language (optional)
   ↓
2. VideoController nhận request
   ↓
3. VideoService xử lý và gọi AITranscriptionService
   ↓
4. AITranscriptionService:
   - Tạo prompt phù hợp với ngôn ngữ
   - Gọi Google Gemini API
   - Parse response từ AI
   ↓
5. Trả về transcript cho client
```

---

## 🤖 Cách AI Xử Lý

### Khi có `language` được chỉ định:

AI sẽ nhận được prompt như sau:

```
Please transcribe this video accurately. 
The video is in Vietnamese. 
Provide the transcript in Vietnamese. 
Provide the complete transcript of all spoken words in the video. 
Format the transcript with proper punctuation and paragraph breaks. 
If there are multiple speakers, indicate speaker changes. 
Return only the transcript text without any additional commentary.
```

### Khi `language` là `"auto"` hoặc không có:

AI sẽ nhận được prompt:

```
Please transcribe this video accurately. 
Provide the complete transcript of all spoken words in the video. 
Format the transcript with proper punctuation and paragraph breaks. 
If there are multiple speakers, indicate speaker changes. 
Return only the transcript text without any additional commentary.
```

---

## ⚙️ Cấu Hình

### Google AI API Key

Cần cấu hình trong `application.yaml`:

```yaml
google-ai:
  api-key: ${GOOGLE_AI_API_KEY:your-google-ai-api-key}
  model: gemini-1.5-flash
  max-tokens: 8000
```

### Lấy API Key:

1. Truy cập: https://makersuite.google.com/app/apikey
2. Tạo API key mới
3. Copy và paste vào `application.yaml`

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Video URL Phải Công Khai

Video URL phải là **public URL** mà Google AI có thể truy cập được. Cloudinary URL thường đã public sẵn.

### 2. Giới Hạn

- **Độ dài video**: Tùy thuộc vào Google AI API limits
- **Kích thước**: Không giới hạn (vì chỉ gửi URL, không gửi file)
- **Thời gian xử lý**: Tùy video, thường 10-60 giây

### 3. Chi Phí

Google Gemini API có **free tier** nhưng có giới hạn số request/ngày. Kiểm tra tại: https://ai.google.dev/pricing

### 4. Độ Chính Xác

- Tốt nhất với giọng nói rõ ràng
- Có thể kém với:
  - Nhiều tiếng ồn nền
  - Giọng nói không rõ
  - Nhiều người nói cùng lúc

---

## 🐛 Troubleshooting

### Lỗi: "API_KEY_INVALID"

**Nguyên nhân:** Google AI API key không hợp lệ

**Giải pháp:**
1. Kiểm tra API key trong `application.yaml`
2. Tạo API key mới tại https://makersuite.google.com/app/apikey
3. Restart server

### Lỗi: "Failed to transcribe video"

**Nguyên nhân:** Video URL không truy cập được hoặc format không hỗ trợ

**Giải pháp:**
1. Kiểm tra URL có mở được trong browser không
2. Đảm bảo video là format MP4
3. Kiểm tra video không bị private/restricted

### Transcript Trống hoặc Sai

**Nguyên nhân:** Video không có giọng nói hoặc chất lượng kém

**Giải pháp:**
1. Kiểm tra video có âm thanh không
2. Thử chỉ định `language` cụ thể thay vì `"auto"`
3. Sử dụng video chất lượng tốt hơn

---

## 📊 So Sánh Upload vs Transcript

| Tính năng | `/api/videos/upload` | `/api/videos/transcript` |
|-----------|---------------------|-------------------------|
| **Chức năng** | Upload video + tạo transcript | Chỉ tạo transcript |
| **Input** | File video | Video URL |
| **Output** | URL + transcript | Transcript |
| **Sử dụng khi** | Upload video mới | Video đã có URL |
| **Thời gian** | Lâu hơn (upload + AI) | Nhanh hơn (chỉ AI) |

---

## 💡 Tips

1. **Chỉ định ngôn ngữ** cho kết quả chính xác hơn
2. **Sử dụng video chất lượng tốt** (giọng rõ, ít nhiễu)
3. **Kiểm tra API key** trước khi test
4. **Lưu transcript** vào database để không phải tạo lại

---

**Chúc bạn sử dụng thành công! 🎉**
