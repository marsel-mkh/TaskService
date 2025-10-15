package com.t1.marselmkh.TaskService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Duration cannot be null")
    @PositiveOrZero(message = "Duration must be zero or positive")
    private Long durationSeconds;
}