# Mock Dữ Liệu Quiz Tiếng Nhật - GeminiService

## Tổng Quan
File `GeminiService.java` đã được cập nhật để trả về **mock data tiếng Nhật** thay vì gọi Google Gemini API thực tế. Điều này giúp bạn test chức năng generate quiz mà không cần API key hoặc gọi API bên ngoài.

## Thay Đổi Đã Thực Hiện

### 1. Phương Thức `generateQuizQuestions(String content)`
- **Trước đây**: Gọi Google Gemini API để generate quiz
- **Bây giờ**: Trả về dữ liệu mock 5 câu hỏi tiếng Nhật

### 2. Mock Data Tiếng Nhật
Service hiện tại trả về **5 câu hỏi về tiếng Nhật**, bao gồm:

1. **Câu 1**: Về chào hỏi cơ bản trong tiếng Nhật
   - Đáp án đúng: おはようございます (Ohayou gozaimasu - Chào buổi sáng)

2. **Câu 2**: Về thứ tự nét viết chữ Hiragana「あ」
   - Đáp án đúng: Nét ngang (横線)

3. **Câu 3**: Về kính ngữ khiêm nhường (謙譲語)
   - Đáp án đúng: 申し上げる (Moushiageru)

4. **Câu 4**: Về văn hóa truyền thống Nhật Bản - Trà đạo
   - Đáp án đúng: Tea Ceremony

5. **Câu 5**: Về ý nghĩa của "ありがとうございます"
   - Đáp án đúng: Biểu thức cảm ơn (感謝を表す表現)

## Cách Test API

### API Endpoint
```
GET /api/lessons/{lessonId}/quiz
```

### Quy Trình Test

#### Bước 1: Tạo Lesson
Sử dụng API để tạo lesson với video và tài liệu:
```
POST /api/lessons/upload
Content-Type: multipart/form-data

Parameters:
- title: "Japanese Language Basics"
- moduleId: {UUID của module}
- videoFile: (file video)
- documentFile: (file tài liệu - optional)
```

#### Bước 2: Generate Quiz
Gọi API để generate quiz (instructor role required):
```
POST /api/lessons/{lessonId}/generate-quiz
Authorization: Bearer {token}
```

Response:
```json
{
  "code": 200,
  "message": "Quiz generation in progress",
  "data": null
}
```

#### Bước 3: Lấy Quiz Data
Sau khi quiz được generate (quá trình async), gọi API để lấy dữ liệu:
```
GET /api/lessons/{lessonId}/quiz
Authorization: Bearer {token}
```

Expected Response:
```json
{
  "code": 200,
  "message": "Quiz retrieved successfully",
  "data": {
    "quizId": "...",
    "lessonId": "...",
    "questions": [
      {
        "content": "日本語の基本的な挨拶で正しいものはどれですか？",
        "options": {
          "A": "おはようございます",
          "B": "สวัสดี",
          "C": "Hello",
          "D": "Bonjour"
        },
        "correctAnswer": "A",
        "explanation": "「おはようございます」は日本語で朝の挨拶を意味します。丁寧な表現で、ビジネスシーンでもよく使われます。"
      },
      // ... 4 câu còn lại
    ],
    "createdAt": "2026-02-02T23:30:00"
  }
}
```

## Logs Để Debug

Khi gọi API generate quiz, bạn sẽ thấy các log sau:
```
[MOCK MODE] Generating Japanese quiz questions for content size: {size}
[MOCK MODE] Returning mock Japanese quiz data instead of calling Gemini API
[MOCK MODE] Successfully generated 5 Japanese quiz questions
```

## Chú Ý Quan Trọng

### 1. Authentication
- Endpoint generate quiz yêu cầu role **INSTRUCTOR**
- Cần có **PREMIUM subscription** còn hiệu lực

### 2. Course Status
- Course phải ở trạng thái **APPROVED** mới được generate quiz

### 3. Quiz Status
- Không thể generate quiz nếu:
  - `quizStatus = COMPLETED` (đã có quiz)
  - `quizStatus = PROCESSING` (đang generate)

### 4. Async Processing
- Quiz generation chạy **bất đồng bộ** (async)
- Sau khi POST `/generate-quiz`, cần đợi một chút trước khi GET quiz data

## Cấu Trúc Quiz JSON

Mỗi câu hỏi trong mock data có cấu trúc:
```json
{
  "content": "Nội dung câu hỏi",
  "options": {
    "A": "Lựa chọn A",
    "B": "Lựa chọn B",
    "C": "Lựa chọn C",
    "D": "Lựa chọn D"
  },
  "correctAnswer": "A",
  "explanation": "Giải thích tại sao đáp án này đúng"
}
```

## Quay Lại Sử Dụng API Thật

Nếu muốn sử dụng Google Gemini API thật, bạn cần:
1. Restore code cũ trong `GeminiService.java`
2. Cấu hình `gemini.api-key` trong `application.yaml`
3. Đảm bảo có kết nối internet

## Tài Liệu Tham Khảo

### Database Schema
- **Quiz Entity**: Chứa thông tin quiz được generate
- **Question Entity**: Chứa từng câu hỏi riêng lẻ
- **Lesson Entity**: Liên kết với quiz qua relationship

### Related Services
- `LessonAsyncService`: Xử lý async quiz generation
- `QuizMapper`: Convert entity to response DTO
- `QuizRepository`: Truy vấn database

### Error Codes
- `LESSON_NOT_FOUND`: Không tìm thấy lesson
- `COURSE_NOT_APPROVED`: Course chưa được approve
- `QUIZ_ALREADY_EXISTS`: Quiz đã tồn tại
- `QUIZ_GENERATION_IN_PROGRESS`: Quiz đang được generate
- `QUIZ_NOT_FOUND`: Không tìm thấy quiz

---

**Last Updated**: 2026-02-02  
**Author**: AI Assistant  
**Version**: 1.0
