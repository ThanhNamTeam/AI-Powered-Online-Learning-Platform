# AI-Powered Online Learning Platform - Full Instructor Workflow

Tài liệu này mô tả chi tiết quy trình từ lúc bắt đầu cho đến khi hoàn thiện nội dung đào tạo của một Giảng viên (Instructor) trên hệ thống.

---

## 1. Khởi tạo tài khoản và Quyền hạn
- **Đăng ký & Chọn Role**: Người dùng đăng ký tài khoản và chọn vai trò là **INSTRUCTOR**. Sau khi xác thực email/OTP thành công, tài khoản sẽ có quyền truy cập vào các tính năng dành riêng cho Giảng viên.
- **Đăng nhập**: Sử dụng tài khoản đã đăng ký để đăng nhập và nhận JWT Token phục vụ cho các yêu cầu API tiếp theo.

---

## 2. Quy trình Quản lý Khóa học (Course Lifecycle)
Để đảm bảo chất lượng nội dung, mỗi khóa học đều trải qua quy trình kiểm soát:
1. **Tạo Khóa học**: Giảng viên tạo thông tin cơ bản cho khóa học (Tên, mô tả, giá, mục tiêu...).
2. **Chờ Phê duyệt (Staff Approval)**: 
    - Khóa học mới tạo sẽ ở trạng thái `PENDING`.
    - **Staff** sẽ kiểm duyệt nội dung. Sau khi được duyệt, trạng thái chuyển sang `PUBLISHED`.
    - Chỉ những khóa học đã được **PUBLISHED** mới có thể tiếp tục xây dựng Module và Lesson.

---

## 3. Xây dựng Cấu trúc Khóa học (Curriculum Building)
Sau khi khóa học được phê duyệt, Giảng viên bắt đầu phân cấp nội dung:
- **Tạo Module**: Phân chia khóa học theo các cấp độ hoặc chủ đề (Ví dụ: Tiếng Nhật cấp độ N5, N4, N3, N2, N1).
- **Tạo Lesson**: Trong mỗi Module, Giảng viên tạo các bài học cụ thể (Ví dụ: "Ngữ pháp N1 - Tuần 1", "Từ vựng chuyên ngành").

---

## 4. Nghiệp vụ bài học (Lesson Details & AI Processing)
Đây là phần trọng tâm xử lý dữ liệu và AI cho từng bài học:

### a. Upload tài nguyên:
- **Video**: Được upload lên Cloudinary. Hệ thống tự động lấy URL và thời lượng. Ngay lập tức, một tiến trình ngầm (**Async**) sẽ gọi `AssemblyAI` để tạo bản **Transcript** (văn bản hóa video).
- **Tài liệu**: Hỗ trợ đính kèm tài liệu học tập. Hệ thống trích xuất nội dung văn bản để làm dữ liệu cho AI.

### b. Tạo Quiz bằng AI (AI Quiz Generation Flow):
Trước khi bắt đầu quá trình tạo Quiz, hệ thống thực hiện các bước kiểm tra nghiêm ngặt:

1. **Kiểm tra bản quyền (Premium Check)**: 
    - Hệ thống kiểm tra xem Giảng viên có sở hữu gói **AI PREMIUM** còn hiệu lực hay không. 
    - Nếu không có gói Premium, hệ thống sẽ từ chối yêu cầu và yêu cầu nâng cấp tài khoản.
2. **Quản lý trạng thái (State Lock)**:
    - Nếu đã có Premium, hệ thống tiếp tục kiểm tra `QuizStatus` để đảm bảo tính ổn định:
        - **NOT_STARTED**: Trạng thái ban đầu, cho phép nhấn nút "Generate Quiz".
        - **PROCESSING**: Khi Giảng viên nhấn nút, hệ thống chuyển sang trạng thái này và "khóa" nút bấm để tránh yêu cầu trùng lặp.
        - **COMPLETED**: AI đã tạo xong bộ câu hỏi. Không cho phép tạo lại trừ khi xóa Quiz cũ.
        - **FAILED**: Nếu có lỗi xảy ra, hệ thống lưu message vào `lastQuizError` và mở khóa để Giảng viên có thể "Thử lại".

### c. Logic Mix dữ liệu (AI Mixing):
Khi tạo Quiz, hệ thống không chỉ dùng video mà còn "trộn" (mix) nội dung từ:
1. **Video Transcript** (Bản dịch từ giọng nói video).
2. **Document Content** (Nội dung trích xuất từ tài liệu đi kèm).
Dữ liệu tổng hợp này được gửi đến **Gemini AI** để tạo ra bộ câu hỏi sát thực tế và bao quát nhất.

---

## 5. Quản lý Tài nguyên và Đồng bộ dữ liệu
- **Xóa Video**: Khi một video bị xóa, bản Transcript liên quan cũng sẽ bị xóa khỏi database để đảm bảo tính toàn vẹn thông tin.
- **Xóa Lesson/Module**: Hệ thống tự động dọn dẹp các tệp vật lý trên Cloudinary và các câu hỏi Quiz liên quan.


---

## 6. Tính năng Thanh toán (Payment)
Hệ thống tích hợp cổng thanh toán **MOMO** cho việc nâng cấp gói tài khoản (AISubscription).
👉 **Xem hướng dẫn chi tiết tại:** [HUONG_DAN_THANH_TOAN.md](HUONG_DAN_THANH_TOAN.md)

---
*Tài liệu này cung cấp cái nhìn tổng quan cho đội ngũ phát triển và người dùng vận hành hệ thống.*
