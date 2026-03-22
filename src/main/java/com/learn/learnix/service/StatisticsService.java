package com.learn.learnix.service;

import com.learn.learnix.domain.*;
import com.learn.learnix.domain.dto.SessionStatsDto;
import com.learn.learnix.repository.SessionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final SessionRepository sessionRepository;
    private final UserService userService;

    // ── Build stats for the session that just completed ───────────────────

    /**
     * Called immediately after SessionService marks the session COMPLETED.
     * Loads the most recently finished session for the user and maps it to a DTO.
     */
    @Transactional(readOnly = true)
    public SessionStatsDto buildForLastSession(Long telegramId) {
        Session session = sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.COMPLETED)
                .orElseThrow(() -> new IllegalStateException(
                        "No completed session found for telegramId=" + telegramId));

        return mapToDto(session);
    }

    /**
     * Overload that works directly from a known session ID —
     * useful when the caller already holds the session reference.
     */
    @Transactional(readOnly = true)
    public SessionStatsDto buildForSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session not found: id=" + sessionId));

        return mapToDto(session);
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private SessionStatsDto mapToDto(Session session) {
        long durationSeconds = Duration.between(
                session.getStartedAt(),
                session.getFinishedAt()).getSeconds();

        List<SessionStatsDto.QuestionStatDto> questionStats = session.getSessionQuestions()
                .stream()
                .sorted(Comparator.comparingInt(SessionQuestion::getSequenceOrder))
                .map(this::mapQuestion)
                .toList();

        return SessionStatsDto.builder()
                .topicName(session.getTopic().getName())
                .totalQuestions(session.getTotalQuestions())
                .correctAnswers(session.getCorrectAnswers())
                .durationSeconds(durationSeconds)
                .questionStats(questionStats)
                .build();
    }

    private SessionStatsDto.QuestionStatDto mapQuestion(SessionQuestion sq) {
        Attempt attempt = sq.getAttempt();

        // attempt should always exist for a COMPLETED session,
        // but we guard defensively just in case
        boolean correct    = attempt != null && attempt.getIsCorrect();
        int     timeTakenMs = attempt != null && attempt.getTimeTakenMs() != null
                ? attempt.getTimeTakenMs() : 0;

        return SessionStatsDto.QuestionStatDto.builder()
                .sequenceNumber(sq.getSequenceOrder() + 1)   // convert 0-based to 1-based
                .questionBody(sq.getQuestion().getBody())
                .correct(correct)
                .timeTakenMs(timeTakenMs)
                .build();
    }
}
