package com.learn.learnix.service;

import com.learn.learnix.domain.*;
import com.learn.learnix.repository.*;
import com.learn.learnix.state.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository      sessionRepository;
    private final SessionQuestionRepository sqRepository;
    private final UserService            userService;
    private final QuestionService        questionService;
    private final TopicService           topicService;
    private final UserStateManager       stateManager;

    // ── 1. Start a new session ────────────────────────────────────────────

    @Transactional
    public Session startSession(Long telegramId, Long topicId) {
        User  user  = userService.getByTelegramId(telegramId);
        Topic topic = topicService.getById(topicId);

        // Abandon any lingering IN_PROGRESS session before starting fresh
        sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .ifPresent(old -> {
                    old.abandon();
                    log.info("Abandoned stale session id={} for telegramId={}",
                            old.getId(), telegramId);
                });

        // Shuffle questions and build the sequence
        List<Question> questions = questionService.getShuffledForTopic(topicId);

        Session session = Session.builder()
                .user(user)
                .topic(topic)
                .totalQuestions(questions.size())
                .build();

        sessionRepository.save(session);

        // Create one SessionQuestion row per question, preserving shuffle order
        for (int i = 0; i < questions.size(); i++) {
            SessionQuestion sq = SessionQuestion.builder()
                    .session(session)
                    .question(questions.get(i))
                    .sequenceOrder(i)
                    .build();
            sqRepository.save(sq);
        }

        // Transition FSM state
        stateManager.setBotState(telegramId, BotState.IN_QUIZ);
        stateManager.getState(telegramId).setQuestionSentAt(System.currentTimeMillis());

        log.info("Started session id={} topicId={} for telegramId={} questions={}",
                session.getId(), topicId, telegramId, questions.size());

        return session;
    }

    // ── 2. Get the current unanswered question ────────────────────────────

    @Transactional(readOnly = true)
    public SessionQuestion getCurrentQuestion(Long telegramId) {
        Long sessionId = getActiveSessionId(telegramId);
        return sqRepository.findNextUnanswered(sessionId)
                .orElseThrow(() -> new IllegalStateException(
                        "No current question for telegramId=" + telegramId));
    }

    // ── 3. Advance after an answer is submitted ───────────────────────────

    @Transactional
    public AdvanceResult advance(Long telegramId) {
        Long sessionId = getActiveSessionId(telegramId);
        boolean hasNext = sqRepository.findNextUnanswered(sessionId).isPresent();

        if (hasNext) {
            // Reset per-question selection state, keep FSM in IN_QUIZ
            stateManager.getState(telegramId).resetSelection();
            stateManager.getState(telegramId).setQuestionSentAt(System.currentTimeMillis());
            return AdvanceResult.NEXT_QUESTION;
        } else {
            // All questions answered — complete the session
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow();
            session.complete();
            stateManager.reset(telegramId);  // wipe FSM state, session is done
            log.info("Completed session id={} for telegramId={}", sessionId, telegramId);
            return AdvanceResult.SESSION_COMPLETE;
        }
    }

    // ── 4. Abandon mid-quiz (e.g. user sends /start again) ───────────────

    @Transactional
    public void abandonCurrentSession(Long telegramId) {
        sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .ifPresent(Session::abandon);
        stateManager.reset(telegramId);
    }

    // ── Result signal to the bot handler ─────────────────────────────────

    public enum AdvanceResult {
        NEXT_QUESTION,
        SESSION_COMPLETE
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Long getActiveSessionId(Long telegramId) {
        return sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .map(Session::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "No active session for telegramId=" + telegramId));
    }
}