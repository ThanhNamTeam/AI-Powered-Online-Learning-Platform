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

    @Query(value = "SELECT * FROM placement_questions WHERE question_type = :questionType ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<PlacementQuestion> findRandomByQuestionType(String questionType, int limit);

}
