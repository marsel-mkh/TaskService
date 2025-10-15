package com.t1.marselmkh.TaskService.service;

import com.t1.marselmkh.TaskService.entity.Task;
import com.t1.marselmkh.TaskService.entity.TaskStatus;
import com.t1.marselmkh.TaskService.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSchedulerService {

    @Value("${pool-size:4}")
    private int poolSize;

    private ScheduledExecutorService scheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final TaskRepository taskRepository;

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(poolSize);
        log.info("Инициализация планировщика задач с пулом потоков размером {}", poolSize);
    }

    public void scheduleCompletion(Task task) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                taskRepository.updateStatusAtomically(task.getId(), TaskStatus.IN_PROGRESS, TaskStatus.DONE);
            } catch (Exception ex) {
                log.error("Ошибка при обновлении статуса задачи {}: {}", task.getId(), ex.getMessage(), ex);
            } finally {
                scheduledTasks.remove(task.getId());
            }
        }, task.getDurationSeconds(), TimeUnit.SECONDS);

        scheduledTasks.put(task.getId(), future);
    }

    public void cancelScheduledTask(Long taskId) {
        scheduledTasks.computeIfPresent(taskId, (key, future) -> {
            future.cancel(false);
            return null;
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Планировщик не завершился за 5 секунд, выполняется принудительное завершение");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Прерывание при завершении планировщика, выполняется принудительное завершение", e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}