package main;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Базовый HTTP-обработчик, предоставляющий общую логику:
 * – Инициализация Gson с адаптерами для работы с java.time (Duration, LocalDateTime),
 * – Утилиты для отправки JSON-ответа с нужным HTTP-кодом,
 * – Утилита для ручной сериализации списков в JSON-массив.
 */
public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    /**
     * manager Экземпляр менеджера задач, к методам которого
     *                будут обращаться все наследники.
     */
    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;

        // Настраиваем Gson:
        // – сериализация Duration в строку ISO-8601, например "PT15M",
        // – десериализация Duration из такой строки обратно в объект,
        // – аналогично для LocalDateTime в формате "YYYY-MM-DDTHH:MM:SS".
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class,
                        (JsonSerializer<Duration>) (src, type, ctx) ->
                                new JsonPrimitive(src == null ? null : src.toString()))
                .registerTypeAdapter(Duration.class,
                        (JsonDeserializer<Duration>) (json, type, ctx) -> {
                            try {
                                if (json == null || json.getAsString().isEmpty()) return null;
                                return Duration.parse(json.getAsString());
                            } catch (Exception e) {
                                throw new JsonParseException(String.format("Не удалось распарсить Duration: %s", json.getAsString()), e);
                            }
                        })
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                                new JsonPrimitive(src == null ? null : src.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
                            try {
                                if (json == null || json.getAsString().isEmpty()) return null;
                                return LocalDateTime.parse(json.getAsString());
                            } catch (Exception e) {
                                throw new JsonParseException(String.format("Не удалось распарсить LocalDateTime: %s", json.getAsString()), e);
                            }
                        })
                .create();
    }

    //Отправляет ответ в JSON-формате.
    protected void sendText(HttpExchange exchange, int statusCode, String responseText)
            throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Отправить пустой ответ с кодом 404 Not Found
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, 404, "");
    }

    // Отправить пустой ответ с кодом 406 Not Acceptable (пересечение по времени)
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, 406, "");
    }

    // Отправить пустой ответ с кодом 500 Internal Server Error
    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, 500, "");
    }

    /**
     * Преобразует список объектов в JSON-массив.
     * Серийное оборачивание каждого элемента через gson.toJson(item).
     */
    protected <T> String toJsonList(List<T> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (T item : list) {
            if (!first) sb.append(",");
            sb.append(gson.toJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
