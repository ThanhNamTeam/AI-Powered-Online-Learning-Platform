package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);


    boolean existsByEmail(String email);

    Optional<User> findByUserId(String userId);

    String existsByRole(User.Role role);

    long countByRoleAndCreatedAtAfter(User.Role role, java.time.LocalDateTime date);
}

