package com.learn.learnix.repository;

import com.learn.learnix.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
            SELECT * FROM questions
            WHERE topic_id = :topicId
            ORDER BY RANDOM()
            LIMIT :total
            """, nativeQuery = true)
    List<Question> findRandomByTopicId(@Param("topicId")Long topicId, @Param("total") int totalQuestions);

    @Query(value = """
            SELECT * FROM questions
            WHERE sub_topic_id = :subTopicId AND topic_id = :topicId
            ORDER BY RANDOM()
            LIMIT :total
            """, nativeQuery = true)
    List<Question> findRandomBySubTopicId(@Param("topicId")Long topicId, @Param("subTopicId") Long subTopicId, @Param("total") int totalQuestions);
}