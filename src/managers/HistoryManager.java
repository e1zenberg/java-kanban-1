package managers;

import managers.task.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(int id); // добавили метод
    List<Task> getHistory();
}