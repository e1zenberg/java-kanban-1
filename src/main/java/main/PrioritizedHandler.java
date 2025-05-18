package main;

import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import managers.task.Task;

import java.io.IOException;
import java.util.List;

/**
 * Обработчик приоритетных задач (/prioritized):
 * – GET /prioritized — вернуть список задач, отсортированный по времени начала.
 *   Остальные методы не поддерживаются.
 */
public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Только GET /prioritized
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/prioritized".equals(exchange.getRequestURI().getPath())) {
            List<Task> list = manager.getPrioritizedTasks();
            sendText(exchange, 200, toJsonList(list));
        } else {
            sendNotFound(exchange);
        }
    }
}
