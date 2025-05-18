package main;

import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import managers.task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Обработчик для CRUD-операций с основными задачами через путь /tasks:
 * – GET   /tasks          — вернуть список всех задач,
 * – GET   /tasks/{id}     — вернуть задачу по её идентификатору,
 * – POST  /tasks          — создать новую задачу или обновить существующую,
 * – DELETE /tasks         — удалить все задачи,
 * – DELETE /tasks/{id}    — удалить задачу по идентификатору.
 */
public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                // Список всех задач
                if ("/tasks".equals(path)) {
                    List<Task> tasks = manager.getAllTasks();
                    sendText(exchange, 200, toJsonList(tasks));
                }
                // Конкретная задача по id
                else if (path.startsWith("/tasks/")) {
                    int id = Integer.parseInt(path.substring("/tasks/".length()));
                    Task t = manager.getTaskById(id);
                    if (t == null) sendNotFound(exchange);
                    else           sendText(exchange, 200, gson.toJson(t));
                }
                else {
                    sendNotFound(exchange);
                }

            } else if ("POST".equalsIgnoreCase(method) && "/tasks".equals(path)) {
                // Создать или обновить задачу
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == 0) {
                    // У нового объекта id == 0, значит создать
                    manager.addTask(task);
                } else {
                    // Иначе обновить
                    manager.updateTask(task);
                }
                sendText(exchange, 201, "");

            } else if ("DELETE".equalsIgnoreCase(method)) {
                // Удалить все
                if ("/tasks".equals(path)) {
                    manager.removeAllTasks();
                    sendText(exchange, 200, "");
                }
                // Удалить по id
                else if (path.startsWith("/tasks/")) {
                    int id = Integer.parseInt(path.substring("/tasks/".length()));
                    manager.removeTaskById(id);
                    sendText(exchange, 200, "");
                }
                else {
                    sendNotFound(exchange);
                }

            } else {
                // Неподдерживаемый метод/путь
                sendNotFound(exchange);
            }

        } catch (NumberFormatException e) {
            // Некорректный формат идентификатора
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            // Ошибка при пересечении задач по времени
            sendHasInteractions(exchange);
        } catch (Exception e) {
            // Любая неожиданная ошибка
            sendServerError(exchange);
        }
    }
}
