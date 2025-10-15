package com.t1.marselmkh.TaskService.exception;

public class TaskAlreadyDoneException extends RuntimeException {
    public TaskAlreadyDoneException(String message) {
        super(message);
    }
}
