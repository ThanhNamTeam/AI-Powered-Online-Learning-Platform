package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private User.Role role;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String address;
    private String phoneNumber;
    private String gender;
    private LocalDate birthOfDate;
    private String notes;


    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .userId(UUID.fromString(user.getUserId()))
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .imageUrl(user.getImageUrl())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthOfDate(user.getBirthOfDate())
                .notes(user.getNotes())
                .build();
    }
}

