package com.t1.marselmkh.TaskService;

import com.t1.marselmkh.TaskService.dto.CreateTaskRequest;
import com.t1.marselmkh.TaskService.dto.TaskResponse;
import com.t1.marselmkh.TaskService.entity.TaskStatus;
import com.t1.marselmkh.TaskService.exception.TaskAlreadyDoneException;
import com.t1.marselmkh.TaskService.service.TaskService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskServiceTest {


    @Autowired
    private TaskService taskService;

    private Long taskId;

    @BeforeEach
    void setup() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setDescription("Тестовая задача");
        request.setDurationSeconds(2L);
        taskId = taskService.create(request).getId();
    }

    @Test
    @DisplayName("Создать задачу.")
    void testCreateTask() {
        TaskResponse task = taskService.getById(taskId);
        assertNotNull(task.getId());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals("Тестовая задача", task.getDescription());
    }

    @Test
    @DisplayName("Получить списоĸ всех задач; проверить, что задача создана.")
    void testGetAllTasks() {
        List<TaskResponse> allTasks = taskService.getAll();
        assertTrue(allTasks.stream().anyMatch(t -> t.getId().equals(taskId)));
    }

    @Test
    @DisplayName("Дождаться поĸа задача перейдет в статус DONE.")
    void testTaskBecomesDone() {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> taskService.getById(taskId).getStatus() == TaskStatus.DONE);

        TaskResponse doneTask = taskService.getById(taskId);
        assertEquals(TaskStatus.DONE, doneTask.getStatus());
    }

    @Test
    @DisplayName("Получить задачу по ID и проверить, что статус изменился.")
    void testGetTaskById() {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> taskService.getById(taskId).getStatus() == TaskStatus.DONE);

        TaskResponse task = taskService.getById(taskId);
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(taskId, task.getId());
    }

    @Test
    @DisplayName("Попробовать отменить задачу и получить ошибĸу.")
    void testCancelDoneTaskThrowsException() {
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> taskService.getById(taskId).getStatus() == TaskStatus.DONE);

        Exception exception = assertThrows(TaskAlreadyDoneException.class,
                () -> taskService.cancel(taskId));

        assertThrows(TaskAlreadyDoneException.class, () -> taskService.cancel(taskId));
    }
}