package com.minhkhoi.swd392.repository;

import com.minhkhoi.swd392.constant.JlptLevel;
import com.minhkhoi.swd392.entity.PlacementQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementQuestionRepository extends JpaRepository<PlacementQuestion, UUID> {

    List<PlacementQuestion> findByJlptLevel(JlptLevel jlptLevel);

    /** Lấy ngẫu nhiên N câu hỏi - sử dụng random() của PostgreSQL */
    @Query(value = "SELECT * FROM placement_questions ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementQuestion> findRandomQuestions(int limit);

    /** Lấy ngẫu nhiên N câu theo level cụ thể */
    @Query(value = "SELECT * FROM placement_questions WHERE jlpt_level = :level ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementQuestion> findRandomByLevel(String level, int limit);

    /**
     * Lấy ngẫu nhiên N câu theo loại câu hỏi (READING hoặc LISTENING).
     * Dùng để phân tách câu text và câu nghe khi build bộ đề.
     */
    @Query(value = "SELECT * FROM placement_questions WHERE question_type = :questionType ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementQuestion> findRandomByQuestionType(String questionType, int limit);

    /** Đếm số câu theo loại */
    long countByQuestionType(PlacementQuestion.QuestionType questionType);

    long countByJlptLevel(JlptLevel jlptLevel);
}
