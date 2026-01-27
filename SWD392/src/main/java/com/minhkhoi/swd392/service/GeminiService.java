package com.minhkhoi.swd392.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeminiService {

    /**
     * Mock method to generate quiz questions from transcript via Gemini.
     * In a real implementation, this would call the Google Gemini API.
     * @param transcript The lesson transcript
     * @return JSON string containing the generated questions
     */
    public String generateQuizQuestions(String transcript) {
        log.info("Mocking Gemini generation for transcript size: {}", transcript.length());
        
        // Mock processing delay
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Return a mock JSON string representing 5 questions
        return """
            [
              {
                "content": "What is the primary function of this video lesson?",
                "options": {
                  "A": "To entertain",
                  "B": "To educate about specific technical concepts",
                  "C": "To promote a product",
                  "D": "None of the above"
                },
                "correctAnswer": "B",
                "explanation": "The video focuses on technical instruction."
              },
              {
                "content": "Which key concept was introduced first?",
                "options": {
                  "A": "Concept A",
                  "B": "Concept B",
                  "C": "Introduction",
                  "D": "Summary"
                },
                "correctAnswer": "C",
                "explanation": "The video starts with an introduction."
              },
               {
                "content": "What is the result of the main process described?",
                "options": {
                  "A": "A new file",
                  "B": "A database record",
                  "C": "A completed transaction",
                  "D": "All of the above"
                },
                "correctAnswer": "D",
                "explanation": "The process impacts multiple layers."
              },
               {
                "content": "Why is it important to follow the steps in order?",
                "options": {
                  "A": "To avoid errors",
                  "B": "To save time",
                  "C": "It is not important",
                  "D": "Both A and B"
                },
                "correctAnswer": "D",
                "explanation": "Efficiency and correctness rely on the order."
              },
               {
                "content": "What is the final step mentioned?",
                "options": {
                  "A": "Cleanup",
                  "B": "Review",
                  "C": "Deployment",
                  "D": "Testing"
                },
                "correctAnswer": "B",
                "explanation": "The video concludes with a review phase."
              }
            ]
            """;
    }
}
