package com.learn.learnix.bot.keyboard;

import com.learn.learnix.domain.dto.TopicDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TopicKeyboardFactory {

    private static final String TOPIC_PREFIX = "topic:";
    private static final String SUB_TOPIC_PREFIX = "subtopic:";
    private static final String ALL_QUESTIONS_TEXT = "ALL";
    private static final String ALL_QUESTIONS_PREFIX = "all:";

    public static InlineKeyboardMarkup build(List<TopicDto> topics, boolean isSubTopic, long mainTopicId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (TopicDto topic : topics) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(topic.getName())
                    .callbackData((isSubTopic ? SUB_TOPIC_PREFIX : TOPIC_PREFIX) + topic.getId())
                    .build();

            rows.add(List.of(button));
        }

        if (isSubTopic) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(ALL_QUESTIONS_TEXT)
                    .callbackData(ALL_QUESTIONS_PREFIX + mainTopicId)
                    .build();
            rows.add(List.of(button));
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
