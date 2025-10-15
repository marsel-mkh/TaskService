package com.t1.marselmkh.TaskService.repository;

import com.t1.marselmkh.TaskService.entity.Task;
import com.t1.marselmkh.TaskService.entity.TaskStatus;
import com.t1.marselmkh.TaskService.exception.TaskAlreadyDoneException;
import com.t1.marselmkh.TaskService.exception.TaskNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TaskRepository {

    private final ConcurrentMap<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public long nextId() {
        return idGenerator.getAndIncrement();
    }

    public Task save(Task task) {
        store.put(task.getId(), task);
        return task;
    }

    public Collection<Task> findAll() {
        return store.values();
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Task updateStatusAtomically(long id, TaskStatus expected, TaskStatus newStatus) {
        return store.compute(id, (key, oldTask) -> {
            if (oldTask == null) {
                throw new TaskNotFoundException(
                        String.format("Task with id %d  not found", id)
                );
            }
            if (oldTask.getStatus() != expected) {
                return oldTask;
            }

            return oldTask.withStatus(newStatus);
        });
    }

    public Task cancelTask(long id) {
        return store.compute(id, (key, oldTask) -> {
            if (oldTask == null) {
                throw new TaskNotFoundException(
                        String.format("Task with id %d  not found", id)
                );
            }

            if (oldTask.getStatus() == TaskStatus.DONE) {
                throw new TaskAlreadyDoneException(
                        String.format("Task with id %d already DONE and cannot be canceled", id)
                );
            }

            if (oldTask.getStatus() == TaskStatus.CANCELED) {
                return oldTask;
            }

            return oldTask.withStatus(TaskStatus.CANCELED);
        });
    }
}