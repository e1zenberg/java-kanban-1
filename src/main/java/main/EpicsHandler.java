package main;

import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import managers.task.Epic;
import managers.task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Обработчик для эпиков (/epics):
 * – GET   /epics                — список всех эпиков,
 * – GET   /epics/{id}           — один эпик,
 * – GET   /epics/{id}/subtasks  — список подзадач конкретного эпика,
 * – POST  /epics                — создать или обновить эпик,
 * – DELETE /epics               — удалить все эпики,
 * – DELETE /epics/{id}          — удалить конкретный эпик.
 */
public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                // Все эпики
                if ("/epics".equals(path)) {
                    List<Epic> list = manager.getAllEpics();
                    sendText(exchange, 200, toJsonList(list));
                }
                // Один эпик по id
                else if (path.matches("/epics/\\d+$")) {
                    int id = Integer.parseInt(path.substring("/epics/".length()));
                    Epic e = manager.getEpicById(id);
                    if (e == null) sendNotFound(exchange);
                    else           sendText(exchange, 200, gson.toJson(e));
                }
                // Подзадачи эпика
                else if (path.matches("/epics/\\d+/subtasks$")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    List<Subtask> subs = manager.getSubtasksForEpic(id);
                    sendText(exchange, 200, toJsonList(subs));
                }
                else {
                    sendNotFound(exchange);
                }

            } else if ("POST".equalsIgnoreCase(method) && "/epics".equals(path)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic.getId() == 0) {
                    manager.addEpic(epic);
                } else {
                    manager.updateEpic(epic);
                }
                sendText(exchange, 201, "");

            } else if ("DELETE".equalsIgnoreCase(method)) {
                if ("/epics".equals(path)) {
                    manager.removeAllEpics();
                    sendText(exchange, 200, "");
                }
                else if (path.startsWith("/epics/")) {
                    int id = Integer.parseInt(path.substring("/epics/".length()));
                    manager.removeEpicById(id);
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
