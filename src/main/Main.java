package main;

import managers.FileBackedTaskManager;
import managers.TaskManager;
import managers.task.*;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        TaskManager taskManager = new FileBackedTaskManager(file);

        Epic epic1 = new Epic("Путешествия", "Планирование и организация путешествий по миру");
        taskManager.addEpic(epic1);

        Epic updatedEpic = new Epic("Путешествия", "Посещать новые страны каждый год");
        updatedEpic.setId(epic1.getId());
        taskManager.updateEpic(updatedEpic);

        Epic epic = taskManager.getEpicById(epic1.getId());

        Task task1 = new Task("Планирование путешествия", "Исследовать лучшие места для отдыха");
        Task task2 = new Task("Поездка в Италию", "Забронировать билеты и гостиницу");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Task updatedTask = new Task("Исправить план путешествия", "Проверить наличие скидок на билеты");
        updatedTask.setId(task1.getId());
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        taskManager.getTaskById(task1.getId());
        taskManager.removeTaskById(task1.getId());

        Subtask subtask1 = new Subtask("Исследовать достопримечательности", "Составить список интересных мест для посещения", epic.getId());
        Subtask subtask2 = new Subtask("Паковать вещи", "Упаковать все необходимое для поездки", epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Subtask updatedSubtask = new Subtask("Исследовать достопримечательности", "Добавить новые места", epic.getId());
        updatedSubtask.setId(subtask1.getId());
        updatedSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(updatedSubtask);

        taskManager.removeSubtaskById(subtask2.getId());
        taskManager.removeEpicById(epic.getId());

        taskManager.removeAllTasks();

        System.out.println("Файл сохранён: " + file.getAbsolutePath());
    }
}
