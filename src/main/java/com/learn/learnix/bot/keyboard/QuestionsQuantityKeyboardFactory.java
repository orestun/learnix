package com.learn.learnix.bot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QuestionsQuantityKeyboardFactory {

    private static final String[] QUESTIONS_QUANTITY = {"10", "20", "35", "50"};
    private static final String QUANTITY_PREFIX = "quantity:";

    public static InlineKeyboardMarkup build() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < QUESTIONS_QUANTITY.length; i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(QUESTIONS_QUANTITY[i])
                    .callbackData(QUANTITY_PREFIX + QUESTIONS_QUANTITY[i])
                    .build());
            row.add(InlineKeyboardButton.builder()
                    .text(QUESTIONS_QUANTITY[i+1])
                    .callbackData(QUANTITY_PREFIX + QUESTIONS_QUANTITY[i+1])
                    .build());

            rows.add(row);
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
