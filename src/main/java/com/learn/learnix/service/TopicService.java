package com.learn.learnix.service;

import com.learn.learnix.domain.Topic;
import com.learn.learnix.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    /**
     * Returns only active topics — what the user sees in the selection keyboard.
     */
    @Transactional(readOnly = true)
    public List<Topic> getActiveTopics() {
        return topicRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Topic getById(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found: id=" + topicId));
    }
}
