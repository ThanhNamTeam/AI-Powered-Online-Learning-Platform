package com.minhkhoi.swd392;

import jdk.jfr.Enabled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

import com.minhkhoi.swd392.config.MomoConfig;
import com.minhkhoi.swd392.config.VnPayConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({MomoConfig.class, VnPayConfig.class})
public class Swd392Application {


	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

		SpringApplication.run(Swd392Application.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner fixDatabase(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_status_check");
				System.out.println("✅ Dropped courses_course_status_check constraint");
			} catch (Exception e) {
				System.out.println("⚠️ Could not drop constraint (might not exist): " + e.getMessage());
			}
		};
	}
}

