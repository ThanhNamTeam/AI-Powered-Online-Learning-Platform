package com.minhkhoi.swd392.controller;

import com.minhkhoi.swd392.dto.placement.GenerateQuestionsResponse;
import com.minhkhoi.swd392.dto.placement.PlacementDocumentResponse;
import com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentType;
import com.minhkhoi.swd392.service.PlacementDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlacementDocumentController.class)
@AutoConfigureMockMvc
public class PlacementDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlacementDocumentService placementDocumentService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.minhkhoi.swd392.service.JwtService jwtService;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.minhkhoi.swd392.repository.RedisTokenRepository redisTokenRepository;

    @Test
    @WithMockUser(roles = "STAFF")
    void uploadReadingDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        PlacementDocumentResponse response = PlacementDocumentResponse.builder().title("Test Doc").build();

        when(placementDocumentService.uploadReadingDocument(any(), any(UploadPlacementDocumentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/placement-documents/reading")
                        .file(file)
                        .param("title", "Test Doc")
                        .param("targetLevel", "N3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Doc"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void generateQuestions_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        GenerateQuestionsResponse response = GenerateQuestionsResponse.builder()
                .documentId(docId)
                .questionsGenerated(10)
                .build();

        when(placementDocumentService.generateQuestionsFromDocument(eq(docId), anyInt())).thenReturn(response);

        mockMvc.perform(post("/api/placement-documents/{id}/generate-reading", docId)
                        .param("questionCount", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionsGenerated").value(10));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getAllDocuments_Success() throws Exception {
        PlacementDocumentResponse doc = PlacementDocumentResponse.builder().title("Doc 1").build();
        when(placementDocumentService.getAllDocuments()).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/placement-documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Doc 1"));
    }
}
