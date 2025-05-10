package managers;

import managers.task.Task;

import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    @Override
    public void add(Task task) {

    }

    @Override
    public List<Task> getHistory() {
        return List.of();
    }
}
