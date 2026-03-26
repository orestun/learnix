package com.learn.learnix.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TopicDto {
    private String name;
    private long id;
}
