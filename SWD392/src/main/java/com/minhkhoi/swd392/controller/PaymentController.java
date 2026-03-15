package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.CreatePaymentRequest;
import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.service.MomoPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.minhkhoi.swd392.service.VnPayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/identity/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment management APIs for MOMO integration")
public class PaymentController {

    private final MomoPaymentService momoPaymentService;
    private final VnPayPaymentService vnPayPaymentService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create payment for premium subscription",
        description = "Creates a MOMO payment for purchasing BASIC, PREMIUM, or ENTERPRISE subscription. Returns payment URL for user to complete payment."
    )
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = momoPaymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/vnpay/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create VNPAY payment")
    public ResponseEntity<PaymentResponse> createVnPayPayment(@Valid @RequestBody CreatePaymentRequest request, HttpServletRequest httpServletRequest) {
        PaymentResponse response = vnPayPaymentService.createPayment(request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/momo/callback")
    @Operation(
        summary = "MOMO IPN callback",
        description = "Webhook endpoint for MOMO to notify payment status. This is called automatically by MOMO."
    )
    public ResponseEntity<String> momoCallback(@RequestBody Map<String, Object> params) {
        try {
            momoPaymentService.handleMomoCallback(params);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Error handling MOMO callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/momo/return")
    @Operation(
        summary = "MOMO return URL handler",
        description = "Handles MoMo redirect after payment. Updates DB and redirects to frontend with result."
    )
    public void momoReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        String orderId = params.get("orderId");
        String resultCode = params.getOrDefault("resultCode", "-1");
        String message = params.getOrDefault("message", "Unknown error");

        try {
            Map<String, Object> objectParams = new java.util.HashMap<>(params);
            momoPaymentService.handleMomoCallback(objectParams);
        } catch (Exception e) {
            log.error("Error processing MOMO return for orderId {}: {}", orderId, e.getMessage());
        }

        String encodedOrderId = URLEncoder.encode(orderId != null ? orderId : "", StandardCharsets.UTF_8);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        if ("0".equals(resultCode)) {
            response.sendRedirect(frontendUrl + "/payment-result?status=success&orderId=" + encodedOrderId);
        } else {
            response.sendRedirect(frontendUrl + "/payment-result?status=failed&orderId=" + encodedOrderId
                    + "&error=" + encodedMessage + "&resultCode=" + resultCode);
        }
    }

    @GetMapping("/vnpay/callback")
    public void vnPayCallback(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        try {
            vnPayPaymentService.handleVnPayCallback(params);
            response.sendRedirect(frontendUrl + "/payment-result?status=success&orderId=" + params.get("vnp_TxnRef"));
        } catch (Exception e) {
            log.error("VNPAY Callback Error", e);
            response.sendRedirect(frontendUrl + "/payment-result?status=failed&orderId=" + params.getOrDefault("vnp_TxnRef", "")
                    + "&error=" + URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Unknown Error", StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get payment details",
        description = "Retrieve payment information by payment ID"
    )
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        PaymentResponse response = momoPaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user's payment history",
        description = "Retrieve all payments made by the current authenticated user"
    )
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        List<PaymentResponse> payments = momoPaymentService.getUserPayments();
        return ResponseEntity.ok(payments);
    }
}
