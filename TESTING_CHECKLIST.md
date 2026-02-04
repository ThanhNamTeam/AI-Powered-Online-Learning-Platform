# ✅ Testing Checklist - Japanese Quiz Mock Data

## 🎯 Pre-Testing Setup

### Environment Verification
- [ ] Spring Boot application is running
- [ ] Database is connected
- [ ] Port 8080 (or configured port) is available
- [ ] Postman/Insomnia is ready

### User Setup
- [ ] Create INSTRUCTOR user account
- [ ] Verify INSTRUCTOR role is assigned
- [ ] Create PREMIUM subscription (ACTIVE status)
- [ ] Set valid subscription end date

### Course & Module Setup
- [ ] Create a course as INSTRUCTOR
- [ ] Submit course for approval
- [ ] Set course status to APPROVED (Admin action)
- [ ] Create at least 1 module in the course
- [ ] Note the moduleId for testing

---

## 🧪 Testing Flow

### Phase 1: Create Lesson
- [ ] **POST** `/api/lessons/upload`
  - [ ] Add `title` parameter
  - [ ] Add `moduleId` parameter
  - [ ] Upload `videoFile` (any video file)
  - [ ] Upload `documentFile` (optional PDF/DOC)
  - [ ] Verify response code: **201 CREATED**
  - [ ] Save `lessonId` from response

**Expected Response:**
```json
{
  "code": 201,
  "message": "Lesson created and processing started",
  "data": {
    "lessonId": "...",
    "title": "...",
    "quizStatus": "PENDING"
  }
}
```

---

### Phase 2: Generate Quiz
- [ ] **POST** `/api/lessons/{lessonId}/generate-quiz`
  - [ ] Replace `{lessonId}` with actual lesson ID
  - [ ] Add Authorization Bearer token (INSTRUCTOR)
  - [ ] Verify response code: **200 OK**
  - [ ] Verify message: "Quiz generation in progress"

**Expected Response:**
```json
{
  "code": 200,
  "message": "Quiz generation in progress",
  "data": null
}
```

**Check Console Logs:**
- [ ] See `[MOCK MODE] Generating Japanese quiz questions`
- [ ] See `[MOCK MODE] Returning mock Japanese quiz data`
- [ ] See `[MOCK MODE] Successfully generated 5 Japanese quiz questions`

---

### Phase 3: Wait for Processing
- [ ] Wait 3-5 seconds for async processing
- [ ] Check database: Quiz status should be COMPLETED
- [ ] Verify Quiz entity exists in database
- [ ] Verify 5 Question entities exist

---

### Phase 4: Retrieve Quiz
- [ ] **GET** `/api/lessons/{lessonId}/quiz`
  - [ ] Replace `{lessonId}` with actual lesson ID
  - [ ] Add Authorization Bearer token
  - [ ] Verify response code: **200 OK**
  - [ ] Verify message: "Quiz retrieved successfully"

**Expected Response Structure:**
- [ ] Response has `data` object
- [ ] `data.quizId` is valid UUID
- [ ] `data.lessonId` matches request
- [ ] `data.questions` is an array
- [ ] Array has exactly **5 items**

---

## 📋 Question Validation

### Question 1: Basic Greetings
- [ ] `content` contains: "日本語の基本的な挨拶で正しいものはどれですか？"
- [ ] Options A: "おはようございます"
- [ ] Options B: "สวัสดี"
- [ ] Options C: "Hello"
- [ ] Options D: "Bonjour"
- [ ] `correctAnswer` is "A"
- [ ] `explanation` is in Japanese

### Question 2: Hiragana Stroke Order
- [ ] `content` contains: "平仮名「あ」の書き順で最初に書く画はどれですか？"
- [ ] Options A: "横線"
- [ ] Options B: "縦線"
- [ ] Options C: "斜め線"
- [ ] Options D: "曲線"
- [ ] `correctAnswer` is "A"
- [ ] `explanation` is in Japanese

### Question 3: Humble Keigo
- [ ] `content` contains: "次の敬語表現のうち、謙譲語はどれですか？"
- [ ] Options A: "いらっしゃる"
- [ ] Options B: "申し上げる"
- [ ] Options C: "お越しになる"
- [ ] Options D: "召し上がる"
- [ ] `correctAnswer` is "B"
- [ ] `explanation` is in Japanese

### Question 4: Traditional Culture
- [ ] `content` contains: "日本の伝統的な文化で「茶道」を表す英語はどれですか？"
- [ ] Options A: "Ikebana"
- [ ] Options B: "Tea Ceremony"
- [ ] Options C: "Calligraphy"
- [ ] Options D: "Origami"
- [ ] `correctAnswer` is "B"
- [ ] `explanation` is in Japanese

