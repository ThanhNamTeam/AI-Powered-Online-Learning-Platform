package com.minhkhoi.swd392.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhkhoi.swd392.dto.request.CreatePaymentRequest;
import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.entity.AISubscription;
import com.minhkhoi.swd392.service.MomoPaymentService;
import com.minhkhoi.swd392.service.VnPayPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MomoPaymentService momoPaymentService;

    @MockBean
    private VnPayPaymentService vnPayPaymentService;

    // Security mockBeans
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "STUDENT")
    void createMomoPayment_Success() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSubscriptionPlan("PREMIUM");
        request.setAmount(new java.math.BigDecimal("99000"));
        // Assuming user buys PREMIUM sub as a student

        PaymentResponse response = PaymentResponse.builder()
                .paymentId(UUID.randomUUID())
                .paymentUrl("http://momo.vn/pay/1234")
                .status(com.minhkhoi.swd392.entity.Payment.PaymentStatus.PENDING)
                .build();

        when(momoPaymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/identity/payment/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentUrl").value("http://momo.vn/pay/1234"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
