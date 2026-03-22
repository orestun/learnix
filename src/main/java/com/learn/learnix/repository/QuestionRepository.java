package com.learn.learnix.repository;

import com.learn.learnix.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId")
    List<Question> findAllByTopicId(@Param("topicId") Long topicId);

    // Fetches the question AND its choices in a single query
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.choices WHERE q.id = :id")
    Optional<Question> findByIdWithChoices(@Param("id") Long id);
}