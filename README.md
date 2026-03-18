# 🚀 SWD392 - AI-Powered Online Learning Platform (Backend)

Hệ thống quản lý học tập trực tuyến (LMS) tích hợp trí tuệ nhân tạo (AI) để tối ưu hóa trải nghiệm giảng dạy và học tập. Dự án tập trung vào việc tự động hóa các tác vụ phức tạp của giảng viên thông qua AI.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

### Core Backend
*   **Java 21**: Phiên bản LTS mới nhất hỗ trợ Virtual Threads.
*   **Spring Boot 3.2.5**: Framework chính cho dự án.
*   **Spring Security & JWT**: Quản lý xác thực và phân quyền (Student, Instructor, Admin, Staff).
*   **Spring Data JPA**: Giao tiếp với Database qua Hibernate.

### Database & Caching
*   **PostgreSQL**: Cơ sở dữ liệu quan hệ chính.
*   **pgvector**: Extension của PostgreSQL để lưu trữ và tìm kiếm vector (hỗ trợ các tính năng AI).
*   **Redis**: Caching và quản lý session (hỗ trợ Upstash Redis cho Cloud).

### AI Integrations
*   **Google Gemini AI**: Tự động tạo câu hỏi Quiz từ nội dung bài học, feedback bài làm.
*   **AssemblyAI**: Chuyển đổi video bài giảng thành văn bản (Transcription) không đồng bộ.
*   **Apache POI/PDFBox/Tika**: Trích xuất nội dung từ các file tài liệu (PDF, DOCX).

### Infrastructure & Services
*   **Cloudinary**: Lưu trữ và tối ưu hóa hình ảnh, video (Thumbnails, Lesson Videos).
*   **Brevo (Sendinblue) API**: Gửi email giao dịch (OTP, thông báo) qua HTTP thay cho SMTP truyền thống.
*   **Momo & VNPay**: Tích hợp cổng thanh toán trực tuyến cho các gói Premium.
*   **SpringDoc OpenAPI (Swagger)**: Tự động tạo tài liệu API.

---

## ✨ Các tính năng chính

| Tính năng | Mô tả |
| :--- | :--- |
| **Authentication** | Đăng ký, Đăng nhập JWT, Quên mật khẩu OTP qua Email. |
| **Course Management** | Luồng tạo khóa học (Draft -> Pending Approval -> Approved). |
| **AI Transcription** | Tự động chuyển video bài giảng sang văn bản sau khi giảng viên upload. |
| **AI Quiz Generation** | Giảng viên có thể yêu cầu AI quét transcript và tài liệu để tạo bộ Quiz. |
| **Subscription (Premium)** | Hệ thống gói tài khoản giúp giảng viên sử dụng các tính năng AI nâng cao. |
| **Payment Integration** | Thanh toán nâng cấp tài khoản qua Momo hoặc VNPay. |
| **Interactive Discussion** | Hệ thống Q&A giữa học viên và giảng viên. |

---

## 🚀 Hướng dẫn cài đặt (Local Setup)

### 1. Yêu cầu hệ thống
*   **JDK 21** trở lên.
*   **Maven 3.6+**.
*   **PostgreSQL** (có cài extension `pgvector`).
*   **Redis Server**.

### 2. Cấu hình Biến môi trường
Tạo file `.env` tại thư mục `/SWD392` (hoặc thư mục gốc dự án) với các thông số sau:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/swd392_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your_32_character_secret_key
JWT_EXPIRATION=86400000

# AI Keys
GEMINI_API_KEY=your_gemini_key
ASSEMBLY_API_KEY=your_assemblyai_key

# Services
CLOUDINARY_CLOUD_NAME=your_name
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret

# Payment
MOMO_PARTNER_CODE=...
VNPAY_TMN_CODE=...
```

### 3. Chạy ứng dụng
Mở terminal tại thư mục `SWD392` và chạy:
```bash
./mvnw spring-boot:run
```
Ứng dụng sẽ chạy tại: `http://localhost:8080`

### 4. Tài liệu API (Swagger)
Xem tài liệu API tương tác tại:
*   Swagger UI: `http://localhost:8080/swagger-ui/index.html`
*   OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 🏗️ Cấu trúc thư mục Backend

