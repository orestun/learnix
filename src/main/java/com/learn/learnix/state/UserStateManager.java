package com.learn.learnix.state;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateManager {

    private final ConcurrentHashMap<Long, UserQuizState> states
            = new ConcurrentHashMap<>();

    public UserQuizState getState(Long telegramId) {
        return states.computeIfAbsent(telegramId, id -> new UserQuizState());
    }

    public BotState getBotState(Long telegramId) {
        return getState(telegramId).getBotState();
    }

    public void setBotState(Long telegramId, BotState state) {
        getState(telegramId).setBotState(state);
    }

    public void reset(Long telegramId) {
        // Full wipe — called when session ends or user sends /start mid-quiz
        states.remove(telegramId);
    }
}