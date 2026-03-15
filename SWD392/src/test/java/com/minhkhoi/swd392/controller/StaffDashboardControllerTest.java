package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.response.StaffDashboardResponse;
import com.minhkhoi.swd392.service.StaffDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StaffDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StaffDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaffDashboardService staffDashboardService;

    // Security mockBeans
    @MockBean private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @MockBean private com.minhkhoi.swd392.service.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Test
    @WithMockUser(roles = "STAFF")
    void getDashboard_Success() throws Exception {
        // Arrange
        StaffDashboardResponse.WeeklyStat mondayStat = StaffDashboardResponse.WeeklyStat.builder()
                .day("Thứ Hai")
                .registrations(5)
                .revenue(new BigDecimal("250000"))
                .build();

        StaffDashboardResponse.TopCourseInfo topCourse = StaffDashboardResponse.TopCourseInfo.builder()
                .courseId("course-001")
                .title("Spring Boot Advanced")
                .code("SPRAB")
                .students(45)
                .rating(4.8)
                .build();

        StaffDashboardResponse response = StaffDashboardResponse.builder()
                .newStudentsToday(10)
                .revenueToday(new BigDecimal("1000000"))
                .pendingRequests(5)
                .averageRating(4.8)
                .weeklyPerformance(List.of(mondayStat))
                .topTrendingCourses(List.of(topCourse))
                .build();

        when(staffDashboardService.getDashboardStats()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/staff/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newStudentsToday").value(10))
                .andExpect(jsonPath("$.data.revenueToday").value(1000000))
                .andExpect(jsonPath("$.data.pendingRequests").value(5))
                .andExpect(jsonPath("$.data.averageRating").value(4.8))
                .andExpect(jsonPath("$.data.weeklyPerformance").isArray())
                .andExpect(jsonPath("$.data.weeklyPerformance[0].day").value("Thứ Hai"))
                .andExpect(jsonPath("$.data.weeklyPerformance[0].registrations").value(5))
                .andExpect(jsonPath("$.data.topTrendingCourses").isArray())
                .andExpect(jsonPath("$.data.topTrendingCourses[0].title").value("Spring Boot Advanced"))
                .andExpect(jsonPath("$.data.topTrendingCourses[0].students").value(45));
    }
}
