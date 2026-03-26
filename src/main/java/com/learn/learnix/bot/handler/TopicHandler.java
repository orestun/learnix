package com.learn.learnix.bot.handler;

import com.learn.learnix.bot.builder.MessageBuilder;
import com.learn.learnix.bot.formatter.MessageFormatter;
import com.learn.learnix.bot.keyboard.QuestionsQuantityKeyboardFactory;
import com.learn.learnix.bot.keyboard.TopicKeyboardFactory;
import com.learn.learnix.domain.SubTopic;
import com.learn.learnix.domain.Topic;
import com.learn.learnix.domain.dto.TopicDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TopicHandler {

    private final SubTopicService subTopicService;
    private final UserStateManager stateManager;
    private final SessionService sessionService;
    private final TopicService topicService;

    public void handle(AbsSender bot, CallbackQuery callback) throws Exception {
        long telegramId = callback.getFrom().getId();
        long chatId     = callback.getMessage().getChatId();
        long topicId    = Long.parseLong(callback.getData().replace("topic:", ""));
        UserQuizState state = stateManager.getState(telegramId);

        Topic topic = topicService.getById(topicId);
        Set<SubTopic> subTopics = subTopicService.getAllSubTopicsByTopicId(topicId);

        if (subTopics.isEmpty()) {
            sessionService.startSession(telegramId, topicId, TopicType.TOPIC);

            String message = MessageFormatter.formatClarifyMessage(
                    topic.getName(),
                    null,
                    null,
                    "Choose how many questions you want to answer:");

            bot.execute(MessageBuilder.buildEditMessageText(chatId, state.getActiveMessageId(), message));
            bot.execute(MessageBuilder.buildEditMessageReplyMarkup(chatId, state.getActiveMessageId(), QuestionsQuantityKeyboardFactory.build()));
            stateManager.setBotState(telegramId, BotState.CHOOSING_QUESTION_SET_QUANTITY);
        } else {
            List<TopicDto> topicDtoList = new ArrayList<>(subTopics
                    .stream()
                    .map(st -> TopicDto.builder()
                            .name(st.getName())
                            .id(st.getId())
                            .build())
                    .toList());

            String message = MessageFormatter.formatClarifyMessage(
                    topic.getName(),
                    null,
                    null,
                    "Great, choose a sub-topic:");


            bot.execute(MessageBuilder.buildEditMessageText(chatId, state.getActiveMessageId(), message));
            bot.execute(MessageBuilder.buildEditMessageReplyMarkup(chatId, state.getActiveMessageId(), TopicKeyboardFactory.build(topicDtoList, true, topicId)));
            stateManager.setBotState(telegramId, BotState.SELECTING_SUB_TOPIC);
        }

    }
}