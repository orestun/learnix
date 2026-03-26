package com.learn.learnix.state;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UserQuizState {

    /**
     * Current FSM state — controls which handler processes the next update.
     */
    private BotState botState = BotState.IDLE;

    /**
     * Telegram message ID of the question currently on screen.
     * Required for EditMessageReplyMarkup (toggle) and EditMessageText (result).
     */
    private Integer activeMessageId;

    /**
     * Epoch millis recorded when the question message was sent.
     * Used to calculate time_taken_ms when the user submits.
     */
    private long questionSentAt;

    /**
     * Choice IDs the user has toggled ON for the current question.
     * Cleared after every submit.
     */
    private Set<Long> selectedChoiceIds = new HashSet<>();

    /**
     * Toggles a choice on or off.
     * If the ID is not in the set it is added; if it is already there it is removed.
     */
    public void toggleChoice(Long choiceId) {
        if (!selectedChoiceIds.add(choiceId)) {
            selectedChoiceIds.remove(choiceId);
        }
    }

    /**
     * Clears per-question state after submit.
     * BotState is NOT reset here — the caller decides the next state.
     */
    public void resetSelection() {
        selectedChoiceIds.clear();
        activeMessageId = null;
        questionSentAt = 0;
    }

    public boolean hasSelections() {
        return !selectedChoiceIds.isEmpty();
    }
}