package com.learn.learnix.bot.keyboard;

import com.learn.learnix.domain.Choice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChoicesKeyboardFactory {

    private static final String CHOICE_PREFIX = "c:";
    private static final String SUBMIT_DATA   = "submit";
    private static final String EXIT   = "exit";
    private static final String SELECTED_MARK = "✅ ";
    private static final String UNSELECTED_MARK = "☐  ";

    /**
     * @param choices     ordered list of choices from the Question entity
     * @param selectedIds set of choice IDs the user has toggled ON so far —
     *                    pass an empty set when first rendering the question
     */
    public static InlineKeyboardMarkup build(List<Choice> choices, Set<Long> selectedIds) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        for (Choice choice : choices) {
            row.add(buildChoiceButton(choice, selectedIds));
        }
        rows.add(row);

        // Submit row — always last, full width
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("Finish test")
                        .callbackData(EXIT)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Submit answer")
                        .callbackData(SUBMIT_DATA)
                        .build()
        ));


        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    private static InlineKeyboardButton buildChoiceButton(Choice choice,
                                                          Set<Long> selectedIds) {
        boolean selected = selectedIds.contains(choice.getId());
        String  label    = (selected ? SELECTED_MARK : UNSELECTED_MARK) + (char)(96 + choice.getDisplayOrder());

        return InlineKeyboardButton.builder()
                .text(label)
                .callbackData(CHOICE_PREFIX + choice.getId())
                .build();
    }
}
