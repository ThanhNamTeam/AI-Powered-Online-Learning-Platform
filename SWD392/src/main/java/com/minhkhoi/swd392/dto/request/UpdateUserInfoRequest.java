package com.minhkhoi.swd392.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserInfoRequest {
    private String fullName;
    private String notes;
    private String phoneNumber;
    private String address;
    private String gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthOfDate;}
