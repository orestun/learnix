package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.keyboard.TopicKeyboardFactory;
import com.learn.learnix.domain.Topic;
import com.learn.learnix.service.*;
import com.learn.learnix.state.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartHandler {

    private final UserService      userService;
    private final TopicService     topicService;
    private final SessionService   sessionService;
    private final UserStateManager stateManager;

    public void handle(AbsSender bot, Long telegramId, Long chatId) throws Exception {

        // Register or refresh the user
        userService.getOrRegister(telegramId,
                telegramId.toString()); // username resolved from Update in real call

        // Abandon any in-progress session cleanly
        sessionService.abandonCurrentSession(telegramId);

        // Load active topics
        List<Topic> topics = topicService.getActiveTopics();

        bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Welcome! Choose a topic to begin:")
                .replyMarkup(TopicKeyboardFactory.build(topics))
                .build());

        stateManager.setBotState(telegramId, BotState.SELECTING_TOPIC);
    }
}