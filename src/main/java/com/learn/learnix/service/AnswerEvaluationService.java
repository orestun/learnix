package com.learn.learnix.service;

import com.learn.learnix.domain.*;
import com.learn.learnix.repository.AttemptRepository;
import com.learn.learnix.repository.SessionQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerEvaluationService {

    private final SessionQuestionRepository sessionQuestionRepository;
    private final AttemptRepository attemptRepository;
    private final UserService userService;

    /**
     * Evaluates a user's answer for the current question in their active session.
     *
     * Correctness rule: the set of selected choice IDs must exactly match
     * the set of correct choice IDs — no more, no less.
     *
     * @param telegramId      the Telegram user ID
     * @param selectedIds     choice IDs the user toggled ON before hitting Submit
     * @param timeTakenMs     ms elapsed from question send to submit callback
     * @return true if the answer is fully correct
     */
    @Transactional
    public boolean evaluate(Long telegramId, Set<Long> selectedIds, int timeTakenMs) {
        User user = userService.getByTelegramId(telegramId);

        SessionQuestion sq = sessionQuestionRepository
                .findNextUnanswered(currentSessionId(user))
                .orElseThrow(() -> new IllegalStateException(
                        "No unanswered question found for telegramId=" + telegramId));

        Question question = sq.getQuestion();

        // Collect the IDs of every choice marked correct in the DB
        Set<Long> correctIds = question.getChoices().stream()
                .filter(Choice::getIsCorrect)
                .map(Choice::getId)
                .collect(Collectors.toSet());

        boolean isCorrect = correctIds.equals(selectedIds);

        // Resolve the full Choice entities the user selected
        Set<Choice> selectedChoices = question.getChoices().stream()
                .filter(c -> selectedIds.contains(c.getId()))
                .collect(Collectors.toSet());

        // Persist the attempt
        Attempt attempt = Attempt.builder()
                .sessionQuestion(sq)
                .isCorrect(isCorrect)
                .timeTakenMs(timeTakenMs)
                .selectedChoices(selectedChoices)
                .build();

        attemptRepository.save(attempt);

        // Update the session score counter
        Session session = sq.getSession();
        if (isCorrect) {
            session.incrementCorrect();
        }

        log.debug("Evaluated answer for telegramId={} questionId={} correct={}",
                telegramId, question.getId(), isCorrect);

        return isCorrect;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long currentSessionId(User user) {
        return user.getSessions().stream()
                .filter(s -> s.getStatus() == SessionStatus.IN_PROGRESS)
                .findFirst()
                .map(Session::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "No active session for user id=" + user.getId()));
    }
}