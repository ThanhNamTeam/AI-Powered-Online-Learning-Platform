package com.minhkhoi.swd392.constant;

public enum CourseStatus {
    DRAFT,
    PENDING_APPROVAL,       // Mới gửi lần đầu, chờ Staff duyệt
    APPROVED,               // Đã được duyệt, đang hiển thị công khai
    REJECTED,               // Bị từ chối
    EDITING,                // Đang trong quá trình chỉnh sửa (nội dung cũ vẫn live)
    PENDING_UPDATE,         // Đã sửa xong, chờ Staff duyệt nội dung mới
    PENDING_DELETION,       // Instructor đã yêu cầu xóa, chờ Staff duyệt
    ARCHIVED                // Đã bị ẩn (sau khi Staff duyệt xóa): không cho đăng kí mới,
                            // nhưng student đã enroll vẫn học được
}
