package com.learn.learnix.service;

import com.learn.learnix.domain.SubTopic;
import com.learn.learnix.repository.SubTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubTopicService {

    private final SubTopicRepository subTopicRepository;

    public Set<SubTopic> getAllSubTopicsByTopicId(long topicId) {
        return subTopicRepository.findSubTopicsByTopicId(topicId);
    }

    public SubTopic getSubTopicById(long id) {
        return subTopicRepository.findById(id).orElse(null);
    }

    public long getTopicIdBySubTopicId(Long topicId) {
        return subTopicRepository.getTopicIdBySubTopicId(topicId);
    }
}
