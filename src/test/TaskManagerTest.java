package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import managers.InMemoryTaskManager;
import managers.task.Epic;
import managers.task.Subtask;
import managers.task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {

    private InMemoryTaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    // Подготовка данных перед каждым тестом
    @BeforeEach
    public void setUp() {
        taskManager = new InMemoryTaskManager();
        task = new Task("Задача 1", "Описание задачи 1");
        epic = new Epic("Эпик 1", "Описание эпика");
        subtask = new Subtask("Подзадача 1", "Описание подзадачи", epic.getId());
    }

    // Проверяем добавления задачи
    @Test
    public void testAddTask() {
        taskManager.addTask(task);
        assertEquals(1, taskManager.getAllTasks().size(), "Задача не добавлена в список");
        assertEquals(task, taskManager.getTaskById(task.getId()), "Задача не найдена по ID");
    }

    // Проверяем добавления эпика
    @Test
    public void testAddEpic() {
        taskManager.addEpic(epic);
        assertEquals(1, taskManager.getAllEpics().size(), "Эпик не добавлен в список");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Эпик не найден по ID");
    }


    // Проверяем удаления задачи
    @Test
    public void testRemoveTask() {
        taskManager.addTask(task);
        taskManager.removeTaskById(task.getId());
        assertNull(taskManager.getTaskById(task.getId()), "Задача не удалена по ID");
    }


    // Проверяем добавления подзадачи
    @Test
    public void testAddSubtask() {
        Epic epic = new Epic("Эпик1", "Описание для эпика");
        epic.setId(1);
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача для эпика", "Описание для подзадачи", 1);
        subtask.setEpicId(epic.getId());
        taskManager.addSubtask(subtask);

        List<Subtask> subtasks = taskManager.getSubtasksForEpic(epic.getId());
        assertEquals(1, subtasks.size(), "Подзадача не добавлена к эпикам");

        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Подзадача не найдена по ID");
    }

    // Проверяем удаления подзадачи
    @Test
    public void testRemoveSubtask() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        taskManager.removeSubtaskById(subtask.getId());
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не удалена по ID");
    }

    // Проверяем обновления эпика
    @Test
    public void testUpdateEpic() {
        taskManager.addEpic(epic);
        epic.setTitle("Обновленный эпик");
        taskManager.updateEpic(epic);
        assertEquals("Обновленный эпик", taskManager.getEpicById(epic.getId()).getTitle(), "Эпик не обновился");
    }

    // Проверяем обновления задачи
    @Test
    public void testUpdateTask() {
        taskManager.addTask(task);
        task.setTitle("Обновленная задача");
        taskManager.updateTask(task);
        assertEquals("Обновленная задача", taskManager.getTaskById(task.getId()).getTitle(), "Задача не обновилась");
    }


    // Проверяем управления историей просмотров
    @Test
    public void testHistoryManager() {
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId());
        taskManager.addEpic(epic);
        taskManager.getEpicById(epic.getId());
        taskManager.addSubtask(subtask);
        taskManager.getSubtaskById(subtask.getId());

        List<Task> history = taskManager.getHistory().getHistory();
        assertEquals(3, history.size(), "Неверное количество элементов в истории просмотров");
        assertTrue(history.contains(task), "История не содержит задачу");
        assertTrue(history.contains(epic), "История не содержит эпик");
        assertTrue(history.contains(subtask), "История не содержит подзадачу");
    }

    // Проверяем обновления подзадачи
    @Test
    public void testUpdateSubtask() {
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        subtask.setTitle("Обновленная подзадача");
        taskManager.updateSubtask(subtask);
        assertEquals("Обновленная подзадача", taskManager.getSubtaskById(subtask.getId()).getTitle(), "Подзадача не обновилась");
    }



    // Проверяем ограничения размера истории
    @Test
    public void testHistoryManagerSizeLimit() {
        // Добавляем больше 10 задач
        for (int i = 1; i <= 15; i++) {
            Task newTask = new Task("Задача " + i, "Описание задачи " + i);
            taskManager.addTask(newTask);
            taskManager.getTaskById(newTask.getId());
        }

        List<Task> history = taskManager.getHistory().getHistory();
        assertEquals(10, history.size(), "История должна содержать только 10 последних задач");
        Assertions.assertEquals("Задача 6", history.get(0).getTitle(), "В истории не тот элемент на первом месте");
        Assertions.assertEquals("Задача 15", history.get(9).getTitle(), "В истории не тот элемент на последнем месте");
    }
}
//не понимаю