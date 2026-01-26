# Hướng Dẫn Chọn Role Khi Đăng Ký

## Tổng Quan

Dự án đã được cập nhật để cho phép người dùng **chọn role** khi đăng ký tài khoản. Thay vì mặc định tất cả người dùng mới đều là **STUDENT**, giờ đây người dùng có thể chọn giữa:

1. **STUDENT** - Học viên
2. **INSTRUCTOR** - Giảng viên

## Các Thay Đổi Đã Thực Hiện

### 1. CreateUserRequest.java
- **Thêm field mới**: `role` (kiểu `User.Role`)
- **Validation**: `@NotNull` - bắt buộc phải chọn role
- **Import**: Thêm `com.minhkhoi.swd392.entity.User`

```java
@NotNull(message = "Role is required")
private User.Role role;
```

### 2. UserService.java
- **Validation logic**: Chỉ cho phép chọn `STUDENT` hoặc `INSTRUCTOR`
- **Không cho phép**: `ADMIN` và `STAFF` (chỉ admin mới có thể tạo)
- **Logging**: Ghi log cả email và role khi tạo user thành công

```java
// Validate role - only allow STUDENT or INSTRUCTOR for registration
if (request.getRole() != User.Role.STUDENT && request.getRole() != User.Role.INSTRUCTOR) {
    throw new AppException(ErrorCode.INVALID_INPUT, 
        "Only STUDENT and INSTRUCTOR roles are allowed for registration");
}
```

### 3. UserMapper.java
- **Xóa ignore mapping**: Cho phép role được map tự động từ request
- **Thêm ignore**: `resetPasswordToken` và `tokenExpirationTime`

## API Usage

### Endpoint: POST `/api/accounts`

**Request Body Mới:**

```json
{
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "password": "password123",
  "otpCode": "123456",
  "role": "STUDENT"
}
```

hoặc

```json
{
  "fullName": "Trần Thị B",
  "email": "tranthib@example.com",
  "password": "password123",
  "otpCode": "654321",
  "role": "INSTRUCTOR"
}
```

### Các Role Hợp Lệ

| Role | Mô Tả | Cho Phép Đăng Ký |
|------|-------|------------------|
| `STUDENT` | Học viên | ✅ Có |
| `INSTRUCTOR` | Giảng viên | ✅ Có |
| `ADMIN` | Quản trị viên | ❌ Không |
| `STAFF` | Nhân viên | ❌ Không |

### Error Responses

**1. Thiếu role:**
```json
{
  "success": false,
  "message": "Role is required",
  "data": null
}
```

**2. Role không hợp lệ:**
```json
{
  "success": false,
  "message": "Only STUDENT and INSTRUCTOR roles are allowed for registration",
  "data": null
}
```

**3. Role là ADMIN hoặc STAFF:**
```json
{
  "success": false,
  "message": "Only STUDENT and INSTRUCTOR roles are allowed for registration",
  "data": null
}
```

## Flow Đăng Ký Mới

```
1. Người dùng nhập email
   ↓
2. Gửi OTP qua email (POST /api/auth/send-otp)
   ↓
3. Người dùng nhập:
   - Full Name
   - Email
   - Password
   - OTP Code
   - **ROLE** (STUDENT hoặc INSTRUCTOR) ← MỚI
   ↓
4. Tạo tài khoản (POST /api/accounts)
   ↓
5. Hệ thống:
   - Validate role (chỉ STUDENT/INSTRUCTOR)
   - Verify OTP
   - Tạo user với role đã chọn
   - Gửi email chào mừng
```

## Testing với Swagger/Postman

### 1. Gửi OTP
```bash
POST http://localhost:8080/api/auth/send-otp
Content-Type: application/json

{
  "email": "test@example.com"
}
```

### 2. Đăng Ký Với Role STUDENT
```bash
POST http://localhost:8080/api/accounts
Content-Type: application/json

{
  "fullName": "Test Student",
  "email": "test@example.com",
  "password": "password123",
  "otpCode": "123456",
  "role": "STUDENT"
}
```

### 3. Đăng Ký Với Role INSTRUCTOR
```bash
POST http://localhost:8080/api/accounts
Content-Type: application/json

{
  "fullName": "Test Instructor",
  "email": "instructor@example.com",
  "password": "password123",
  "otpCode": "654321",
  "role": "INSTRUCTOR"
}
```

## Frontend Integration

### React/Vue/Angular Example

```javascript
const registerUser = async (userData) => {
  // Step 1: Send OTP
  await fetch('/api/auth/send-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: userData.email })
  });

  // Step 2: User enters OTP and selects role
  const registrationData = {
    fullName: userData.fullName,
    email: userData.email,
    password: userData.password,
    otpCode: userData.otpCode,
    role: userData.role // "STUDENT" or "INSTRUCTOR"
  };

  // Step 3: Create account
  const response = await fetch('/api/accounts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(registrationData)
  });

  return response.json();
};
```

### HTML Form Example

```html
<form id="registrationForm">
  <input type="text" name="fullName" placeholder="Full Name" required>
  <input type="email" name="email" placeholder="Email" required>
  <input type="password" name="password" placeholder="Password" required>
  <input type="text" name="otpCode" placeholder="OTP Code" required>
  
  <!-- Role Selection -->
  <label>Select Role:</label>
  <select name="role" required>
    <option value="">-- Choose Role --</option>
    <option value="STUDENT">Student (Học viên)</option>
    <option value="INSTRUCTOR">Instructor (Giảng viên)</option>
  </select>
  
  <button type="submit">Register</button>
</form>
```

## Lưu Ý Quan Trọng

1. **Role là bắt buộc**: Frontend phải gửi field `role` trong request
2. **Chỉ 2 role được phép**: `STUDENT` và `INSTRUCTOR`
3. **Case-sensitive**: Role phải viết hoa đúng format (VD: `STUDENT`, không phải `student`)
4. **Admin/Staff**: Chỉ có thể được tạo bởi admin thông qua endpoint khác hoặc trực tiếp trong database

## Backward Compatibility

⚠️ **Breaking Change**: API này **KHÔNG** tương thích ngược với version cũ vì:
- Field `role` là **required** (`@NotNull`)
- Request cũ không có field `role` sẽ bị reject với lỗi validation

**Migration**: Frontend cần được cập nhật để thêm role selection vào form đăng ký.

## Security Considerations

1. ✅ Validation ở tầng service để đảm bảo chỉ STUDENT/INSTRUCTOR được tạo
2. ✅ Không cho phép tạo ADMIN/STAFF qua public registration endpoint
3. ✅ OTP verification vẫn được giữ nguyên để đảm bảo email hợp lệ
4. ✅ Role được log để audit trail

## Troubleshooting

### Lỗi: "Role is required"
- **Nguyên nhân**: Request không có field `role`
- **Giải pháp**: Thêm field `role` vào request body

### Lỗi: "Only STUDENT and INSTRUCTOR roles are allowed"
- **Nguyên nhân**: Đang cố gắng tạo user với role ADMIN hoặc STAFF
- **Giải pháp**: Chỉ sử dụng `STUDENT` hoặc `INSTRUCTOR`

### Lỗi: Cannot deserialize value
- **Nguyên nhân**: Role value không đúng format (VD: `student` thay vì `STUDENT`)
- **Giải pháp**: Sử dụng uppercase: `STUDENT`, `INSTRUCTOR`

---

**Tác giả**: SWD392 Team-5  
**Ngày cập nhật**: 2026-01-26  
**Version**: 2.0
