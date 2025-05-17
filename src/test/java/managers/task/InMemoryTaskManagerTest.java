package managers.task;

import managers.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Специфичные тесты для InMemoryTaskManager.
 */
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void changingTaskIdAfterAddDoesNotAffectManager() {
        Task task = new Task("T1", "Desc");
        manager.addTask(task);
        int originalId = task.getId();
        task.setId(999);
        assertNull(manager.getTaskById(999),
                "Менеджер не должен находить задачу с изменённым идентификатором");
        assertNotNull(manager.getTaskById(originalId),
                "Менеджер должен находить задачу по оригинальному идентификатору");
    }

    @Test
    void subtaskRemovalRemovesReferenceFromEpic() {
        Epic epic = new Epic("E1", "Desc");
        manager.addEpic(epic);
        Subtask sub = new Subtask(
                "S1", "Desc", epic.getId(),
                Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 20, 14, 0)
        );
        manager.addSubtask(sub);
        manager.removeSubtaskById(sub.getId());
        assertTrue(
                manager.getSubtasksForEpic(epic.getId()).isEmpty(),
                "После удаления подзадачи ссылка на неё должна исчезнуть у эпика"
        );
    }

    @Test
    void subtaskStoredUnderOriginalEpicId() {
        Epic epic = new Epic("E1", "Desc");
        manager.addEpic(epic);
        Subtask sub = new Subtask(
                "S1", "Desc", epic.getId(),
                Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 20, 14, 0)
        );
        manager.addSubtask(sub);
        assertEquals(
                epic.getId(), sub.getEpicId(),
                "Подзадача должна хранить идентификатор эпика, к которому принадлежит"
        );
    }
}
