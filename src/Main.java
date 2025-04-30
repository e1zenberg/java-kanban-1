import managers.HistoryManager;
import managers.task.Task;
import managers.task.Epic;
import managers.task.Subtask;
import managers.TaskManager;
import managers.task.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager() {
            @Override
            public HistoryManager getHistory() {
                return null;
            }
        };

        Epic epic1 = new Epic("Путешествия", "Планирование и организация путешествий по миру");

        taskManager.addEpic(epic1);
        System.out.println(epic1);
        Epic updatedEpic = new Epic("Путешествия", "Посещать новые страны каждый год");
        updatedEpic.setId(epic1.getId());
        taskManager.updateEpic(updatedEpic);
        System.out.println("Обновленный эпик " + updatedEpic);

        Epic epic = taskManager.getEpicById(1);
        if (epic != null) {
            System.out.println("Эпик: " + epic);
        } else {
            System.out.println("Эпик с таким ID не найден.");
        }

        Task task1 = new Task("Планирование путешествия", "Исследовать лучшие места для отдыха");
        Task task2 = new Task("Поездка в Италию", "Забронировать билеты и гостиницу");

        taskManager.addTask(task1);
        System.out.println(task1);
        Task updatedTask = new Task("Исправить план путешествия", "Проверить наличие скидок на билеты");
        updatedTask.setId(task1.getId());
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(updatedTask);
        System.out.println("Обновленная задача " + updatedTask);

        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.removeTaskById(task1.getId());

        System.out.println("Вывод задач после удаления по айди " + task1.getId() +
                " оставшиеся задачи: " + taskManager.getAllTasks());

        assert epic != null;
        Subtask subtask1 = new Subtask("Исследовать достопримечательности", "Составить список интересных мест для посещения", epic.getId());
        Subtask subtask2 = new Subtask("Паковать вещи", "Упаковать все необходимое для поездки", epic.getId());

        taskManager.addSubtask(subtask1);
        Subtask updatedSubtask = new Subtask("Исследовать достопримечательности", "Добавить новые места для путешествий", epic.getId());
        updatedSubtask.setId(subtask1.getId());
        updatedSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(updatedSubtask);

        taskManager.addSubtask(subtask2);

        System.out.println("Подзадачи для эпика: " + taskManager.getSubtasksForEpic(epic.getId()));

        System.out.println("Получение подзадачи по айди: " + taskManager.getSubtaskById(subtask1.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.addSubtask(subtask1);

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.addSubtask(subtask2);
        System.out.println("Статус эпиков после обновления статуса подзадач: "
                + taskManager.getEpicById(epic1.getId()).getStatus());

        taskManager.removeSubtaskById(subtask2.getId());
        System.out.println("Подзадачи после удаления второй подзадачи: " + taskManager.getAllSubtasks());

        epic.clearSubtask();
        taskManager.removeEpicById(epic.getId());
        System.out.println("Все эпики после удаления эпика 1: " + taskManager.getAllEpics());

        taskManager.removeAllTasks();
        System.out.println("Все задачи после удаления всех задач: " + taskManager.getAllTasks());
    }
}