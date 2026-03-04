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

    /**
     * Create a new payment for premium subscription
     * Only INSTRUCTOR and STUDENT can access
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Create payment for premium subscription",
        description = "Creates a MOMO payment for purchasing BASIC, PREMIUM, or ENTERPRISE subscription. Returns payment URL for user to complete payment."
    )
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for subscription plan: {}", request.getSubscriptionPlan());
        PaymentResponse response = momoPaymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/vnpay/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create VNPAY payment")
    public ResponseEntity<PaymentResponse> createVnPayPayment(@Valid @RequestBody CreatePaymentRequest request, HttpServletRequest httpServletRequest) {
        log.info("Creating VNPAY payment for subscription plan: {}", request.getSubscriptionPlan());
        PaymentResponse response = vnPayPaymentService.createPayment(request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * MOMO IPN callback endpoint
     * This endpoint is called by MOMO to notify payment status
     */
    @PostMapping("/momo/callback")
    @Operation(
        summary = "MOMO IPN callback",
        description = "Webhook endpoint for MOMO to notify payment status. This is called automatically by MOMO."
    )
    public ResponseEntity<String> momoCallback(@RequestBody Map<String, Object> params) {
        log.info("Received MOMO callback: {}", params);
        try {
            momoPaymentService.handleMomoCallback(params);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Error handling MOMO callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/vnpay/callback")
    public void vnPayCallback(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        log.info("Received VNPAY callback: {}", params);
        try {
            vnPayPaymentService.handleVnPayCallback(params);
            response.sendRedirect(frontendUrl + "/payment-result?status=success&orderId=" + params.get("vnp_TxnRef"));
        } catch (Exception e) {
            log.error("VNPAY Callback Error", e);
            response.sendRedirect(frontendUrl + "/payment-result?status=success&orderId=" + URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Unknown Error", StandardCharsets.UTF_8));
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get payment details",
        description = "Retrieve payment information by payment ID"
    )
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        log.info("Getting payment with ID: {}", paymentId);
        PaymentResponse response = momoPaymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's payment history
     */
    @GetMapping("/my-payments")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user's payment history",
        description = "Retrieve all payments made by the current authenticated user"
    )
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        log.info("Getting payment history for current user");
        List<PaymentResponse> payments = momoPaymentService.getUserPayments();
        return ResponseEntity.ok(payments);
    }
}
