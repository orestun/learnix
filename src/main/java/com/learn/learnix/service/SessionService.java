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
    private final SubTopicService        subTopicService;
    private final UserStateManager       stateManager;

    @Transactional
    public void startSession(Long telegramId, Long topicId, TopicType topicType) {
        User  user  = userService.getByTelegramId(telegramId);
        Topic topic;
        SubTopic subTopic = null;

        if (TopicType.TOPIC.equals(topicType)) {
            topic = topicService.getById(topicId);
        } else {
            subTopic = subTopicService.getSubTopicById(topicId);
            topic = subTopic.getTopic();
        }

        // Abandon any lingering IN_PROGRESS session before starting fresh
        sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .ifPresent(old -> {
                    old.abandon();
                    log.info("Abandoned stale session id={} for telegramId={}",
                            old.getId(), telegramId);
                });

        Session session = Session.builder()
                .user(user)
                .topic(topic)
                .totalQuestions(0)
                .build();

        if (subTopic != null) {
            session.setSubTopic(subTopic);
        }

        sessionRepository.save(session);
    }

    @Transactional
    public void generateSessionQuestions(Long telegramId, int totalQuestions) {
        Session session = getInProgressSessionByTelegramId(telegramId);
        TopicType topicType;
        long topicId;

        if (session.getSubTopic() != null) {
            topicId = session.getSubTopic().getId();
            topicType = TopicType.SUB_TOPIC;
        } else {
            topicId = session.getTopic().getId();
            topicType = TopicType.TOPIC;
        }

        // Shuffle questions and build the sequence
        List<Question> questions = questionService.getQuestionsForTopic(topicId, topicType, totalQuestions);

        session.setTotalQuestions(questions.size());

        // Create one SessionQuestion row per question, preserving shuffle order
        for (int i = 0; i < questions.size(); i++) {
            SessionQuestion sq = SessionQuestion.builder()
                    .session(session)
                    .question(questions.get(i))
                    .sequenceOrder(i)
                    .build();

            sqRepository.save(sq);
        }
    }

    public Session getInProgressSessionByTelegramId(Long telegramId) {
        return sessionRepository.findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(telegramId, SessionStatus.IN_PROGRESS)
                .orElseThrow(() -> new IllegalStateException("Session not found for telegramId=" + telegramId));
    }

    @Transactional
    public void startQuiz(Session session, long telegramId) {
        // Transition FSM state
        stateManager.setBotState(telegramId, BotState.IN_QUIZ);
        stateManager.getState(telegramId).setQuestionSentAt(System.currentTimeMillis());

        log.info("Started session id={} for telegramId={}",
                session.getId(), telegramId);

    }

    @Transactional(readOnly = true)
    public SessionQuestion getCurrentQuestion(Long telegramId) {
        Long sessionId = getActiveSessionId(telegramId);
        return sqRepository.findNextUnansweredWithChoices(sessionId)
                .orElseThrow(() -> new IllegalStateException(
                        "No current question for telegramId=" + telegramId));
    }

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
            completeSession(telegramId);
            return AdvanceResult.SESSION_COMPLETE;
        }
    }

    @Transactional
    public void abandonCurrentSession(Long telegramId) {
        sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .ifPresent(Session::abandon);
        stateManager.reset(telegramId);
    }

    @Transactional
    public void completeSession(Long telegramId) {
        Long sessionId = getActiveSessionId(telegramId);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();
        session.complete();
        stateManager.reset(telegramId);  // wipe FSM state, session is done
        log.info("Completed session id={} for telegramId={}", sessionId, telegramId);
    }

    public enum AdvanceResult {
        NEXT_QUESTION,
        SESSION_COMPLETE
    }

    private Long getActiveSessionId(Long telegramId) {
        return sessionRepository
                .findTopByUserTelegramIdAndStatusOrderByStartedAtDesc(
                        telegramId, SessionStatus.IN_PROGRESS)
                .map(Session::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "No active session for telegramId=" + telegramId));
    }
}