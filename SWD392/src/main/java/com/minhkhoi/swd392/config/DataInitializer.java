package com.minhkhoi.swd392.config;

import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminAccount();
    }

    private void initializeAdminAccount() {
        try {
            long userCount = userRepository.count();

            if (userCount == 0) {
                User admin = User.builder()
                        .fullName("Administrator")
                        .email("admin@sabo.com")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .role(User.Role.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .enabled(true)
                        .build();

                userRepository.save(admin);

                User staff = User.builder()
                        .fullName("Staff User")
                        .email("staff@swd392.com")
                        .passwordHash(passwordEncoder.encode("Staff@123"))
                        .role(User.Role.STAFF)
                        .createdAt(LocalDateTime.now())
                        .enabled(true)
                        .build();

                userRepository.save(staff);

                User instructor = User.builder()
                        .fullName("Instructor User")
                        .email("instructor@gmail.com")
                        .passwordHash(passwordEncoder.encode("Instructor@123"))
                        .role(User.Role.INSTRUCTOR)
                        .createdAt(LocalDateTime.now())
                        .enabled(true)
                        .build();

                userRepository.save(instructor);
            }
        } catch (Exception e) {
        }
    }
}
