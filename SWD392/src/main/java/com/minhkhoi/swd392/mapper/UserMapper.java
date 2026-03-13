package com.minhkhoi.swd392.mapper;

import com.minhkhoi.swd392.dto.request.CreateUserRequest;
import com.minhkhoi.swd392.dto.request.UpdateUserRequest;
import com.minhkhoi.swd392.dto.response.UserResponse;
import com.minhkhoi.swd392.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "createdCourses", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "reportsMade", ignore = true)
    @Mapping(target = "aiSubscriptions", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "tokenExpirationTime", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "birthOfDate", ignore = true)
    @Mapping(target = "estimatedJlptLevel", ignore = true)
    User toUser(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true) // Handled manually for validation
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    @Mapping(target = "createdCourses", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "reportsMade", ignore = true)
    @Mapping(target = "aiSubscriptions", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "tokenExpirationTime", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "birthOfDate", ignore = true)
    @Mapping(target = "estimatedJlptLevel", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
