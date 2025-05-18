package main;

import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import managers.task.Task;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик истории просмотров (/history):
 * – GET /history — вернуть список задач в порядке обращения пользователя.
 *   Остальные методы не поддерживаются.
 */
public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Поддерживаем только GET /history
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/history".equals(exchange.getRequestURI().getPath())) {
            List<Task> history = manager.getHistory().getHistory();
            sendText(exchange, 200, toJsonList(history));
        } else {
            sendNotFound(exchange);
        }
    }
}
