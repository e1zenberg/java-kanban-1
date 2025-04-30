package test;

import static org.junit.jupiter.api.Assertions.*;

import managers.InMemoryTaskManager;
import managers.InMemoryHistoryManager;
import managers.task.Epic;
import managers.task.Subtask;
import managers.task.Task;
import org.junit.jupiter.api.*;

import java.util.List;

public class TaskManagerTest {
    private InMemoryTaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    public void setUp() {
        taskManager = new InMemoryTaskManager();
        task = new Task("Задача 1", "Описание задачи 1");
        epic = new Epic("Эпик 1", "Описание эпика");
        taskManager.addEpic(epic);
        subtask = new Subtask("Подзадача 1", "Описание подзадачи", epic.getId());
    }

    @Test
    public void testAddTask() {
        taskManager.addTask(task);
        assertEquals(1, taskManager.getAllTasks().size());
        assertEquals(task.getTitle(), taskManager.getTaskById(task.getId()).getTitle());
    }

    @Test
    public void testAddEpic() {
        assertEquals(1, taskManager.getAllEpics().size());
        assertEquals(epic.getTitle(), taskManager.getEpicById(epic.getId()).getTitle());
    }

    @Test
    public void testRemoveTask() {
        taskManager.addTask(task);
        taskManager.removeTaskById(task.getId());
        assertNull(taskManager.getTaskById(task.getId()));
    }

    @Test
    public void testAddSubtask() {
        taskManager.addSubtask(subtask);
        List<Subtask> subtasks = taskManager.getSubtasksForEpic(epic.getId());
        assertEquals(1, subtasks.size());
        assertEquals(subtask.getTitle(), subtasks.get(0).getTitle());
    }

    @Test
    public void testRemoveSubtask() {
        taskManager.addSubtask(subtask);
        taskManager.removeSubtaskById(subtask.getId());
        assertNull(taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    public void testUpdateEpic() {
        epic.setTitle("Обновленный эпик");
        taskManager.updateEpic(epic);
        assertEquals("Обновленный эпик", taskManager.getEpicById(epic.getId()).getTitle());
    }

    @Test
    public void testUpdateTask() {
        taskManager.addTask(task);
        task.setTitle("Обновленная задача");
        taskManager.updateTask(task);
        assertEquals("Обновленная задача", taskManager.getTaskById(task.getId()).getTitle());
    }

    @Test
    public void testUpdateSubtask() {
        taskManager.addSubtask(subtask);
        subtask.setTitle("Обновленная подзадача");
        taskManager.updateSubtask(subtask);
        assertEquals("Обновленная подзадача", taskManager.getSubtaskById(subtask.getId()).getTitle());
    }

    @Test
    public void testHistoryManager() {
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.addSubtask(subtask);
        taskManager.getSubtaskById(subtask.getId());

        List<Task> history = taskManager.getHistory().getHistory();
        assertEquals(3, history.size());
    }

    @Test
    public void testHistoryManagerFullSize() {
        for (int i = 1; i <= 15; i++) {
            Task newTask = new Task("Задача " + i, "Описание задачи " + i);
            taskManager.addTask(newTask);
            taskManager.getTaskById(newTask.getId());
        }
        List<Task> history = taskManager.getHistory().getHistory();
        assertEquals(15, history.size());
    }

    @Test
    public void testInMemoryHistoryManager_AddAndRemoveTasks() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("T1", "Desc");
        task1.setId(1);
        Task task2 = new Task("T2", "Desc");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());

        historyManager.remove(1);
        history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    public void testSubtaskRemovalRemovesReferenceFromEpic() {
        taskManager.addSubtask(subtask);
        taskManager.removeSubtaskById(subtask.getId());
        assertTrue(taskManager.getSubtasksForEpic(epic.getId()).isEmpty());
    }

    @Test
    public void testSubtaskStoredUnderOriginalEpicId() {
        taskManager.addSubtask(subtask);
        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    public void testChangingTaskIdAfterAddDoesNotAffectManager() {
        taskManager.addTask(task);
        int originalId = task.getId();
        task.setId(999);
        assertNull(taskManager.getTaskById(999));
        assertNotNull(taskManager.getTaskById(originalId));
    }
}
