# ⚠️ TRANSCRIPT API - GIẢI THÍCH VÀ HƯỚNG DẪN

## 🔴 Vấn Đề Hiện Tại

API `/api/videos/transcript` hiện tại đang sử dụng **MOCK TRANSCRIPT** (transcript giả) vì:

### **Gemini API không hỗ trợ truyền video URL trực tiếp từ Cloudinary**

Gemini API chỉ hỗ trợ:
1. ✅ File đã upload lên **Google File API** (có file URI dạng `gs://...`)
2. ✅ Video data inline (base64 encoded)
3. ❌ **KHÔNG** hỗ trợ URL từ Cloudinary, YouTube, hoặc bất kỳ URL công khai nào

---

## 📝 Hiện Tại API Hoạt Động Như Thế Nào?

### Request:
```json
POST /api/videos/transcript

{
  "videoUrl": "https://res.cloudinary.com/doobcjvfl/video/upload/v.../video.mp4",
  "language": "vi"
}
```

### Response (Mock):
```json
{
  "success": true,
  "transcript": "[MOCK TRANSCRIPT - Tiếng Việt]\n\nXin chào các bạn...",
  "message": "Transcript generated successfully"
}
```

### Transcript sẽ khác nhau tùy theo `language`:
- `"vi"` → Transcript tiếng Việt mẫu
- `"en"` → Transcript tiếng Anh mẫu  
- `"auto"` hoặc không có → Transcript mặc định

---

## 🎯 Để Có Transcript Thực Tế, Bạn Cần:

### **Option 1: Sử dụng Google File API + Gemini** (Phức tạp)

#### Bước 1: Upload video lên Google File API
```java
// 1. Download video từ Cloudinary
byte[] videoBytes = downloadVideo(cloudinaryUrl);

// 2. Upload lên Google File API
String fileUri = uploadToGoogleFileAPI(videoBytes);
// fileUri sẽ có dạng: "gs://bucket-name/file-name"

// 3. Dùng fileUri với Gemini
String transcript = geminiAPI.transcribe(fileUri);
```

#### Bước 2: Cấu hình Google Cloud
- Tạo project trên Google Cloud Console
- Enable File API
- Tạo Service Account và download credentials
- Cấu hình trong code

**Chi phí:** Free tier có giới hạn, sau đó tính phí theo usage

---

### **Option 2: Google Cloud Speech-to-Text API** (Khuyến nghị)

Đây là service chuyên dụng cho speech-to-text, tốt hơn Gemini cho mục đích này.

#### Ưu điểm:
- ✅ Hỗ trợ nhiều ngôn ngữ
- ✅ Độ chính xác cao
- ✅ Hỗ trợ video URL trực tiếp
- ✅ Có nhiều tùy chọn (speaker diarization, punctuation, etc.)

#### Cách sử dụng:

```java
// 1. Add dependency
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-speech</artifactId>
    <version>4.x.x</version>
</dependency>

// 2. Code
SpeechClient speechClient = SpeechClient.create();

RecognitionConfig config = RecognitionConfig.newBuilder()
    .setEncoding(RecognitionConfig.AudioEncoding.MP3)
    .setLanguageCode("vi-VN")
    .build();

RecognitionAudio audio = RecognitionAudio.newBuilder()
    .setUri("gs://bucket/video.mp4")
    .build();

LongRunningRecognizeResponse response = 
    speechClient.longRunningRecognizeAsync(config, audio).get();

String transcript = response.getResultsList()
    .stream()
    .map(result -> result.getAlternativesList().get(0).getTranscript())
    .collect(Collectors.joining("\n"));
```

**Chi phí:** $0.006/15 giây audio (có free tier 60 phút/tháng)

---

### **Option 3: AssemblyAI** (Dễ nhất, Khuyến nghị cho MVP)

Service third-party chuyên về transcription, rất dễ sử dụng.

#### Ưu điểm:
- ✅ API đơn giản
- ✅ Hỗ trợ video URL trực tiếp (kể cả Cloudinary)
- ✅ Không cần Google Cloud setup
- ✅ Có free tier
- ✅ Độ chính xác cao

#### Cách sử dụng:

```java
// 1. Đăng ký tại: https://www.assemblyai.com/
// 2. Lấy API key

// 3. Code
String apiKey = "your-assemblyai-api-key";
String videoUrl = "https://res.cloudinary.com/...";

// Upload video
JSONObject uploadRequest = new JSONObject();
uploadRequest.put("audio_url", videoUrl);
uploadRequest.put("language_code", "vi"); // hoặc "en", "ja", etc.

Request request = new Request.Builder()
    .url("https://api.assemblyai.com/v2/transcript")
    .post(RequestBody.create(uploadRequest.toString(), MediaType.parse("application/json")))
    .addHeader("authorization", apiKey)
    .build();

Response response = httpClient.newCall(request).execute();
JSONObject result = new JSONObject(response.body().string());
String transcriptId = result.getString("id");

// Poll for result
while (true) {
    Request statusRequest = new Request.Builder()
        .url("https://api.assemblyai.com/v2/transcript/" + transcriptId)
        .addHeader("authorization", apiKey)
        .build();
    
    Response statusResponse = httpClient.newCall(statusRequest).execute();
    JSONObject status = new JSONObject(statusResponse.body().string());
    
    if (status.getString("status").equals("completed")) {
        String transcript = status.getString("text");
        break;
    }
    
    Thread.sleep(5000); // Wait 5 seconds
}
```

