package com.learn.learnix.bot.handler;

import com.learn.learnix.domain.SessionQuestion;
import com.learn.learnix.service.SessionService;
import com.learn.learnix.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class TopicHandler {

    private final SessionService   sessionService;
    private final UserStateManager stateManager;
    private final QuestionSender   questionSender;   // see below

    public void handle(AbsSender bot, CallbackQuery callback) throws Exception {
        Long telegramId = callback.getFrom().getId();
        Long chatId     = callback.getMessage().getChatId();
        Long topicId    = Long.parseLong(callback.getData().replace("topic:", ""));

        sessionService.startSession(telegramId, topicId);

        SessionQuestion first = sessionService.getCurrentQuestion(telegramId);
        questionSender.send(bot, chatId, telegramId, first);
    }
}