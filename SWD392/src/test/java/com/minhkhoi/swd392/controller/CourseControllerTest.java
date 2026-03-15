package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.constant.CourseStatus;
import com.minhkhoi.swd392.dto.request.CreateCourseRequest;
import com.minhkhoi.swd392.dto.response.CourseResponse;
import com.minhkhoi.swd392.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    // Các mockBean security cơ bản
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void createCourse_WithPaymentAndThumbnail_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("thumbnailFile", "thumb.jpg", "image/jpeg", "image content".getBytes());
        CourseResponse response = CourseResponse.builder()
                .courseId(UUID.randomUUID())
                .title("Paid Course")
                .price(new BigDecimal("99.99"))
                .status(CourseStatus.DRAFT)
                .build();

        // Mock methods
        when(courseService.createCourse(any(CreateCourseRequest.class))).thenReturn(response);

        mockMvc.perform(multipart("/api/courses")
                        .file(file)
                        .param("title", "Paid Course")
                        .param("description", "Course Description")
                        .param("price", "99.99")
                        .param("status", "DRAFT")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Paid Course"))
                .andExpect(jsonPath("$.data.price").value(99.99));
    }
}
