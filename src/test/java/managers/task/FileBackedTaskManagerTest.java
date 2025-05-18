package managers.task;

import managers.FileBackedTaskManager;
import managers.HistoryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для FileBackedTaskManager: проверка сериализации и десериализации.
 */
class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // файл изначально пуст
        return FileBackedTaskManager.loadFromFile(file);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(file.toPath());
    }

    @Test
    void shouldSaveAndLoadTasksWithNewFields() {
        // создаём задачи
        Task t1 = new Task("T1", "D1");
        t1.setDuration(Duration.ofMinutes(15));
        t1.setStartTime(LocalDateTime.of(2025, 5, 20, 9, 0));
        manager.addTask(t1);

        Epic epic = new Epic("E1", "DescE");
        manager.addEpic(epic);

        Subtask s1 = new Subtask(
                "S1", "D2", epic.getId(),
                Duration.ofMinutes(20), LocalDateTime.of(2025, 5, 20, 10, 0)
        );
        manager.addSubtask(s1);

        // проверяем, что файл не пуст
        assertTrue(file.length() > 0, "Файл должен содержать данные после сохранения");

        // загружаем менеджер из файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        // проверяем задачи
        Task lt1 = loaded.getTaskById(t1.getId());
        assertNotNull(lt1);
        assertEquals(t1.getDuration(), lt1.getDuration());
        assertEquals(t1.getStartTime(), lt1.getStartTime());

        // проверяем эпик (без времени)
        Epic le = loaded.getEpicById(epic.getId());
        assertNotNull(le);

        // проверяем подзадачу
        Subtask ls1 = loaded.getSubtaskById(s1.getId());
        assertNotNull(ls1);
        assertEquals(s1.getDuration(), ls1.getDuration());
        assertEquals(s1.getStartTime(), ls1.getStartTime());
    }

    @Test
    void shouldMaintainHistoryAfterLoad() {
        Task t = new Task("TaskForHistoryTest", "");
        manager.addTask(t);
        manager.getTaskById(t.getId());
        Epic e = new Epic("EpicForHistoryTest", "");
        manager.addEpic(e);
        manager.getEpicById(e.getId());
        Subtask s = new Subtask(
                "HistorySubtask", "", e.getId(),
                Duration.ofMinutes(5), LocalDateTime.of(2025,5,20,11,0)
        );
        manager.addSubtask(s);
        manager.getSubtaskById(s.getId());

        // load
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        List<Task> history = loaded.getHistory().getHistory();
        assertEquals(3, history.size(), "История должна сохраниться и загрузиться корректно");
    }
}
