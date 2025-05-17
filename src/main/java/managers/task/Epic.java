package managers.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

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
            this.duration = Duration.ZERO;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        boolean allDone = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);
        boolean allNew  = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);

        if (allDone) {
            this.status = TaskStatus.DONE;
        } else if (allNew) {
            this.status = TaskStatus.NEW;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }

        updateTimeParameters();
    }

    private void updateTimeParameters() {
        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                LocalDateTime subStart = subtask.getStartTime();
                LocalDateTime subEnd   = subtask.getEndTime();

                if (earliestStart == null || subStart.isBefore(earliestStart)) {
                    earliestStart = subStart;
                }
                if (latestEnd == null || subEnd.isAfter(latestEnd)) {
                    latestEnd = subEnd;
                }

                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        this.startTime = earliestStart;
        this.endTime   = latestEnd;
        this.duration  = totalDuration;
    }

    @Override
    public Duration getDuration() {
        updateEpicStatus();
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        updateEpicStatus();
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        updateEpicStatus();
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Epic epic)) return false;
        return super.equals(o)
                && Objects.equals(subtasks, epic.subtasks)
                && Objects.equals(getDuration(), epic.getDuration())
                && Objects.equals(getStartTime(), epic.getStartTime())
                && Objects.equals(getEndTime(), epic.getEndTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                subtasks,
                getDuration(),
                getStartTime(),
                getEndTime()
        );
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskCount=" + subtasks.size() +
                ", duration=" + (getDuration() != null
                ? getDuration().toMinutes() + " minutes"
                : "null") +
                ", startTime=" + (getStartTime() != null
                ? getStartTime()
                : "null") +
                ", endTime=" + (getEndTime() != null
                ? getEndTime()
                : "null") +
                '}';
    }
}
