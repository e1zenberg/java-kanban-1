package managers.task;

import java.util.ArrayList;
import java.util.List;
public class Epic extends Task {
    private final List<Subtask> subtasks;

    public  Epic(String title, String description) {
        super(title, description);
        this.status = TaskStatus.NEW;
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubTasks() {
        return subtasks;
    }

    public void addSubTask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }

    public void clearSubtask() {
        subtasks.clear();
    }
}