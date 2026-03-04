package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.response.PaymentResponse;
import com.minhkhoi.swd392.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "paymentUrl", ignore = true) // Set manually if available
    PaymentResponse toPaymentResponse(Payment payment);
}
