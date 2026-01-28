package com.minhkhoi.swd392.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPayload {
    private String token;
    private String jwtId;
    private Date expiredTime;
}
