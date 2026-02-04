# 🧪 Example API Response - Japanese Quiz

## API Endpoint
```
GET /api/lessons/{lessonId}/quiz
```

## Complete JSON Response Example

```json
{
  "code": 200,
  "message": "Quiz retrieved successfully",
  "data": {
    "quizId": "a3c5e7f9-1234-5678-90ab-cdef12345678",
    "lessonId": "b1d3f5h7-4321-8765-09ba-fedc98765432",
    "questions": [
      {
        "questionId": "q1a2b3c4-d5e6-f7g8-h9i0-j1k2l3m4n5o6",
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
      {
        "questionId": "q2b3c4d5-e6f7-g8h9-i0j1-k2l3m4n5o6p7",
        "content": "平仮名「あ」の書き順で最初に書く画はどれですか？",
        "options": {
          "A": "横線",
          "B": "縦線",
          "C": "斜め線",
          "D": "曲線"
        },
        "correctAnswer": "A",
        "explanation": "「あ」の最初の画は横線から始まります。正しい書き順を覚えることは、美しい日本語の文字を書くために重要です。"
      },
      {
        "questionId": "q3c4d5e6-f7g8-h9i0-j1k2-l3m4n5o6p7q8",
        "content": "次の敬語表現のうち、謙譲語はどれですか？",
        "options": {
          "A": "いらっしゃる",
          "B": "申し上げる",
          "C": "お越しになる",
          "D": "召し上がる"
        },
        "correctAnswer": "B",
        "explanation": "「申し上げる」は謙譲語で、自分の行為をへりくだって表現する敬語です。他の選択肢は尊敬語に分類されます。"
      },
      {
        "questionId": "q4d5e6f7-g8h9-i0j1-k2l3-m4n5o6p7q8r9",
        "content": "日本の伝統的な文化で「茶道」を表す英語はどれですか？",
        "options": {
          "A": "Ikebana",
          "B": "Tea Ceremony",
          "C": "Calligraphy",
          "D": "Origami"
        },
        "correctAnswer": "B",
        "explanation": "「茶道」は英語で「Tea Ceremony」と表現されます。日本の伝統的な文化の一つで、抹茶を点てて客人に振る舞う儀式です。"
      },
      {
        "questionId": "q5e6f7g8-h9i0-j1k2-l3m4-n5o6p7q8r9s0",
        "content": "「ありがとうございます」の意味として正しいものはどれですか？",
        "options": {
          "A": "すみません",
          "B": "さようなら",
          "C": "感謝を表す表現",
          "D": "謝罪を表す表現"
        },
        "correctAnswer": "C",
        "explanation": "「ありがとうございます」は感謝を表す丁寧な日本語表現です。日常生活やビジネスシーンで頻繁に使用されます。"
      }
    ],
    "createdAt": "2026-02-02T23:30:00.123456",
    "quizStatus": "COMPLETED"
  }
}
```

---

## Question Details Breakdown

### Question 1: Basic Greetings (基本的な挨拶)
**Topic**: Japanese Greetings  
**Difficulty**: Beginner  
**Correct**: A - おはようございます (Ohayou gozaimasu)  
**Translation**: "What is the correct basic Japanese greeting?"

---

### Question 2: Hiragana Stroke Order (書き順)
**Topic**: Writing System  
**Difficulty**: Intermediate  
**Correct**: A - 横線 (Horizontal line)  
**Translation**: "What is the first stroke when writing the hiragana 'あ'?"

---

### Question 3: Humble Keigo (謙譲語)
**Topic**: Japanese Honorifics  
**Difficulty**: Advanced  
**Correct**: B - 申し上げる (Moushiageru)  
**Translation**: "Which of the following honorific expressions is humble keigo?"

---

### Question 4: Traditional Culture (伝統文化)
**Topic**: Japanese Culture  
**Difficulty**: Beginner  
**Correct**: B - Tea Ceremony  
**Translation**: "What is the English word for the traditional Japanese culture '茶道'?"

---

### Question 5: Common Phrases (よく使う表現)
**Topic**: Daily Expressions  
**Difficulty**: Beginner  
**Correct**: C - 感謝を表す表現 (Expression of gratitude)  
**Translation**: "What is the correct meaning of 'ありがとうございます'?"

---

## 🧪 Testing with Postman/Insomnia

### Request Headers
```
GET /api/lessons/b1d3f5h7-4321-8765-09ba-fedc98765432/quiz
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### Expected Status Code
```
200 OK
```

### Response Time
```
< 500ms (mock data, no external API call)
```

---

## 🎯 Validation Checklist

- [x] Response code is 200
- [x] Message is "Quiz retrieved successfully"
- [x] Data contains 5 questions
- [x] Each question has Japanese content
- [x] All questions have 4 options (A-D)
- [x] Correct answer is specified
- [x] Explanation is in Japanese
- [x] QuizId and LessonId are UUIDs
- [x] CreatedAt is ISO 8601 timestamp
- [x] QuizStatus is COMPLETED

---

## 🔍 Character Encoding

**Important**: Ensure your API client supports UTF-8 encoding to properly display Japanese characters.

**Japanese Characters Used**:
- Hiragana: あ, お, は, よ, う, ご, ざ, い, ま, す
- Kanji: 日本語, 基本的, 挨拶, 正しい, 平仮名, 書き順, 敬語, 謙譲語, 伝統的, 文化, 茶道, 感謝, 表現
- Katakana: (not used in this quiz)

---

**Generated**: 2026-02-02  
**Mock Mode**: ✅ Active  
**Language**: 日本語 (Japanese)
