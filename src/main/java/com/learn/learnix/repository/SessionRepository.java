package com.learn.learnix.repository;

import com.learn.learnix.domain.Session;
import com.learn.learnix.domain.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
            Long telegramId, SessionStatus status);

    List<Session> findAllByUserTelegramIdOrderByStartedAtDesc(Long telegramId);
}