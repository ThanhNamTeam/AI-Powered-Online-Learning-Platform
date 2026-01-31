# Hướng Dẫn Tích Hợp & Kiểm Thử Thanh Toán MOMO

Tài liệu này hướng dẫn cách cấu hình, chạy và kiểm thử tính năng thanh toán qua cổng MOMO cho việc mua gói Subscription (BASIC, PREMIUM, ENTERPRISE).

---

## 1. Cấu hình

Đảm bảo file `application.yaml` hoặc biến môi trường (`.env`) đã có thông tin cấu hình cho MOMO (Sandbox Environment):

```yaml
momo:
  partnerCode: MOMOLRJZ20181206
  accessKey: mTCKt9W3eU1m39TW
  secretKey: SetA5RDnLHvt51AULf51DyauxUo3kDU6
  endpoint: https://test-payment.momo.vn/v2/gateway/api/create
  redirectUrl: http://localhost:5173/checkout/result  # URL Frontend nhận kết quả
  ipnUrl: http://localhost:8080/identity/payment/momo/callback # URL Backend nhận IPN từ MOMO
```

> **Lưu ý**: Trong môi trường Test Local, MOMO sẽ KHÔNG thể gọi lại `ipnUrl` là `localhost` được (cần deploy hoặc dùng Ngrok). Tuy nhiên, luồng tạo link thanh toán vẫn hoạt động bình thường.

---

## 2. Khởi động Server

Mở terminal và chạy lệnh sau tại thư mục gốc của dự án:

```bash
mvn spring-boot:run
```

Đảm bảo server khởi động thành công và không có lỗi (Started Swd392Application in ... seconds).

---

## 3. Cách Test Nhanh (Khuyên dùng)

Hệ thống đã tích hợp sẵn một trang HTML để kiểm tra quy trình thanh toán một cách trực quan mà không cần cài đặt Frontend.

### Bước 1: Truy cập trang Test
Mở trình duyệt và truy cập:
👉 **[http://localhost:8080/momo-test.html](http://localhost:8080/momo-test.html)**

### Bước 2: Đăng nhập (Lấy Token)
1. Tại phần **Quick Login**, hệ thống đã điền sẵn tài khoản Test (Role: INSTRUCTOR).
   - Email: `instructor@gmail.com`
   - Pass: `Instructor@123`
2. Nhấn nút **"Login & Autofill"**.
3. Nếu thành công, JWT Token sẽ tự động được điền vào ô "Authentication Token".

### Bước 3: Tạo thanh toán
1. Chọn một gói đăng ký (Ví dụ: **PREMIUM Plan** - 500,000 VND).
2. Nhấn nút **"Pay with MOMO"**.
3. Hệ thống sẽ gọi API và chuyển hướng bạn sang trang thanh toán của MOMO.

### Bước 4: Thanh toán trên MOMO (Sandbox)
1. Tại trang MOMO, bạn sẽ thấy mã QR.
2. Tải app **Momo Test** trên điện thoại (hoặc dùng thông tin thẻ test nếu có).
3. Hoặc đơn giản nhất: Nhìn vào URL hoặc giao diện giả lập của MOMO Sandbox để hoàn tất giao dịch.
   - **Lưu ý**: Đối với Sandbox, thường bạn chỉ cần nhấn "Thanh toán" hoặc quét bằng app test là được.


---

## 4. Test bằng Postman & Giả lập Callback (Quan trọng)

⚠️ **Lưu ý quan trọng**: Khi chạy trên `localhost`, server MOMO **KHÔNG THỂ** gọi ngược lại (Callback) vào máy tính của bạn được. Do đó, sau khi bạn quét QR thành công trên MOMO:
1. Giao dịch trên MOMO là **Thành công**.
2. Nhưng Backend của bạn vẫn thấy trạng thái là **PENDING** (do chưa nhận được tín hiệu từ MOMO).

**Giải pháp**: Bạn cần giả lập việc MOMO gọi lại bằng cách dùng Postman hoặc Terminal.

### Cách giả lập Callback (Set trạng thái SUCCESS):
Sau khi có `transactionId` (hoặc `orderId`), bạn gọi API sau để báo cho hệ thống biết là đã thanh toán xong:

**URL**: `POST /identity/payment/momo/callback`
**Params**:
- `orderId`: (Điền transactionId của giao dịch vừa tạo)
- `resultCode`: `0` (0 nghĩa là thành công)
- `message`: `Success`
- `signature`: (Để trống hoặc điền bừa nếu tắt check signature, tuy nhiên code hiện tại có check signature nên bạn cần tắt check trong code hoặc dùng Postman collection đã setup sẵn script tạo signature)

> **Mẹo**: Để test nhanh mà không làm phức tạp, bạn có thể tạm thời comment dòng `verifyMomoSignature` trong code `MomoPaymentService.java` khi chạy local.

---

## 5. Các API chính (Postman Collection)
File Postman Collection đã được tạo tại: `MOMO_Payment_API.postman_collection.json`

---

## 5. Xử lý lỗi thường gặp

| Lỗi (Error Code) | Nguyên nhân | Cách khắc phục |
|------------------|-------------|----------------|
| `Access Denied` (403) | Chưa có Token hoặc Token hết hạn | Nhấn nút Login lại trên trang request để lấy token mới |
| `Connection Refused` | Server chưa chạy | Kiểm tra lại lệnh `mvn spring-boot:run` |
| `Invalid Signature` | Sai Secret Key hoặc dữ liệu bị đổi | Kiểm tra lại `MomoConfig` và `application.yaml` |

---

### Hỗ trợ
Nếu gặp vấn đề về biến môi trường (Environment Variables), hãy đảm bảo file `.env` nằm đúng vị trí và Extension của IDE đã load được nó.
