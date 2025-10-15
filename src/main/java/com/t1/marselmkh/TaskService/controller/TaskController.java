package com.t1.marselmkh.TaskService.controller;


import com.t1.marselmkh.TaskService.dto.CreateTaskRequest;
import com.t1.marselmkh.TaskService.dto.TaskResponse;
import com.t1.marselmkh.TaskService.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest taskRequest) {
        TaskResponse taskResponse = taskService.create(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll() {
        List<TaskResponse> taskResponses = taskService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(taskResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable @NotNull Long id) {
        TaskResponse taskResponse = taskService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponse> cancel(@PathVariable @NotNull  Long id) {
        TaskResponse taskResponse = taskService.cancel(id);
        return ResponseEntity.status(HttpStatus.OK).body(taskResponse);
    }
}