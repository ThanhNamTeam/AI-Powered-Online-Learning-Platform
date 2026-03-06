//package com.minhkhoi.swd392.service;
//
//import com.minhkhoi.swd392.dto.response.PaymentStatusResponse;
//import com.minhkhoi.swd392.entity.Payment;
//import com.minhkhoi.swd392.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentService {
//    private PaymentRepository paymentRepository;
//
//    public PaymentStatusResponse getPayment(String paymentId){
//
//        Payment payment = paymentRepository.findByPaymentId(UUID.fromString(paymentId));
//        return PaymentStatusResponse.builder()
//                .type(payment.getType())
//                .status(payment.getStatus())
//                .amount(payment.getAmount())
//                .courseId(String.valueOf(payment.getCourse().getCourseId()))
//                .courseName(payment.getCourse().getTitle())
//                .subscriptionPlan()
//                .build();
//    }
//}
