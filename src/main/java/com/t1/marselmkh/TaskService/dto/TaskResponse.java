package com.t1.marselmkh.TaskService.dto;

import com.t1.marselmkh.TaskService.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String description;
    private Long durationSeconds;
    private TaskStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}