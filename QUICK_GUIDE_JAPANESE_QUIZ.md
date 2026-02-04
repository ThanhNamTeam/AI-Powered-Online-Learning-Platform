# 🇯🇵 Mock Quiz Tiếng Nhật - Quick Guide

## ✅ Đã Hoàn Thành

File `GeminiService.java` đã được cập nhật để **mock dữ liệu quiz tiếng Nhật** thay vì gọi Google Gemini API.

## 🎯 Test API Generate Quiz

### 1️⃣ Generate Quiz (POST)
```http
POST /api/lessons/{lessonId}/generate-quiz
Authorization: Bearer {instructor_token}
```

**Requirements:**
- ✅ Role: INSTRUCTOR
- ✅ Premium Subscription: ACTIVE
- ✅ Course Status: APPROVED

**Response:**
```json
{
  "code": 200,
  "message": "Quiz generation in progress",
  "data": null
}
```

---

### 2️⃣ Get Generated Quiz (GET)
```http
GET /api/lessons/{lessonId}/quiz
Authorization: Bearer {token}
```

**Response - 5 Câu Hỏi Tiếng Nhật:**

#### Câu 1: Chào hỏi cơ bản 🙇
```
日本語の基本的な挨拶で正しいものはどれですか？
A. おはようございます ✅
B. สวัสดี
C. Hello
D. Bonjour
```

#### Câu 2: Thứ tự nét viết ✍️
```
平仮名「あ」の書き順で最初に書く画はどれですか？
A. 横線 ✅
B. 縦線
C. 斜め線
D. 曲線
```

#### Câu 3: Kính ngữ khiêm nhường 🎎
```
次の敬語表現のうち、謙譲語はどれですか？
A. いらっしゃる
B. 申し上げる ✅
C. お越しになる
D. 召し上がる
```

#### Câu 4: Văn hóa Nhật Bản 🍵
```
日本の伝統的な文化で「茶道」を表す英語はどれですか？
A. Ikebana
B. Tea Ceremony ✅
C. Calligraphy
D. Origami
```

#### Câu 5: Từ vựng cơ bản 🙏
```
「ありがとうございます」の意味として正しいものはどれですか？
A. すみません
B. さようなら
C. 感謝を表す表現 ✅
D. 謝罪を表す表現
```

---

## 📋 Logs Trong Console

Khi generate quiz, bạn sẽ thấy:
```
[MOCK MODE] Generating Japanese quiz questions for content size: 1234
[MOCK MODE] Returning mock Japanese quiz data instead of calling Gemini API
[MOCK MODE] Successfully generated 5 Japanese quiz questions
```

---

## 📝 Quiz JSON Structure

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
  "explanation": "Giải thích đáp án"
}
```

---

## 🚨 Error Codes

| Code | Message | Giải thích |
|------|---------|-----------|
| `LESSON_NOT_FOUND` | Lesson not found | Không tìm thấy lesson |
| `COURSE_NOT_APPROVED` | Course not approved | Course chưa được approve |
| `QUIZ_ALREADY_EXISTS` | Quiz already exists | Quiz đã tồn tại |
| `QUIZ_GENERATION_IN_PROGRESS` | Quiz generation in progress | Đang generate quiz |
| `QUIZ_NOT_FOUND` | Quiz not found | Không tìm thấy quiz |

---

## 🔄 Async Processing

⏰ **Lưu ý:** Quiz generation chạy **bất đồng bộ**
- POST `/generate-quiz` → Khởi động quá trình
- Đợi vài giây ⏳
- GET `/quiz` → Lấy kết quả

---

## 📚 Tài Liệu Chi Tiết

Xem file: `MOCK_QUIZ_JAPANESE_DOCUMENTATION.md` để biết thêm chi tiết.

---

**✨ Build Status:** ✅ SUCCESS  
**📅 Last Build:** 2026-02-02T23:31:43+07:00  
**🎌 Language:** Japanese (日本語)
