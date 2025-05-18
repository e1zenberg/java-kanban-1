package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import managers.Managers;
import managers.TaskManager;
import managers.task.Epic;
import managers.task.Subtask;
import managers.task.Task;
import managers.task.TaskStatus;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private HttpTaskServer server;

    // Gson с адаптерами для Duration и LocalDateTime
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class,
                    (JsonSerializer<Duration>) (src, type, ctx) ->
                            new JsonPrimitive(src == null ? null : src.toString()))
            .registerTypeAdapter(Duration.class,
                    (JsonDeserializer<Duration>) (json, type, ctx) -> {
                        try {
                            return json == null || json.getAsString().isEmpty()
                                    ? null
                                    : Duration.parse(json.getAsString());
                        } catch (Exception e) {
                            throw new JsonParseException(e);
                        }
                    })
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src == null ? null : src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> {
                        try {
                            return json == null || json.getAsString().isEmpty()
                                    ? null
                                    : LocalDateTime.parse(json.getAsString());
                        } catch (Exception e) {
                            throw new JsonParseException(e);
                        }
                    })
            .create();

    private static final String BASE = "http://localhost:8080";

    @BeforeEach
    void setUp() throws IOException {
        // Для тестов используем InMemoryTaskManager
        TaskManager manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    private HttpClient client() {
        return HttpClient.newHttpClient();
    }

    @Test
    void getEmptyTasks() throws Exception {
        HttpResponse<String> r = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, r.statusCode());
        assertEquals("[]", r.body());
    }

    @Test
    void testGetNonexistentTask() throws Exception {
        HttpResponse<String> r = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/1"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, r.statusCode());
    }

    @Test
    void postAndGetTask() throws Exception {
        Task t = new Task("T1", "D1");
        t.setDuration(Duration.ofMinutes(5));
        t.setStartTime(LocalDateTime.of(2025,5,20,10,0));
        String json = gson.toJson(t);

        HttpResponse<String> post = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, post.statusCode());

        HttpResponse<String> getAll = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Task[] arr = gson.fromJson(getAll.body(), Task[].class);
        assertEquals(1, arr.length);
        assertEquals("T1", arr[0].getTitle());
    }

    @Test
    void updateTask() throws Exception {
        // создаём
        Task t = new Task("Old", "OldDesc");
        client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t)))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        // обновляем
        Task updated = new Task("New", "NewDesc");
        updated.setId(1);
        updated.setStatus(TaskStatus.IN_PROGRESS);
        HttpResponse<String> postUpd = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(updated)))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, postUpd.statusCode());

        HttpResponse<String> getOne = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/1"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        Task tOut = gson.fromJson(getOne.body(), Task.class);
        assertEquals("New", tOut.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, tOut.getStatus());
    }

    @Test
    void deleteTaskById() throws Exception {
        Task t = new Task("X", "x");
        client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t)))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );

        HttpResponse<String> del = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/1"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, del.statusCode());

        HttpResponse<String> get404 = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks/1"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, get404.statusCode());
    }

    @Test
    void deleteAllTasks() throws Exception {
        client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new Task("A",""))))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
        client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new Task("B",""))))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );

        HttpResponse<String> delAll = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, delAll.statusCode());

        HttpResponse<String> getEmpty = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/tasks"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals("[]", getEmpty.body());
    }

    @Test
    void getEmptyAndNonexistentSubtasks() throws Exception {
        HttpResponse<String> list = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/subtasks"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, list.statusCode());
        assertEquals("[]", list.body());

        HttpResponse<String> one404 = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/subtasks/1"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, one404.statusCode());
    }

    @Test
    void getEmptyAndNonexistentEpicsAndSubtasksList() throws Exception {
        HttpResponse<String> list = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/epics"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, list.statusCode());
        assertEquals("[]", list.body());

        HttpResponse<String> e404 = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/epics/1"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, e404.statusCode());

        HttpResponse<String> subs = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/epics/1/subtasks"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, subs.statusCode());
        assertEquals("[]", subs.body());
    }

    @Test
    void historyAndPrioritizedInitiallyEmpty() throws Exception {
        HttpResponse<String> hist = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/history"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, hist.statusCode());
        assertEquals("[]", hist.body());

        HttpResponse<String> prio = client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE + "/prioritized"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, prio.statusCode());
        assertEquals("[]", prio.body());
    }
}