**Chi phí:** 
- Free tier: 5 hours/month
- Paid: $0.00025/second ($0.015/minute)

---

### **Option 4: Deepgram** (Nhanh nhất)

Service AI transcription với tốc độ xử lý nhanh nhất.

#### Ưu điểm:
- ✅ Tốc độ xử lý cực nhanh
- ✅ Hỗ trợ real-time transcription
- ✅ API đơn giản
- ✅ Hỗ trợ video URL

**Chi phí:** $0.0125/minute (có free trial $200 credit)

---

## 🚀 Khuyến Nghị Cho Dự Án Của Bạn

### **Cho MVP/Demo:**
👉 **AssemblyAI** - Dễ nhất, không cần setup phức tạp

### **Cho Production:**
👉 **Google Cloud Speech-to-Text** - Ổn định, nhiều tính năng

### **Cho Real-time:**
👉 **Deepgram** - Nhanh nhất

---

## 📋 So Sánh Chi Tiết

| Tính năng | Gemini | Google Speech-to-Text | AssemblyAI | Deepgram |
|-----------|--------|----------------------|------------|----------|
| **Hỗ trợ URL trực tiếp** | ❌ | ❌ | ✅ | ✅ |
| **Độ chính xác** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Tốc độ** | Chậm | Trung bình | Nhanh | Rất nhanh |
| **Dễ setup** | Khó | Trung bình | Dễ | Dễ |
| **Free tier** | Có | 60 phút/tháng | 5 giờ/tháng | $200 credit |
| **Giá** | Free (giới hạn) | $0.006/15s | $0.015/phút | $0.0125/phút |
| **Ngôn ngữ** | Nhiều | 125+ | 50+ | 30+ |

---

## 💻 Code Mẫu Tích Hợp AssemblyAI

Mình đã chuẩn bị sẵn code mẫu để tích hợp AssemblyAI. Bạn có thể tham khảo và thay thế mock transcript:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AssemblyAITranscriptionService {
    
    @Value("${assemblyai.api-key}")
    private String apiKey;
    
    private static final String API_URL = "https://api.assemblyai.com/v2";
    private final OkHttpClient httpClient = new OkHttpClient();
    
    public String transcribeVideo(String videoUrl, String language) throws IOException {
        // 1. Submit transcription job
        String transcriptId = submitTranscriptionJob(videoUrl, language);
        
        // 2. Poll for result
        return pollForTranscript(transcriptId);
    }
    
    private String submitTranscriptionJob(String videoUrl, String language) throws IOException {
        JSONObject request = new JSONObject();
        request.put("audio_url", videoUrl);
        request.put("language_code", mapLanguageCode(language));
        
        Request httpRequest = new Request.Builder()
            .url(API_URL + "/transcript")
            .post(RequestBody.create(
                request.toString(),
                MediaType.parse("application/json")
            ))
            .addHeader("authorization", apiKey)
            .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            JSONObject result = new JSONObject(response.body().string());
            return result.getString("id");
        }
    }
    
    private String pollForTranscript(String transcriptId) throws IOException {
        while (true) {
            Request request = new Request.Builder()
                .url(API_URL + "/transcript/" + transcriptId)
                .addHeader("authorization", apiKey)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                JSONObject result = new JSONObject(response.body().string());
                String status = result.getString("status");
                
                if (status.equals("completed")) {
                    return result.getString("text");
                } else if (status.equals("error")) {
                    throw new IOException("Transcription failed: " + result.getString("error"));
                }
                
                Thread.sleep(5000); // Wait 5 seconds
            } catch (InterruptedException e) {
                throw new IOException("Polling interrupted", e);
            }
        }
    }
    
    private String mapLanguageCode(String code) {
        return switch (code.toLowerCase()) {
            case "vi" -> "vi";
            case "en" -> "en";
            case "ja" -> "ja";
            case "ko" -> "ko";
            case "zh" -> "zh";
            default -> "en";
        };
    }
}
```

---

## ✅ Kết Luận

**Hiện tại:** API đang trả về mock transcript để bạn có thể test flow

**Để production:** Chọn một trong các service trên và tích hợp

**Khuyến nghị:** Bắt đầu với **AssemblyAI** vì dễ nhất và có free tier tốt

---

**Nếu cần hỗ trợ tích hợp service thực tế, hãy cho mình biết bạn chọn service nào! 🚀**
