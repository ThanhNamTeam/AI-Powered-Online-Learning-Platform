package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.dto.placement.GenerateQuestionsResponse;
import com.minhkhoi.swd392.dto.placement.PlacementDocumentResponse;
import com.minhkhoi.swd392.dto.placement.UploadPlacementDocumentRequest;
import com.minhkhoi.swd392.entity.PlacementDocument;
import com.minhkhoi.swd392.entity.PlacementDocument.DocumentType;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.repository.PlacementDocumentRepository;
import com.minhkhoi.swd392.repository.PlacementQuestionRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlacementDocumentServiceTest {

    @Mock
    private PlacementDocumentRepository documentRepository;
    @Mock
    private PlacementQuestionRepository questionRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private GeminiService geminiService;
    @Mock
    private AssemblyAITranscriptionService assemblyAIService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlacementDocumentService placementDocumentService;

    private User staff;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        staff = User.builder()
                .email("staff@test.com")
                .fullName("Test Staff")
                .build();

        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void uploadReadingDocument_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "Dummy content".getBytes());
        UploadPlacementDocumentRequest request = new UploadPlacementDocumentRequest();
        request.setTitle("Reading Test");
        request.setTargetLevel("N3");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("staff@test.com");
        when(userRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(Map.of(
                "secure_url", "http://cloudinary.com/test.pdf",
                "public_id", "test_id"
        ));

        // Act
        PlacementDocumentResponse response = placementDocumentService.uploadReadingDocument(file, request);

        // Assert
        assertNotNull(response);
        assertEquals("Reading Test", response.getTitle());
        assertEquals(DocumentType.READING, response.getDocumentType());
        verify(documentRepository, times(1)).save(any());
    }

    @Test
    void uploadListeningDocument_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "audio.mp3", "audio/mpeg", "Dummy audio".getBytes());
        UploadPlacementDocumentRequest request = new UploadPlacementDocumentRequest();
        request.setTitle("Listening Test");
        request.setTargetLevel("N4");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("staff@test.com");
        when(userRepository.findByEmail("staff@test.com")).thenReturn(Optional.of(staff));
        when(cloudinaryService.uploadFile(any(), eq("video"))).thenReturn(Map.of(
                "secure_url", "http://cloudinary.com/audio.mp3",
                "public_id", "audio_id"
        ));

        // Act
        PlacementDocumentResponse response = placementDocumentService.uploadListeningDocument(file, request);

        // Assert
        assertNotNull(response);
        assertEquals("Listening Test", response.getTitle());
        assertEquals(DocumentType.LISTENING, response.getDocumentType());
        verify(documentRepository, times(1)).save(any());
    }

    @Test
    void generateQuestionsFromDocument_Success() {
        // Arrange
        UUID docId = UUID.randomUUID();
        PlacementDocument doc = PlacementDocument.builder()
                .id(docId)
                .documentType(DocumentType.READING)
                .extractedContent("Sample Japanese content")
                .targetLevel(JlptLevel.N3)
                .title("Reading Doc")
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(geminiService.callGeminiWithPrompt(anyString())).thenReturn("[{\"content\": \"Question 1?\", \"options\": {\"A\": \"ans1\", \"B\": \"ans2\", \"C\": \"ans3\", \"D\": \"ans4\"}, \"correctAnswer\": \"A\", \"explanation\": \"exp\", \"topic\": \"Grammar\", \"jlptLevel\": \"N3\"}]");
        when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlacementQuestion> list = invocation.getArgument(0);
            list.forEach(q -> q.setId(UUID.randomUUID()));
            return list;
        });

        // Act
        GenerateQuestionsResponse response = placementDocumentService.generateQuestionsFromDocument(docId, 1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getQuestionsGenerated());
        verify(questionRepository, times(1)).saveAll(anyList());
        assertEquals(PlacementDocument.DocumentStatus.PROCESSED, doc.getStatus());
    }

    @Test
    void createListeningQuestionsFromAudio_Success() throws IOException {
        // Arrange
        UUID docId = UUID.randomUUID();
        PlacementDocument doc = PlacementDocument.builder()
                .id(docId)
                .documentType(DocumentType.LISTENING)
                .fileUrl("http://cloudinary.com/audio.mp3")
                .targetLevel(JlptLevel.N4)
                .title("Audio Doc")
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(assemblyAIService.transcribeVideo(anyString(), eq("ja"))).thenReturn("Transcribed text");
        when(geminiService.callGeminiWithPrompt(anyString())).thenReturn("[{\"content\": \"Listen and answer\", \"options\": {\"A\": \"a\", \"B\": \"b\", \"C\": \"c\", \"D\": \"d\"}, \"correctAnswer\": \"B\", \"explanation\": \"exp\", \"topic\": \"Listening\", \"jlptLevel\": \"N4\"}]");
        when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlacementQuestion> list = invocation.getArgument(0);
            list.forEach(q -> q.setId(UUID.randomUUID()));
            return list;
        });

        // Act
        GenerateQuestionsResponse response = placementDocumentService.createListeningQuestionsFromAudio(docId, 1);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getQuestionsGenerated());
        verify(questionRepository, times(1)).saveAll(anyList());
        assertEquals(PlacementDocument.DocumentStatus.PROCESSED, doc.getStatus());
    }
}
