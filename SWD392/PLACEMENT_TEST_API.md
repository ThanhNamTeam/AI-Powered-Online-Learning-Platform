# 🎌 Placement Test API – Tài liệu & Dữ liệu Test Swagger

> **Base URL:** `http://localhost:8080`
> **Auth:** ❌ Không cần JWT Token (Public endpoints)
> **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## Mục lục

- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Danh sách API](#danh-sách-api)
- [API 1 – Lấy câu hỏi](#api-1--lấy-câu-hỏi)
- [API 2 – Nộp bài](#api-2--nộp-bài)
- [Dữ liệu giả test Swagger](#-dữ-liệu-giả-test-swagger)
- [Giải thích logic Level](#giải-thích-logic-level)
- [Cấu trúc bảng DB](#cấu-trúc-bảng-db)

---

## Kiến trúc hệ thống

```
[Client / Guest]
     │
     ├── GET  /api/placement-test/questions?count=25
     │         └─► Trả về 25 câu ngẫu nhiên (ẩn correctAnswer)
     │
     └── POST /api/placement-test/submit
               ├─ 1. Chấm điểm (so câu trả lời với DB)
               ├─ 2. Tổng hợp câu sai theo chủ đề
               ├─ 3. Gửi Gemini AI phân tích → JSON
               ├─ 4. Tìm khóa học APPROVED theo JLPT level
               └─► Trả về: Điểm + Nhận xét AI + Khóa học gợi ý
```

---

## Danh sách API

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `GET` | `/api/placement-test/questions` | ❌ Public | Lấy câu hỏi ngẫu nhiên |
| `POST` | `/api/placement-test/submit` | ❌ Public | Nộp bài + nhận kết quả AI |

---

## API 1 – Lấy câu hỏi

### Request

```
GET /api/placement-test/questions?count=25
```

| Query Param | Type | Default | Mô tả |
|-------------|------|---------|-------|
| `count` | `int` | `25` | Số câu hỏi (tối đa 50) |

### Response mẫu

```json
[
  {
    "questionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "content": "次の文の（　）に入る正しい助詞はどれですか？「私（　）学生です。」",
    "options": {
      "A": "が",
      "B": "は",
      "C": "を",
      "D": "に"
    },
    "topic": "Trợ từ",
    "jlptLevel": "N5"
  },
  {
    "questionId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "content": "「電話をかける」の意味はどれですか？",
    "options": {
      "A": "Nghe điện thoại",
      "B": "Gọi điện thoại",
      "C": "Tắt điện thoại",
      "D": "Mua điện thoại"
    },
    "topic": "Từ vựng",
    "jlptLevel": "N4"
  }
]
```

> ⚠️ **Lưu ý:** Response KHÔNG trả về `correctAnswer` và `explanation`.

---

## API 2 – Nộp bài

### Request

```
POST /api/placement-test/submit
Content-Type: application/json
```

### Response mẫu

```json
{
  "correctCount": 18,
  "totalQuestions": 25,
  "scorePercent": 72.0,
  "estimatedLevel": "N2",
  "overallComment": "Bạn có nền tảng tiếng Nhật khá tốt ở mức N3-N2. Điểm mạnh là ngữ pháp cơ bản và từ vựng thông dụng, tuy nhiên cần củng cố thêm về thành ngữ và chữ Hán nâng cao.",
  "strengths": [
    "Nắm vững ngữ pháp cơ bản N4-N3",
    "Từ vựng thông dụng phong phú",
    "Hiểu tốt các mẫu câu trang trọng"
  ],
  "weaknesses": [
    "Còn yếu về Hán tự cấp độ N2",
    "Cần cải thiện thành ngữ và cố định ngữ",
    "Chưa nắm chắc văn phong trang trọng"
  ],
  "wrongAnswers": [
    {
      "questionNumber": 3,
      "questionContent": "「これ・それ・あれ」の使い分けで、話し手と聞き手の「両方から遠い」ものを指すのはどれですか？",
      "topic": "Chỉ thị từ",
      "jlptLevel": "N5",
      "yourAnswer": "B",
      "correctAnswer": "C",
      "explanation": "「あれ」は話し手からも聞き手からも遠いものを指します。"
    },
    {
      "questionNumber": 9,
      "questionContent": "「先生に作文を直していただきました」の意味はどれですか？",
      "topic": "Kính ngữ",
      "jlptLevel": "N4",
      "yourAnswer": "A",
      "correctAnswer": "C",
      "explanation": "「〜ていただく」は「〜てもらう」の謙譲語で、話し手が恩恵を受けることを表します。"
    }
  ],
  "studyRecommendation": "Bạn đang ở ngưỡng N3-N2. Hãy tập trung ôn luyện Hán tự N2 (khoảng 1000 chữ) và thành ngữ tiếng Nhật. Nên đăng ký khóa học N2 để có lộ trình học rõ ràng và thi JLPT N2 trong 6-9 tháng tới.",
  "suggestedCourses": [
    {
      "courseId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "title": "Luyện thi JLPT N2 – Toàn diện",
      "description": "Khóa học chuẩn bị thi JLPT N2 với đầy đủ ngữ pháp, từ vựng, đọc hiểu và nghe.",
      "thumbnailUrl": "https://example.com/n2-course.jpg",
      "price": 1299000,
      "level": "N2"
    },
    {
      "courseId": "d4e5f6a7-b8c9-0123-defa-234567890123",
      "title": "Hán Tự N2 – Học theo chủ đề",
      "description": "Hệ thống hóa 1000 chữ Hán N2 theo nhóm chủ đề giúp ghi nhớ bền vững.",
      "thumbnailUrl": "https://example.com/kanji-n2.jpg",
      "price": 699000,
      "level": "N2"
    }
  ]
}
```

---

## 🧪 Dữ liệu giả test Swagger

> Copy các JSON body bên dưới và paste trực tiếp vào Swagger UI.

---

### Test Case 1 – Học viên giỏi (kỳ vọng N1/N2)

**Mô tả:** Trả lời đúng hầu hết câu, chỉ sai 1-2 câu N2.

```json
{
  "answers": [
    { "questionId": "REPLACE_WITH_REAL_ID_1",  "selectedAnswer": "A" },
    { "questionId": "REPLACE_WITH_REAL_ID_2",  "selectedAnswer": "A" },
    { "questionId": "REPLACE_WITH_REAL_ID_3",  "selectedAnswer": "C" },
    { "questionId": "REPLACE_WITH_REAL_ID_4",  "selectedAnswer": "B" },
    { "questionId": "REPLACE_WITH_REAL_ID_5",  "selectedAnswer": "C" },
    { "questionId": "REPLACE_WITH_REAL_ID_6",  "selectedAnswer": "A" },
    { "questionId": "REPLACE_WITH_REAL_ID_7",  "selectedAnswer": "A" },
    { "questionId": "REPLACE_WITH_REAL_ID_8",  "selectedAnswer": "B" },
    { "questionId": "REPLACE_WITH_REAL_ID_9",  "selectedAnswer": "C" },
    { "questionId": "REPLACE_WITH_REAL_ID_10", "selectedAnswer": "B" }
  ]
}
```

---

### Test Case 2 – Học viên trung bình (kỳ vọng N3)

**Mô tả:** Dùng sau khi gọi `GET /questions`. Lấy `questionId` từ response rồi điền vào đây.

```json
{
  "answers": [
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_1",  "selectedAnswer": "B" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_2",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_3",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_4",  "selectedAnswer": "B" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_5",  "selectedAnswer": "C" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_6",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_7",  "selectedAnswer": "D" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_8",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_9",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_10", "selectedAnswer": "C" }
  ]
}
```

---

### Test Case 3 – Học viên mới bắt đầu (kỳ vọng N5)

**Mô tả:** Trả lời sai nhiều câu hoặc bỏ qua (null).

```json
{
  "answers": [
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_1",  "selectedAnswer": "C" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_2",  "selectedAnswer": "D" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_3",  "selectedAnswer": "A" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_4",  "selectedAnswer": "D" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_5",  "selectedAnswer": null },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_6",  "selectedAnswer": "C" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_7",  "selectedAnswer": null },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_8",  "selectedAnswer": "C" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_9",  "selectedAnswer": "B" },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_10", "selectedAnswer": "D" }
  ]
}
```

---

### Test Case 4 – Chỉ gửi 1 câu (edge case)

```json
{
  "answers": [
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_1", "selectedAnswer": "A" }
  ]
}
```

---

### Test Case 5 – Bỏ trống tất cả (edge case – null answers)

```json
{
  "answers": [
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_1", "selectedAnswer": null },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_2", "selectedAnswer": null },
    { "questionId": "QUESTION_ID_TỪ_RESPONSE_3", "selectedAnswer": null }
  ]
}
```

---

## 📋 Hướng dẫn test với Swagger từng bước

### Bước 1 – Lấy UUID câu hỏi

1. Mở Swagger: `http://localhost:8080/swagger-ui/index.html`
2. Tìm tag **"Placement Test"**
3. Mở **`GET /api/placement-test/questions`**
4. Nhấn **"Try it out"** → **"Execute"**
5. Copy danh sách `questionId` từ response

### Bước 2 – Nộp bài

1. Mở **`POST /api/placement-test/submit`**
2. Nhấn **"Try it out"**
3. Paste JSON body bên dưới vào ô **Request body**
4. **Thay `QUESTION_ID_TỪ_RESPONSE_X`** bằng UUID thực từ Bước 1
5. Nhấn **"Execute"**
6. Xem kết quả trong **Response body**

### Ví dụ JSON body hoàn chỉnh sau khi thay ID

Giả sử Bước 1 trả về các ID sau:
```
Câu 1: "11111111-1111-1111-1111-111111111111"
Câu 2: "22222222-2222-2222-2222-222222222222"
Câu 3: "33333333-3333-3333-3333-333333333333"
Câu 4: "44444444-4444-4444-4444-444444444444"
Câu 5: "55555555-5555-5555-5555-555555555555"
```

Thì body submit sẽ là:

```json
{
  "answers": [
    { "questionId": "11111111-1111-1111-1111-111111111111", "selectedAnswer": "B" },
    { "questionId": "22222222-2222-2222-2222-222222222222", "selectedAnswer": "A" },
    { "questionId": "33333333-3333-3333-3333-333333333333", "selectedAnswer": "C" },
    { "questionId": "44444444-4444-4444-4444-444444444444", "selectedAnswer": "B" },
    { "questionId": "55555555-5555-5555-5555-555555555555", "selectedAnswer": "A" }
  ]
}
```

---

## Giải thích logic Level

| Điểm % | Level ước tính |
|--------|---------------|
| `< 40%` | **N5** – Sơ cấp |
| `40% – 54%` | **N4** – Sơ cấp nâng cao |
| `55% – 69%` | **N3** – Trung cấp |
| `70% – 84%` | **N2** – Cao cấp |
| `≥ 85%` | **N1** – Thành thạo |

> **Lưu ý:** Đây là fallback khi Gemini AI không phản hồi. Khi AI hoạt động, level được phân tích chi tiết hơn dựa trên loại câu sai.

---

## Cấu trúc bảng DB

### `placement_questions`

| Column | Type | Mô tả |
|--------|------|-------|
| `placement_question_id` | `UUID` | Primary Key |
| `content` | `TEXT` | Nội dung câu hỏi |
| `options` | `JSONB` | `{"A":"...","B":"...","C":"...","D":"..."}` |
| `correct_answer` | `VARCHAR(1)` | A, B, C hoặc D |
| `explanation` | `TEXT` | Giải thích đáp án |
| `topic` | `VARCHAR(100)` | Chủ đề (Trợ từ, Hán tự, ...) |
| `jlpt_level` | `VARCHAR(2)` | N5, N4, N3, N2, N1 |
| `source` | `VARCHAR(200)` | Nguồn tài liệu |

### `courses` (field mới thêm)

| Column | Type | Mô tả |
|--------|------|-------|
| `course_jlpt_level` | `VARCHAR(2)` | N5, N4, N3, N2, N1 – Cấp độ của khóa học |

---

## Phân bổ câu hỏi seed data

| Level | Số câu | Chủ đề bao gồm |
|-------|--------|----------------|
| **N5** | 6 câu | Hội thoại, Chỉ thị từ, Trợ từ, Hán tự, Động từ |
| **N4** | 6 câu | ために vs ように, Từ vựng, Kính ngữ (いただく), Suy đoán |
| **N3** | 6 câu | Liên kết câu (にもかかわらず), Phó từ, Dạng て, Tần suất, Hán tự |
| **N2** | 7 câu | Từ vựng nâng cao, Văn phong trang trọng, Câu bị động, Thành ngữ |
| **Tổng** | **25 câu** | |
