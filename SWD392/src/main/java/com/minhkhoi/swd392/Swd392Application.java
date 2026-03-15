package com.minhkhoi.swd392;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.TimeZone;
import com.minhkhoi.swd392.config.MomoConfig;
import com.minhkhoi.swd392.config.VnPayConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({MomoConfig.class, VnPayConfig.class})
public class Swd392Application {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

		SpringApplication.run(Swd392Application.class, args);
	}

	@Bean
	public CommandLineRunner fixDatabase(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_status_check");
				jdbcTemplate.execute("ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_type varchar(255)");
				jdbcTemplate.execute("UPDATE payments SET payment_type = 'COURSE' WHERE payment_type IS NULL");
				jdbcTemplate.execute("ALTER TABLE payments ALTER COLUMN payment_type SET NOT NULL");
			} catch (Exception ignored) {
			}
		};
	}
}

