package com.learn.learnix.repository;

import com.learn.learnix.domain.SubTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {

    @Query("SELECT s FROM SubTopic s WHERE s.topic.id = :topicId")
    Set<SubTopic> findSubTopicsByTopicId(@Param("topicId") long topicId);

    @Query("SELECT s.topic.id FROM SubTopic s WHERE s.id = :topicId")
    long getTopicIdBySubTopicId(Long topicId);
}
