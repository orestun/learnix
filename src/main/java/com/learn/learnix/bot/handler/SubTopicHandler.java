package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.builder.MessageBuilder;
import com.learn.learnix.bot.formatter.MessageFormatter;
import com.learn.learnix.bot.keyboard.QuestionsQuantityKeyboardFactory;
import com.learn.learnix.domain.SubTopic;
import com.learn.learnix.domain.Topic;
import com.learn.learnix.service.SessionService;
import com.learn.learnix.service.SubTopicService;
import com.learn.learnix.service.TopicService;
import com.learn.learnix.state.BotState;
import com.learn.learnix.state.TopicType;
import com.learn.learnix.state.UserQuizState;
import com.learn.learnix.state.UserStateManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class SubTopicHandler {

    private final SessionService sessionService;
    private final UserStateManager stateManager;
    private final SubTopicService subTopicService;
    private final TopicService topicService;

    public void handle(AbsSender bot, CallbackQuery callback) throws Exception {
        long telegramId = callback.getFrom().getId();
        long chatId     = callback.getMessage().getChatId();
        long topicId;
        String message;
        Topic topic;
        SubTopic subTopic;
        UserQuizState state = stateManager.getState(telegramId);

        if (callback.getData().contains("all")) {
            topicId = Long.parseLong(callback.getData().replace("all:", ""));
            sessionService.startSession(telegramId, topicId, TopicType.TOPIC);
            topic = topicService.getById(topicId);

            message = MessageFormatter.formatClarifyMessage(
                    topic.getName(),
                    null,
                    null,
                    "Choose how many questions you want to answer:");
        } else {
            topicId = Long.parseLong(callback.getData().replace("subtopic:", ""));
            sessionService.startSession(telegramId, topicId, TopicType.SUB_TOPIC);
            subTopic = subTopicService.getSubTopicById(topicId);
            topic = topicService.getById(subTopicService.getTopicIdBySubTopicId(topicId));

            message = MessageFormatter.formatClarifyMessage(
                    topic.getName(),
                    subTopic.getName(),
                    null,
                    "Choose how many questions you want to answer:");
        }

        bot.execute(MessageBuilder.buildEditMessageText(chatId, state.getActiveMessageId(), message));
        bot.execute(MessageBuilder.buildEditMessageReplyMarkup(chatId, state.getActiveMessageId(), QuestionsQuantityKeyboardFactory.build()));
        stateManager.setBotState(telegramId, BotState.CHOOSING_QUESTION_SET_QUANTITY);
    }
}
