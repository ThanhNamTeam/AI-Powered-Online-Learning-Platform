# AI-Powered-Online-Learning-Platform
Project by Group 5, Class SWD392

## 🎯 Overview
AI-powered online learning platform with automated quiz generation using Google Gemini AI.

---

## 🆕 Latest Update: Japanese Quiz Mock Data (2026-02-02)

### What's New?
✅ **Mock Japanese Quiz Generation** - Implemented mock data for testing quiz generation without requiring Gemini API key.

### Quick Links
- 📘 [Quick Start Guide](./QUICK_GUIDE_JAPANESE_QUIZ.md) - Fast reference for API usage
- 📖 [Full Documentation](./MOCK_QUIZ_JAPANESE_DOCUMENTATION.md) - Complete guide
- 🧪 [Testing Checklist](./TESTING_CHECKLIST.md) - Step-by-step testing guide
- 📄 [Example API Response](./EXAMPLE_API_RESPONSE.md) - Sample JSON responses
- 📋 [Summary of Changes](./SUMMARY_CHANGES.md) - What was modified

### Mock Quiz Content
The system now returns **5 Japanese language quiz questions** covering:
1. 🙇 Basic Greetings (基本的な挨拶)
2. ✍️ Hiragana Stroke Order (書き順)
3. 🎎 Humble Keigo (謙譲語)
4. 🍵 Traditional Culture (伝統文化)
5. 🙏 Common Phrases (よく使う表現)

### API Endpoints

#### Generate Quiz
```http
POST /api/lessons/{lessonId}/generate-quiz
Authorization: Bearer {instructor_token}
```

#### Get Quiz
```http
GET /api/lessons/{lessonId}/quiz
Authorization: Bearer {token}
```

---

## 🏗️ Project Structure

```
AI Platform Onl Cousrse BE/
├── SWD392/                          # Main Spring Boot application
│   ├── src/main/java/
│   │   └── com/minhkhoi/swd392/
│   │       ├── controller/          # REST API controllers
│   │       ├── service/             # Business logic
│   │       │   └── GeminiService.java  # ⭐ Mock quiz generator
│   │       ├── entity/              # JPA entities
│   │       ├── repository/          # Data access layer
│   │       └── dto/                 # Data transfer objects
│   └── pom.xml
├── QUICK_GUIDE_JAPANESE_QUIZ.md     # 🚀 Start here!
├── MOCK_QUIZ_JAPANESE_DOCUMENTATION.md
├── EXAMPLE_API_RESPONSE.md
├── TESTING_CHECKLIST.md
└── README.md                        # This file
```

---

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Database**: MySQL/PostgreSQL
- **Security**: Spring Security + JWT
- **Cloud Storage**: Cloudinary
- **Payment**: VNPay, MoMo
- **AI**: Google Gemini API (with mock mode)
- **Build Tool**: Maven

---

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/ThanhNam0403/AI-Powered-Online-Learning-Platform.git
cd "AI Platform Onl Cousrse BE/SWD392"
```

### 2. Build Project
```bash
mvn clean compile
```

### 3. Run Application
```bash
mvn spring-boot:run
```

### 4. Test Quiz Generation
Follow the [Testing Checklist](./TESTING_CHECKLIST.md)

---

## 📚 Key Features

- ✅ User Management (Student, Instructor, Admin)
- ✅ Course Management with Approval Workflow
- ✅ Module & Lesson Structure
- ✅ Video Upload & Processing
- ✅ AI-Powered Quiz Generation (Mock Mode)
- ✅ Payment Integration (VNPay, MoMo)
- ✅ Premium Subscriptions
- ✅ Enrollment System

---

## 🔐 Authentication & Roles

| Role | Permissions |
|------|-------------|
| **STUDENT** | Enroll courses, take quizzes |
| **INSTRUCTOR** | Create courses, generate quizzes (requires PREMIUM) |
| **ADMIN** | Approve courses, manage users |

---

## 💳 Premium Subscription

**Required for**: Quiz generation (INSTRUCTOR only)
- Status: ACTIVE
- Valid end date
- Payment via VNPay or MoMo

---

## 🧪 Testing

### Automated Tests
```bash
mvn test
```

### Manual Testing
See [TESTING_CHECKLIST.md](./TESTING_CHECKLIST.md)

---

## 📊 Database Schema

Key entities:
- **User** - User accounts and roles
- **Course** - Course information
- **Module** - Course modules
- **Lesson** - Individual lessons with video/document
- **Quiz** - Generated quizzes
- **Question** - Quiz questions
- **Enrollment** - Student course enrollments
- **Payment** - Payment transactions
- **AISubscription** - Premium subscriptions

---

## 🌐 API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

## 🐛 Troubleshooting

### Common Issues

**Issue**: Japanese characters not displaying
- **Solution**: Ensure UTF-8 encoding in database and API client

**Issue**: Quiz generation fails
- **Solution**: Check PREMIUM subscription status and course approval

**Issue**: Build errors
- **Solution**: Ensure Java 21 is installed: `java -version`

---

## 📞 Contact

**Team**: Group 5, SWD392  
**Repository**: [GitHub](https://github.com/ThanhNam0403/AI-Powered-Online-Learning-Platform)

---

## 📝 License

This project is developed for educational purposes at FPT University.

---

**Last Updated**: 2026-02-02  
**Version**: 0.0.1-SNAPSHOT  
**Status**: ✅ Active Development