```text
SWD392/
├── src/main/java/com/minhkhoi/swd392/
│   ├── controller/      # REST Endpoints
│   ├── service/         # Business Logic & AI Integrations
│   ├── entity/          # JPA Hibernate Entities
│   ├── repository/      # Data Access Layers
│   ├── dto/             # Data Transfer Objects & Mappers
│   ├── config/          # Spring & Security Configs
│   └── exception/       # Global Exception Handling
├── src/main/resources/
│   ├── application.yaml # Cấu hình chính (Render/Production)
│   └── application-local.yaml # Cấu hình dev local
└── pom.xml              # Maven dependencies
```

---

## 🧑‍🏫 Quy trình Giảng viên (Instructor Workflow)

1.  **Đăng ký & Xác thực**: Tạo tài khoản và xác thực OTP qua Email.
2.  **Tạo Khóa học**: Thiết lập thông tin (Thumbnail, Tên, Mô tả). Cần tối thiểu **3 Modules** để gửi yêu cầu duyệt.
3.  **Upload Nội dung**: Tải lên Video bài giảng. Hệ thống tự động kích hoạt **Async Transcription** qua AssemblyAI.
4.  **Tạo Quiz bằng AI**: Sau khi khóa học được duyệt, Giảng viên có thể nhấn "Generate Quiz" để Gemini AI tự động soạn thảo câu hỏi.
5.  **Quản lý Doanh thu**: Theo dõi học viên đăng ký và thu nhập qua Dashboard.

---

## 🎓 Quy trình Học viên (Student Workflow)

1.  **Kiểm tra trình độ (Placement Test)**: Thực hiện bài test đầu vào để AI đánh giá năng lực JLPT và gợi ý lộ trình học phù hợp.
2.  **Tìm kiếm & Đăng ký**: Duyệt danh sách khóa học, xem Dashboard học tập và đăng ký khóa học (thanh toán qua VNPay/Momo).
3.  **Học tập chủ động**:
    *   Xem video bài giảng, tải tài nguyên học tập.
    *   Làm Quiz do AI tạo sau mỗi bài học để củng cố kiến thức.
    *   Đặt câu hỏi trong mục thảo luận/Q&A để được Giảng viên giải đáp.
4.  **Theo dõi tiến độ**: Xem thống kê thời gian học, streak học tập và điểm số trung bình qua Student Dashboard.

---

## 🛡️ Quy trình Nhân viên (Staff/Admin Workflow)

1.  **Kiểm duyệt nội dung (Staff)**:
    *   Xem danh sách khóa học `PENDING_APPROVAL`.
    *   Phê duyệt (Approve) để khóa học hiển thị công khai hoặc Từ chối (Reject) kèm lý do để giảng viên điều chỉnh.
2.  **Quản lý hệ thống (Admin)**: Quản trị người dùng, theo dõi doanh thu toàn hệ thống và cấu hình các thông số vận hành.
3.  **Báo cáo & Thống kê**: Theo dõi số lượng học viên mới và hiệu suất kinh doanh theo tuần/tháng.

---

## 📝 Quy trình Kiểm tra trình độ (Placement Test Flow - AI Powered)

Đây là tính năng độc đáo giúp học viên định vị bản thân:
1.  **Lấy đề thi**: Hệ thống lấy ngẫu nhiên 25 câu hỏi (bao gồm Nghe - Listening và Đọc - Reading) phù hợp với nhiều cấp độ.
2.  **Làm bài & Chấm điểm**: Hệ thống tự động tính điểm dựa trên đáp án đã thiết lập.
3.  **AI Phân tích (Gemini)**: 
    *   Dựa trên các câu sai, AI phân tích điểm mạnh/điểm yếu về ngữ pháp, từ vựng hay kỹ năng nghe.
    *   Ước tính trình độ JLPT hiện tại (N5 -> N1).
4.  **Gợi ý lộ trình**: Dựa trên kết quả AI, hệ thống tự động gợi ý danh sách các khóa học phù hợp nhất với trình độ vừa đánh giá.

---
*Dự án được phát triển bởi Group 5 - Lớp SWD392.*
