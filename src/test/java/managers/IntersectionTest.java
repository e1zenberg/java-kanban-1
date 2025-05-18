package managers;

import managers.task.Task;
import managers.task.Subtask;
import managers.task.Epic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {
    private InMemoryTaskManager manager;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        baseTime = LocalDateTime.of(2025, 5, 20, 9, 0);
    }

    @Test
    void shouldAddNonOverlappingTasks() {
        // Создаём первую задачу с временем 9:00–9:10
        Task t1 = new Task("Task1", "Desc");
        t1.setDuration(Duration.ofMinutes(10));
        t1.setStartTime(baseTime);
        assertDoesNotThrow(() -> manager.addTask(t1));

        // Создаём вторую задачу начинается ровно после первой — без пересечения
        Task t2 = new Task("Task2", "Desc");
        t2.setDuration(Duration.ofMinutes(5));
        t2.setStartTime(baseTime.plusMinutes(10));
        assertDoesNotThrow(() -> manager.addTask(t2));
    }

    @Test
    void shouldDetectOverlapBetweenTasks() {
        // Первая задача 9:00–9:20
        Task t1 = new Task("Task1", "");
        t1.setDuration(Duration.ofMinutes(20));
        t1.setStartTime(baseTime);
        manager.addTask(t1);

        // Вторая задача 9:10–9:15 — пересекается
        Task t2 = new Task("Task2", "");
        t2.setDuration(Duration.ofMinutes(5));
        t2.setStartTime(baseTime.plusMinutes(10));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> manager.addTask(t2));
        assertTrue(ex.getMessage().contains("пересекается"));
    }

    @Test
    void shouldIgnoreTasksWithoutTime() {
        // Задача без времени
        Task t = new Task("NoTime", "");
        // duration и startTime не заданы
        assertDoesNotThrow(() -> manager.addTask(t));
    }

    @Test
    void shouldDetectOverlapBetweenSubtaskAndTask() {
        // Создаём эпик
        Epic epic = new Epic("Epic", "");
        manager.addEpic(epic);

        // Добавляем подзадачу 10:00–10:30
        Subtask s = new Subtask("Sub", "", epic.getId());
        s.setDuration(Duration.ofMinutes(30));
        s.setStartTime(baseTime.plusHours(1));
        manager.addSubtask(s);

        // Добавляем обычную задачу 10:15–10:20 — пересечение с подзадачей
        Task t = new Task("Task", "");
        t.setDuration(Duration.ofMinutes(5));
        t.setStartTime(baseTime.plusHours(1).plusMinutes(15));
        assertThrows(IllegalArgumentException.class, () -> manager.addTask(t));
    }
}
