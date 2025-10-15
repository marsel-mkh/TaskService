package com.t1.marselmkh.TaskService;

import com.jayway.jsonpath.JsonPath;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Создание задачи")
    void testCreateTask() throws Exception {
        String requestJson = """
                    {"description": "Тест создания", "durationSeconds": 3}
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Тест создания")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("Получение списка всех задач")
    void testGetAllTasks() throws Exception {
        String requestJson = """
                    {"description": "Тест списка", "durationSeconds": 1}
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("Ожидание перехода задачи в статус DONE")
    void testTaskBecomesDone() throws Exception {
        String requestJson = """
                    {"description": "Тест перехода", "durationSeconds": 2}
                """;

        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andReturn();

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long taskId = idNumber.longValue();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    MvcResult getResult = mockMvc.perform(get("/tasks/" + taskId)).andReturn();
                    String body = getResult.getResponse().getContentAsString();
                    return JsonPath.read(body, "$.status").equals("DONE");
                });

        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    @DisplayName("Получение задачи по ID и проверка статуса")
    void testGetTaskById() throws Exception {
        String requestJson = """
                    {"description": "Проверка по ID", "durationSeconds": 1}
                """;

        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andReturn();

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long taskId = idNumber.longValue();

        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) taskId)))
                .andExpect(jsonPath("$.description", is("Проверка по ID")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("Попытка отменить выполненную задачу — ошибка")
    void testCancelDoneTaskThrowsError() throws Exception {
        String requestJson = """
                    {"description": "Нельзя отменить DONE", "durationSeconds": 1}
                """;

        MvcResult result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andReturn();

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long taskId = idNumber.longValue();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    MvcResult getResult = mockMvc.perform(get("/tasks/" + taskId)).andReturn();
                    String body = getResult.getResponse().getContentAsString();
                    return JsonPath.read(body, "$.status").equals("DONE");
                });

        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isBadRequest());
    }
}