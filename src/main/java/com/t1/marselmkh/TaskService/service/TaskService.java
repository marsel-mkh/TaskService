package com.t1.marselmkh.TaskService.service;

import com.t1.marselmkh.TaskService.dto.CreateTaskRequest;
import com.t1.marselmkh.TaskService.dto.TaskResponse;
import com.t1.marselmkh.TaskService.entity.Task;
import com.t1.marselmkh.TaskService.entity.TaskStatus;
import com.t1.marselmkh.TaskService.exception.TaskNotFoundException;
import com.t1.marselmkh.TaskService.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskSchedulerService taskSchedulerService;

    public TaskResponse create(CreateTaskRequest taskRequest) {
        Task task = Task.builder()
                .id(taskRepository.nextId())
                .description(taskRequest.getDescription())
                .durationSeconds(taskRequest.getDurationSeconds())
                .status(TaskStatus.IN_PROGRESS)
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .build();

        taskRepository.save(task);
        taskSchedulerService.scheduleCompletion(task);

        return mapToResponse(task);
    }

    public List<TaskResponse> getAll() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(String.format("Task with id %d not found", id)));

        return mapToResponse(task);
    }

    public TaskResponse cancel(Long id) {
        Task canceledTask = taskRepository.cancelTask(id);
        taskSchedulerService.cancelScheduledTask(id);

        return mapToResponse(canceledTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .description(task.getDescription())
                .durationSeconds(task.getDurationSeconds())
                .status(task.getStatus())
                .createdDate(task.getCreatedDate())
                .modifiedDate(task.getModifiedDate())
                .build();
    }
}