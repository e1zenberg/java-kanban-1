package main;

import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import managers.task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Обработчик для подзадач (/subtasks):
 * – GET   /subtasks        — список всех подзадач,
 * – GET   /subtasks/{id}   — одна подзадача,
 * – POST  /subtasks        — создать или обновить подзадачу,
 * – DELETE /subtasks       — удалить все,
 * – DELETE /subtasks/{id}  — удалить конкретную.
 */
public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if ("/subtasks".equals(path)) {
                    List<Subtask> list = manager.getAllSubtasks();
                    sendText(exchange, 200, toJsonList(list));
                }
                else if (path.startsWith("/subtasks/")) {
                    int id = Integer.parseInt(path.substring("/subtasks/".length()));
                    Subtask s = manager.getSubtaskById(id);
                    if (s == null) sendNotFound(exchange);
                    else           sendText(exchange, 200, gson.toJson(s));
                }
                else {
                    sendNotFound(exchange);
                }

            } else if ("POST".equalsIgnoreCase(method) && "/subtasks".equals(path)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Subtask sub = gson.fromJson(body, Subtask.class);
                if (sub.getId() == 0) {
                    manager.addSubtask(sub);
                } else {
                    manager.updateSubtask(sub);
                }
                sendText(exchange, 201, "");

            } else if ("DELETE".equalsIgnoreCase(method)) {
                if ("/subtasks".equals(path)) {
                    manager.removeAllSubtasks();
                    sendText(exchange, 200, "");
                }
                else if (path.startsWith("/subtasks/")) {
                    int id = Integer.parseInt(path.substring("/subtasks/".length()));
                    manager.removeSubtaskById(id);
                    sendText(exchange, 200, "");
                }
                else {
                    sendNotFound(exchange);
                }

            } else {
                sendNotFound(exchange);
            }

        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }
}
