package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.builder.MessageBuilder;
import com.learn.learnix.bot.formatter.MessageFormatter;
import com.learn.learnix.domain.Session;
import com.learn.learnix.domain.SessionQuestion;
import com.learn.learnix.domain.SubTopic;
import com.learn.learnix.domain.Topic;
import com.learn.learnix.service.SessionService;
import com.learn.learnix.state.UserQuizState;
import com.learn.learnix.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class QuestionQuantityHandler {
    private final SessionService sessionService;
    private final UserStateManager stateManager;
    private final QuestionSender   questionSender;

    @Transactional
    public void handle(AbsSender bot, CallbackQuery callback) throws Exception {
        long telegramId = callback.getFrom().getId();
        long chatId     = callback.getMessage().getChatId();
        int quantity    = Integer.parseInt(callback.getData().replace("quantity:", ""));

        UserQuizState state = stateManager.getState(telegramId);
        Session session = sessionService.getInProgressSessionByTelegramId(telegramId);
        Topic topic = session.getTopic();
        SubTopic subTopic = session.getSubTopic();

        String message = MessageFormatter.formatClarifyMessage(
                topic.getName(),
                subTopic != null ? subTopic.getName() : null,
                quantity,
                "Good luck!");

        bot.execute(MessageBuilder.buildEditMessageText(chatId, state.getActiveMessageId(), message));

        sessionService.generateSessionQuestions(telegramId, quantity);
        sessionService.startQuiz(session, telegramId);

        SessionQuestion first = sessionService.getCurrentQuestion(telegramId);
        questionSender.send(bot, chatId, telegramId, first);
    }

}
