package com.learn.learnix.bot.keyboard;

import com.learn.learnix.domain.Topic;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the inline keyboard shown after /start.
 * Each button carries callback data "topic:<id>" so TopicHandler
 * can parse the selected topic ID directly from the callback.
 *
 * Layout: one topic per row for clarity — topic names can be long
 * and two-column layouts truncate unpredictably across devices.
 *
 * Example:
 *   [ Java Core        ]
 *   [ Spring Framework ]
 *   [ SQL & Databases  ]
 */
public class TopicKeyboardFactory {

    private static final String TOPIC_PREFIX = "topic:";

    public static InlineKeyboardMarkup build(List<Topic> topics) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Topic topic : topics) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(topic.getName())
                    .callbackData(TOPIC_PREFIX + topic.getId())
                    .build();

            rows.add(List.of(button));
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
