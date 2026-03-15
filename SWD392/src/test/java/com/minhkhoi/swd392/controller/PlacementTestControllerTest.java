package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.placement.PlacementQuestionResponse;
import com.minhkhoi.swd392.dto.placement.PlacementTestResultResponse;
import com.minhkhoi.swd392.dto.placement.SubmitPlacementTestRequest;
import com.minhkhoi.swd392.service.PlacementTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlacementTestController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for public endpoints testing
public class PlacementTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlacementTestService placementTestService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.minhkhoi.swd392.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getQuestions_Success() throws Exception {
        PlacementQuestionResponse q = PlacementQuestionResponse.builder().content("Q1").build();
        when(placementTestService.getRandomQuestions(anyInt())).thenReturn(List.of(q));

        mockMvc.perform(get("/api/placement-test/questions").param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Q1"));
    }

    @Test
    void submitTest_Success() throws Exception {
        SubmitPlacementTestRequest request = new SubmitPlacementTestRequest();
        request.setAnswers(List.of(new SubmitPlacementTestRequest.PlacementAnswerItem()));
        
        PlacementTestResultResponse response = PlacementTestResultResponse.builder()
                .correctCount(5)
                .totalQuestions(10)
                .estimatedLevel(com.minhkhoi.swd392.constant.JlptLevel.N3)
                .build();

        when(placementTestService.submitTest(any(SubmitPlacementTestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/placement-test/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(5))
                .andExpect(jsonPath("$.estimatedLevel").value("N3"));
    }
}
