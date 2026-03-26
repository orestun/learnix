package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.keyboard.TopicKeyboardFactory;
import com.learn.learnix.domain.Topic;
import com.learn.learnix.domain.dto.TopicDto;
import com.learn.learnix.service.*;
import com.learn.learnix.state.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
        userService.getOrRegister(telegramId,
                telegramId.toString());

        sessionService.abandonCurrentSession(telegramId);

        List<Topic> topics = topicService.getActiveTopics();
        List<TopicDto> topicDtoList = topics.stream()
                .map(t -> TopicDto.builder()
                        .name(t.getName())
                        .id(t.getId())
                        .build()).toList();

        Message sent = bot.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Welcome! Choose a topic to begin:")
                .replyMarkup(TopicKeyboardFactory.build(topicDtoList, false, 0))
                .build());

        stateManager.setBotState(telegramId, BotState.SELECTING_TOPIC);
        UserQuizState state = stateManager.getState(telegramId);
        state.setActiveMessageId(sent.getMessageId());
    }
}