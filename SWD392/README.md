# AI-Powered Online Learning Platform - Full Instructor Workflow

Tài liệu này mô tả chi tiết quy trình nghiệp vụ từ lúc bắt đầu cho đến khi hoàn thiện nội dung đào tạo của một Giảng viên (Instructor) trên hệ thống, bao gồm các cập nhật mới nhất về tính năng AI và quản lý khóa học.

---

## 1. Khởi tạo tài khoản và Quyền hạn
*   **Đăng ký & Xác thực**: Người dùng đăng ký tài khoản và phải xác thực qua **OTP** (gửi qua Email) để kích hoạt tài khoản.
*   **Vai trò (Role)**: Chọn vai trò là **INSTRUCTOR** trong quá trình đăng ký hoặc thiết lập tài khoản.
*   **Đăng nhập**: Sử dụng JWT Token để thực hiện các thao tác tiếp theo. Hệ thống hỗ trợ Refresh Token qua Cookie để duy trì phiên làm việc.

---

## 2. Quy trình Quản lý Khóa học (Course Lifecycle)
Quy trình kiểm soát chất lượng khóa học nghiêm ngặt hơn với các trạng thái cụ thể:

1.  **Tạo Khóa học (Draft)**:
    *   Giảng viên nhập thông tin cơ bản: Tên, mô tả, giá...
    *   **Bắt buộc**: Phải upload **Thumbnail** (Lưu trữ trên Cloudinary).
    *   Sau khi tạo, khóa học mặc định ở trạng thái `DRAFT`.
2.  **Xây dựng nội dung cơ bản**:
    *   Giảng viên tạo các **Module** và **Lesson**.
    *   **Điều kiện tiên quyết**: Khóa học phải có tối thiểu **3 Modules** mới được phép gửi yêu cầu phê duyệt.
3.  **Gửi yêu cầu Phê duyệt (Pending Approval)**:
    *   Giảng viên nhấn nút "Request Approval". Trạng thái chuyển từ `DRAFT` sang `PENDING_APPROVAL`.
4.  **Kiểm duyệt (Staff Action)**:
    *   **Staff** xem xét nội dung khóa học.
    *   **APPROVED**: Khóa học chính thức hiển thị và cho phép học viên đăng ký.
    *   **REJECTED**: Khóa học bị từ chối kèm theo **Lý do (Reason)**. Giảng viên cần chỉnh sửa để gửi lại yêu cầu.

---

## 3. Quy trình Xử lý Bài học & AI (Lesson & AI Flow)
Đây là phần có sự thay đổi lớn về cách thức vận hành:

### a. Upload tài nguyên & Transcription tự động:
*   **Upload**: Giảng viên upload Video (Bắt buộc) và Tài liệu (Tùy chọn).
*   **Async Transcription**: Ngay sau khi upload thành công, hệ thống tự động kích hoạt một tiến trình chạy ngầm (**Async**) gọi `AssemblyAI` để chuyển đổi giọng nói trong video thành văn bản (**Transcript**).
*   **Lưu trữ**: Bản transcript được lưu trực tiếp vào database của bài học sau khi hoàn tất.

### b. Tạo Quiz bằng AI (Manual Trigger):
Khác với trước đây, việc tạo Quiz hiện tại là một hành động **chủ động** của Giảng viên:

1.  **Nút bấm "Generate Quiz"**: Giảng viên chỉ có thể nhấn nút khi:
    *   Khóa học đã được **APPROVED**.
    *   Trạng thái Quiz hiện tại là `NOT_STARTED` hoặc `FAILED`.
2.  **Kiểm tra đồng bộ (Synchronous Check)**: Trước khi bắt đầu, hệ thống kiểm tra ngay lập tức:
    *   Giảng viên phải sở hữu gói **AI PREMIUM** còn hiệu lực.
3.  **Quản lý trạng thái (State Management)**: Khi bắt đầu tạo:
    *   `PROCESSING`: Khóa nút bấm để tránh yêu cầu trùng lặp.
    *   `COMPLETED`: Tạo xong bộ câu hỏi.
    *   `FAILED`: Lưu lỗi vào `lastQuizError` và cho phép Giảng viên thử lại.

### c. Logic Mix nội dung:
AI (Gemini) sẽ không chỉ dựa vào video mà thực hiện "trộn" dữ liệu:
*   Sử dụng **Video Transcript** (từ AssemblyAI).
*   Sử dụng **Document Content** (nội dung trích xuất từ tài liệu đính kèm).
*   Dữ liệu tổng hợp được gửi đến **Gemini AI** để tạo ra bộ câu hỏi trắc nghiệm sát với nội dung học tập nhất.

---

## 4. Quản lý Tài nguyên và Đồng bộ
*   **Xóa Video/Tài liệu**: Khi xóa tài nguyên trên hệ thống, các tệp vật lý trên Cloudinary sẽ bị xóa theo.
*   **Đồng bộ Transcription**: Nếu Video bị xóa, bản Transcript liên quan trong database cũng sẽ bị xóa sạch để đảm bảo tính nhất quán.
*   **Xóa Bài học**: Tự động dọn dẹp toàn bộ Video, Tài liệu, Transcript và bộ câu hỏi Quiz liên quan.


---

## 6. Tính năng Thanh toán (Payment)
Hệ thống tích hợp cổng thanh toán **MOMO** cho việc nâng cấp gói tài khoản (AISubscription).
👉 **Xem hướng dẫn chi tiết tại:** [HUONG_DAN_THANH_TOAN.md](HUONG_DAN_THANH_TOAN.md)

---
*Tài liệu cập nhật ngày 29/01/2026 bởi Đội ngũ Phát triển.*

