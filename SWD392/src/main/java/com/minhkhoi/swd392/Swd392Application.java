package com.minhkhoi.swd392;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

import com.minhkhoi.swd392.config.MomoConfig;
import com.minhkhoi.swd392.config.VnPayConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableAsync
@EnableScheduling  // Bật scheduler cho @Scheduled tasks (VD: cleanup OTP)
@EnableConfigurationProperties({MomoConfig.class, VnPayConfig.class})
@Slf4j
public class Swd392Application {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(Swd392Application.class, args);
	}

	/**
	 * Chạy các migration thủ công cần thiết khi startup.
	 * Dùng IF NOT EXISTS / IF EXISTS để idempotent — an toàn khi restart nhiều lần.
	 */
	@Bean
	public CommandLineRunner fixDatabase(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Xóa constraint cũ nếu còn tồn tại (migration một lần)
				jdbcTemplate.execute(
					"ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_status_check"
				);
				log.info("[DB Migration] Dropped old course_status_check constraint (if existed)");
			} catch (Exception e) {
				log.warn("[DB Migration] Could not drop course_status_check constraint: {}", e.getMessage());
			}

			try {
				// Thêm cột payment_type nếu chưa có
				jdbcTemplate.execute(
					"ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_type varchar(255)"
				);
				jdbcTemplate.execute(
					"UPDATE payments SET payment_type = 'COURSE' WHERE payment_type IS NULL"
				);
				jdbcTemplate.execute(
					"ALTER TABLE payments ALTER COLUMN payment_type SET NOT NULL"
				);
				log.info("[DB Migration] payment_type column ensured on payments table");
			} catch (Exception e) {
				log.warn("[DB Migration] Could not migrate payment_type: {}", e.getMessage());
			}
		};
	}
}
