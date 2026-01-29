package com.minhkhoi.swd392.config;

import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data initializer that runs on application startup
 * Creates default admin account if no users exist in the database
 */
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

    /**
     * Initialize default admin account if database is empty
     */
    private void initializeAdminAccount() {
        try {
            // Check if any users exist in the database
            long userCount = userRepository.count();

            if (userCount == 0) {
                log.info("No users found in database. Creating default admin account...");

                // Create default admin user
                User admin = User.builder()
                        .fullName("Administrator")
                        .email("admin@sabo.com")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .role(User.Role.ADMIN)
                        .createdAt(LocalDateTime.now())
                        .enabled(true)
                        .build();

                userRepository.save(admin);

                // Create default staff user
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

                log.info("✅ Default admin and staff accounts created successfully!");
            } else {
                log.info("Users already exist in database. Skipping initialization.");
            }
        } catch (Exception e) {
            log.error("❌ Error initializing admin account: {}", e.getMessage(), e);
        }
    }
}
