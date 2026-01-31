# MOMO Payment Integration - API Documentation

## Overview
This document describes the MOMO payment integration for purchasing premium subscriptions (BASIC, PREMIUM, ENTERPRISE) for INSTRUCTOR and STUDENT roles.

## Configuration

### MOMO Sandbox Credentials (application.yaml)
```yaml
momo:
  partnerCode: MOMOLRJZ20181206
  accessKey: mTCKt9W3eU1m39TW
  secretKey: SetA5RDnLHvt51AULf51DyauxUo3kDU6
  endpoint: https://test-payment.momo.vn/v2/gateway/api/create
  redirectUrl: http://localhost:5173/checkout/result
  ipnUrl: http://localhost:8080/identity/payment/momo/callback
```

## API Endpoints

### 1. Create Payment
**Endpoint:** `POST /identity/payment/create`

**Authorization:** Required (Bearer Token)

**Roles:** INSTRUCTOR, STUDENT

**Request Body:**
```json
{
  "subscriptionPlan": "PREMIUM",
  "amount": 500000,
  "orderInfo": "Premium Subscription - 1 Month"
}
```

**Request Parameters:**
- `subscriptionPlan` (required): BASIC, PREMIUM, or ENTERPRISE
- `amount` (required): Payment amount in VND (must be positive)
- `orderInfo` (optional): Description of the order

**Response (201 Created):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user-uuid",
  "userEmail": "user@example.com",
  "amount": 500000,
  "status": "PENDING",
  "method": "MOMO",
  "transactionId": "order-uuid",
  "createdAt": "2026-01-31T20:30:00",
  "completedAt": null,
  "notes": "Subscription Plan: PREMIUM | Order Info: Premium Subscription - 1 Month",
  "paymentUrl": "https://test-payment.momo.vn/gw_payment/..."
}
```

**Usage:**
1. Client calls this endpoint to create a payment
2. Backend creates a payment record and calls MOMO API
3. Backend returns payment URL to client
4. Client redirects user to `paymentUrl` to complete payment
5. User completes payment on MOMO page
6. MOMO redirects user back to `redirectUrl` (frontend)
7. MOMO calls IPN callback to notify backend of payment status

---

### 2. MOMO IPN Callback
**Endpoint:** `POST /identity/payment/momo/callback`

**Authorization:** None (called by MOMO)

**Description:** This endpoint is automatically called by MOMO to notify payment status. You don't need to call this manually.

**Request Parameters (from MOMO):**
```
orderId=xxx&requestId=xxx&amount=500000&resultCode=0&message=Success&...
```

**Response:**
```
Success
```

**Payment Status Codes:**
- `resultCode = 0`: Payment successful
- `resultCode != 0`: Payment failed

**What happens on success:**
1. Payment status updated to COMPLETED
2. AI Subscription created with:
   - BASIC: 100 AI credits
   - PREMIUM: 500 AI credits
   - ENTERPRISE: Unlimited credits
3. Subscription valid for 1 month

---

### 3. Get Payment by ID
**Endpoint:** `GET /identity/payment/{paymentId}`

**Authorization:** Required (Bearer Token)

**Roles:** INSTRUCTOR, STUDENT, ADMIN

**Response (200 OK):**
```json
{
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user-uuid",
  "userEmail": "user@example.com",
  "amount": 500000,
  "status": "COMPLETED",
  "method": "MOMO",
  "transactionId": "order-uuid",
  "createdAt": "2026-01-31T20:30:00",
  "completedAt": "2026-01-31T20:35:00",
  "notes": "Subscription Plan: PREMIUM | Order Info: Premium Subscription - 1 Month"
}
```

---

### 4. Get My Payment History
**Endpoint:** `GET /identity/payment/my-payments`

**Authorization:** Required (Bearer Token)

**Roles:** INSTRUCTOR, STUDENT

**Response (200 OK):**
```json
[
  {
    "paymentId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user-uuid",
    "userEmail": "user@example.com",
    "amount": 500000,
    "status": "COMPLETED",
    "method": "MOMO",
    "transactionId": "order-uuid",
    "createdAt": "2026-01-31T20:30:00",
    "completedAt": "2026-01-31T20:35:00",
    "notes": "Subscription Plan: PREMIUM | Order Info: Premium Subscription - 1 Month"
  }
]
```

---

## Subscription Plans

### BASIC
- **Price:** Suggested 100,000 VND/month
- **AI Credits:** 100 credits/month
- **Features:** Basic AI assistance

### PREMIUM
- **Price:** Suggested 500,000 VND/month
- **AI Credits:** 500 credits/month
- **Features:** Advanced AI assistance

### ENTERPRISE
- **Price:** Suggested 2,000,000 VND/month
- **AI Credits:** Unlimited
- **Features:** Full AI capabilities

---

## Payment Flow

```
1. User (INSTRUCTOR/STUDENT) → Frontend
   ↓
