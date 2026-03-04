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
}

