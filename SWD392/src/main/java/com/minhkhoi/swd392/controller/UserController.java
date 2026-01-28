package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.request.ChangePasswordRequest;
import com.minhkhoi.swd392.dto.request.UpdateUserInfoRequest;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.service.CloudinaryService;
import com.minhkhoi.swd392.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for authentication and token management")
public class UserController {
    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file){
         String url = cloudinaryService.uploadImage(file);
         userService.updateAvatarUrl(url);
         return ResponseEntity.ok(ApiResponse.success("Upload avatar successfully", url));
    }

    @PutMapping("/update-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateUserInfoRequest request){
        userService.updateUserInfo(request);
        return ResponseEntity.ok(ApiResponse.success("Update profile successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success("Change password successfully", null));
    }
}
