package main;

import com.sun.net.httpserver.HttpServer;
import managers.TaskManager;
import managers.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Главный класс приложения.
 * – Создаёт HttpServer на порту 8080,
 * – Регистрирует все контексты (/tasks, /subtasks, /epics, /history, /prioritized),
 * – Предоставляет методы start() и stop() для управления сервером.
 */
public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    // manager Менеджер задач — InMemory или любой другой, передаваемый через Managers
    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks",      new TasksHandler(manager));
        server.createContext("/subtasks",   new SubtasksHandler(manager));
        server.createContext("/epics",      new EpicsHandler(manager));
        server.createContext("/history",    new HistoryHandler(manager));
        server.createContext("/prioritized",new PrioritizedHandler(manager));
    }

    // Запускает HTTP-сервер в фоновом потоке
    public void start() {
        server.start();
        System.out.println("HTTP Task Server запущен на порту " + PORT);
    }

    // Останавливает сервер сразу (0 секунд задержки)
    public void stop() {
        server.stop(0);
    }

    // Точка входа для запуска из командной строки
    public static void main(String[] args) {
        try {
            TaskManager manager = Managers.getDefault();
            HttpTaskServer httpServer = new HttpTaskServer(manager);
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
