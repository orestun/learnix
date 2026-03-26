package com.learn.learnix.service;

import com.learn.learnix.domain.Question;
import com.learn.learnix.repository.QuestionRepository;
import com.learn.learnix.state.TopicType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubTopicService subTopicService;

    /**
     * Returns all questions for a topic, shuffled so each session feels fresh.
     * Choices within each question are already ordered by displayOrder (set in @OrderBy).
     */
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForTopic(Long topicId, TopicType topicType, int totalQuestions) {
        List<Question> questions;
        if (TopicType.TOPIC.equals(topicType)) {
            questions = questionRepository.findRandomByTopicId(topicId, totalQuestions);
        } else {
            long mainTopicId = subTopicService.getTopicIdBySubTopicId(topicId);
            questions = questionRepository.findRandomBySubTopicId(mainTopicId, topicId, totalQuestions);
        }

        Collections.shuffle(questions);
        return questions;
    }
}