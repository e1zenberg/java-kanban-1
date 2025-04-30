package managers.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
        this.status = TaskStatus.NEW;
    }

    public List<Subtask> getSubTasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubTask(Subtask subtask) {
        if (!subtasks.contains(subtask)) {
            subtasks.add(subtask);
            subtask.setEpicId(this.getId());
            updateEpicStatus();
        }
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        updateEpicStatus();
    }

    public void clearSubtask() {
        subtasks.clear();
        updateEpicStatus();
    }

    public void updateEpicStatus() {
        if (subtasks.isEmpty()) {
            this.status = TaskStatus.NEW;
            return;
        }

        boolean allDone = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);
        boolean allNew = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);

        if (allDone) {
            this.status = TaskStatus.DONE;
        } else if (allNew) {
            this.status = TaskStatus.NEW;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic epic)) return false;
        return super.equals(o) && Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtasks);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskCount=" + subtasks.size() +
                '}';
    }
}