### Question 5: Common Phrases
- [ ] `content` contains: "「ありがとうございます」の意味として正しいものはどれですか？"
- [ ] Options A: "すみません"
- [ ] Options B: "さようなら"
- [ ] Options C: "感謝を表す表現"
- [ ] Options D: "謝罪を表す表現"
- [ ] `correctAnswer` is "C"
- [ ] `explanation` is in Japanese

---

## 🚨 Error Testing

### Test 1: Non-PREMIUM User
- [ ] Create INSTRUCTOR without PREMIUM subscription
- [ ] Try to generate quiz
- [ ] Expected: Error response (subscription required)

### Test 2: Course Not Approved
- [ ] Create course with PENDING status
- [ ] Try to generate quiz for lesson in this course
- [ ] Expected: `COURSE_NOT_APPROVED` error

### Test 3: Quiz Already Exists
- [ ] Generate quiz for a lesson (first time)
- [ ] Try to generate quiz again for same lesson
- [ ] Expected: `QUIZ_ALREADY_EXISTS` error

### Test 4: Quiz Processing
- [ ] Start quiz generation
- [ ] Immediately try to generate again (within 1 second)
- [ ] Expected: `QUIZ_GENERATION_IN_PROGRESS` error

### Test 5: Lesson Not Found
- [ ] Use invalid/non-existent lessonId
- [ ] Try to generate quiz
- [ ] Expected: `LESSON_NOT_FOUND` error

### Test 6: Quiz Not Found
- [ ] Use lessonId without generated quiz
- [ ] Try to GET quiz
- [ ] Expected: `QUIZ_NOT_FOUND` error

---

## 🔍 Database Verification

### Quiz Table
- [ ] Open database client (DBeaver, MySQL Workbench, etc.)
- [ ] Query: `SELECT * FROM quiz WHERE lesson_id = ?`
- [ ] Verify 1 row exists
- [ ] Check `quiz_status` = 'COMPLETED'
- [ ] Check `created_at` timestamp

### Question Table
- [ ] Query: `SELECT * FROM question WHERE quiz_id = ?`
- [ ] Verify exactly 5 rows exist
- [ ] Check all `content` fields have Japanese text
- [ ] Check all `options` are JSON format
- [ ] Check all `correct_answer` values are A, B, C, or D
- [ ] Check all `explanation` fields are not null

---

## 📊 Performance Testing

### Response Time
- [ ] Generate quiz: < 1000ms (async, returns immediately)
- [ ] Get quiz: < 500ms (simple database query)

### Concurrent Requests
- [ ] Generate quiz from 5 different lessons simultaneously
- [ ] Verify all succeed
- [ ] Verify no database conflicts

---

## 🎨 UTF-8 Encoding Test

### Postman/Insomnia
- [ ] Response displays Japanese characters correctly
- [ ] No mojibake (文字化け) / garbled text
- [ ] All kanji, hiragana visible

### Browser DevTools
- [ ] If using frontend, verify Japanese text renders
- [ ] Check Content-Type header: `application/json; charset=UTF-8`

---

## 📝 Documentation Verification

- [ ] Read `MOCK_QUIZ_JAPANESE_DOCUMENTATION.md`
- [ ] Read `QUICK_GUIDE_JAPANESE_QUIZ.md`
- [ ] Read `EXAMPLE_API_RESPONSE.md`
- [ ] Read `SUMMARY_CHANGES.md`
- [ ] Review flowchart image: `japanese_quiz_flow.png`

---

## ✅ Final Sign-Off

### Code Quality
- [ ] No compilation errors
- [ ] No runtime exceptions in logs
- [ ] Clean console output with [MOCK MODE] tags

### Functionality
- [ ] All 5 questions generated correctly
- [ ] All Japanese text displays properly
- [ ] All correct answers are accurate
- [ ] All explanations are meaningful

### Security
- [ ] Only INSTRUCTOR can generate quiz
- [ ] PREMIUM subscription is enforced
- [ ] Course approval is checked

### Performance
- [ ] Async processing works
- [ ] No API call to external Gemini service (mock mode)
- [ ] Fast response times

---

## 🎉 Success Criteria

**ALL checkboxes above should be checked** ✅

If all tests pass:
- ✅ Mock data is working correctly
- ✅ API endpoints are functional
- ✅ Japanese quiz generation is ready for use
- ✅ Ready for production deployment

---

## 📞 Troubleshooting

If tests fail, check:
1. Application logs for errors
2. Database connection
3. User roles and permissions
4. Subscription status and dates
5. Course approval status
6. Character encoding settings

---

**Testing Date**: _________________  
**Tester Name**: _________________  
**Test Environment**: _________________  
**Result**: ⬜ PASS  ⬜ FAIL  

**Notes**:
_______________________________________
_______________________________________
_______________________________________
