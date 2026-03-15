package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.InstructorDashboardResponse;
import com.minhkhoi.swd392.service.InstructorDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstructorDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InstructorDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstructorDashboardService instructorDashboardService;

    // Các mockBean security cơ bản
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getDashboard_Success() throws Exception {
        InstructorDashboardResponse response = new InstructorDashboardResponse();
        response.setTotalCourses(5);
        response.setTotalRevenue(new BigDecimal("1500.00"));
        response.setTotalEnrollments(120);

        when(instructorDashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/instructor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCourses").value(5))
                .andExpect(jsonPath("$.data.totalRevenue").value(1500.00))
                .andExpect(jsonPath("$.data.totalEnrollments").value(120));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getMyCourses_Success() throws Exception {
        when(instructorDashboardService.getMyCourses(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/instructor/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getMyStudents_Success() throws Exception {
        when(instructorDashboardService.getMyStudents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/instructor/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
