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
public class JwtInfo {
    private String jwtId;
    private Date expiredTime;
    private Date issuedTime;

}
