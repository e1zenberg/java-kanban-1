package managers.task;

import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Общие тесты CRUD-операций и истории для любых реализаций TaskManager.
public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    //Возвращает экземпляр конкретной реализации менеджера.
    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Задача1", "Описание");
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.of(2025, 5, 20, 10, 0));
        manager.addTask(task);

        Task fetched = manager.getTaskById(task.getId());
        assertNotNull(fetched, "Добавленная задача должна вернуться");
        assertEquals(task, fetched, "Содержимое задачи должно совпадать");
    }

    @Test
    void shouldAddAndGetEpicAndSubtask() {
        Epic epic = new Epic("Эпик1", "Описание эпика");
        manager.addEpic(epic);
        Subtask sub = new Subtask(
                "Под1", "Desc",
                epic.getId(),
                Duration.ofMinutes(15),
                LocalDateTime.of(2025, 5, 20, 12, 0)
        );
        manager.addSubtask(sub);

        Epic fetchedEpic = manager.getEpicById(epic.getId());
        Subtask fetchedSub = manager.getSubtaskById(sub.getId());
        assertNotNull(fetchedEpic, "Добавленный эпик должен быть возвращен методом getEpicById");
        assertNotNull(fetchedSub, "Добавленная подзадача должна быть возвращена методом getSubtaskById");
        assertEquals(epic.getId(), fetchedSub.getEpicId(),
                "Подзадача должна ссылаться на правильный эпик");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Old", "OldDesc");
        manager.addTask(task);
        task.setTitle("New");
        task.setDescription("NewDesc");
        manager.updateTask(task);

        Task updated = manager.getTaskById(task.getId());
        assertEquals("New", updated.getTitle(), "Заголовок должен обновиться");
        assertEquals("NewDesc", updated.getDescription(), "Описание должно обновиться");
    }

    @Test
    void shouldRemoveTaskById() {
        Task task = new Task("ToRemove", "");
        manager.addTask(task);
        manager.removeTaskById(task.getId());
        assertNull(manager.getTaskById(task.getId()), "Задача должна быть удалена");
    }

    @Test
    void shouldRemoveAllTasks() {
        manager.addTask(new Task("A", ""));
        manager.addTask(new Task("B", ""));
        manager.removeAllTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void shouldReturnEmptySubtasksForUnknownEpic() {
        List<Subtask> subs = manager.getSubtasksForEpic(999);
        assertTrue(subs.isEmpty(), "Для неизвестного эпика список подзадач должен быть пуст");
    }

    @Test
    void shouldRecordHistoryOnAccess() {
        Task task = new Task("H1", "");
        manager.addTask(task);
        manager.getTaskById(task.getId());
        assertTrue(
                manager.getHistory().getHistory().contains(task),
                "История должна содержать задачу после её получения"
        );
    }
}
