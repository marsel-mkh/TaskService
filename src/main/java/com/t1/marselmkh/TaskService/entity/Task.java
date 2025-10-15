package com.t1.marselmkh.TaskService.entity;

import lombok.Builder;

import lombok.Value;

import java.time.LocalDateTime;


@Value
@Builder
public class Task {
    Long id;
    String description;
    Long durationSeconds;
    TaskStatus status;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;

    public Task withStatus(TaskStatus newStatus) {
        return Task.builder()
                .id(this.id)
                .description(this.description)
                .durationSeconds(this.durationSeconds)
                .status(newStatus)
                .createdDate(this.createdDate)
                .modifiedDate(LocalDateTime.now())
                .build();
    }
}