2. Frontend → POST /identity/payment/create
   ↓
3. Backend creates Payment record (PENDING)
   ↓
4. Backend → MOMO API (create payment)
   ↓
5. MOMO API → Backend (payment URL)
   ↓
6. Backend → Frontend (payment URL)
   ↓
7. Frontend redirects user to MOMO payment page
   ↓
8. User completes payment on MOMO
   ↓
9. MOMO → Frontend (redirectUrl with result)
   ↓
10. MOMO → Backend IPN callback (payment status)
    ↓
11. Backend updates Payment status to COMPLETED
    ↓
12. Backend creates AISubscription record
    ↓
13. User can now use premium features
```

---

## Testing with MOMO Sandbox

### Test Cards
MOMO provides test accounts for sandbox testing. Use the MOMO sandbox app or test credentials provided by MOMO.

### Test Scenarios

1. **Successful Payment:**
   - Create payment with valid data
   - Complete payment on MOMO sandbox
   - Check payment status becomes COMPLETED
   - Verify AISubscription is created

2. **Failed Payment:**
   - Create payment
   - Cancel payment on MOMO page
   - Check payment status becomes FAILED

3. **Payment History:**
   - Create multiple payments
   - Call GET /identity/payment/my-payments
   - Verify all payments are returned

---

## Error Handling

### Common Errors

**403 Forbidden (ONLY_STUDENT_OR_INSTRUCTOR_CAN_PURCHASE):**
```json
{
  "code": 6002,
  "message": "Only INSTRUCTOR or STUDENT can purchase premium subscriptions"
}
```

**400 Bad Request (INVALID_SUBSCRIPTION_PLAN):**
```json
{
  "code": 6001,
  "message": "Invalid subscription plan. Must be BASIC, PREMIUM, or ENTERPRISE"
}
```

**404 Not Found (PAYMENT_NOT_FOUND):**
```json
{
  "code": 6000,
  "message": "Payment not found"
}
```

**500 Internal Server Error (PAYMENT_CREATION_FAILED):**
```json
{
  "code": 6003,
  "message": "Failed to create payment: [error details]"
}
```

---

## Database Schema

### Payment Table
```sql
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    enrollment_id UUID NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    payment_transaction_id VARCHAR(255) UNIQUE,
    payment_created_at TIMESTAMP NOT NULL,
    payment_completed_at TIMESTAMP,
    payment_notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);
```

### AISubscription Table
```sql
CREATE TABLE ai_subscriptions (
    subscription_id UUID PRIMARY KEY,
    instructor_id VARCHAR(255) NOT NULL,
    subscription_plan VARCHAR(50) NOT NULL,
    subscription_status VARCHAR(50) NOT NULL,
    subscription_price DECIMAL(10,2) NOT NULL,
    subscription_start_date TIMESTAMP NOT NULL,
    subscription_end_date TIMESTAMP NOT NULL,
    subscription_auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    subscription_ai_credits INTEGER,
    subscription_ai_credits_used INTEGER DEFAULT 0,
    subscription_last_renewed_at TIMESTAMP,
    subscription_cancelled_at TIMESTAMP,
    subscription_notes TEXT,
    FOREIGN KEY (instructor_id) REFERENCES users(user_id)
);
```

---

## Security Considerations

1. **Signature Verification:** All MOMO callbacks are verified using HMAC-SHA256 signature
2. **Role-Based Access:** Only INSTRUCTOR and STUDENT can create payments
3. **Transaction Tracking:** Each payment has a unique transaction ID
4. **Idempotency:** Duplicate callbacks are handled safely

---

## Frontend Integration Example

### React/TypeScript Example

```typescript
// Create payment
const createPayment = async (plan: 'BASIC' | 'PREMIUM' | 'ENTERPRISE', amount: number) => {
  const response = await fetch('http://localhost:8080/identity/payment/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
    body: JSON.stringify({
      subscriptionPlan: plan,
      amount: amount,
      orderInfo: `${plan} Subscription - 1 Month`
    })
  });

  const data = await response.json();
  
  // Redirect to MOMO payment page
  window.location.href = data.paymentUrl;
};

// Handle payment result (on redirectUrl page)
const handlePaymentResult = () => {
  const urlParams = new URLSearchParams(window.location.search);
  const resultCode = urlParams.get('resultCode');
  
  if (resultCode === '0') {
    // Payment successful
    alert('Payment successful! Your subscription is now active.');
  } else {
    // Payment failed
    alert('Payment failed. Please try again.');
  }
};
```

---

## Swagger/OpenAPI Documentation

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui/index.html
```

Look for the "Payment" tag to see all payment-related endpoints.

---

## Support

For MOMO API issues, refer to:
- MOMO Developer Documentation: https://developers.momo.vn/
- MOMO Sandbox: https://test-payment.momo.vn/

For application issues, check the logs:
```bash
# View application logs
tail -f logs/application.log
```
