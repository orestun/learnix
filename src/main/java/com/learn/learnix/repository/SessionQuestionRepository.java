package com.learn.learnix.repository;

import com.learn.learnix.domain.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {

    /**
     * Returns the next unanswered question in the session, ordered by sequence.
     */
    @Query("""
            SELECT sq FROM SessionQuestion sq
            WHERE sq.session.id = :sessionId
              AND sq.attempt IS NULL
            ORDER BY sq.sequenceOrder ASC
            LIMIT 1
            """)
    Optional<SessionQuestion> findNextUnanswered(@Param("sessionId") Long sessionId);
}