package managers.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
        this.status = TaskStatus.NEW;
    }

    // Дополнительный конструктор с временем и продолжительностью
    public Subtask(String title, String description, int epicId, Duration duration, LocalDateTime startTime) {
        super(title, description);
        this.epicId = epicId;
        this.status = TaskStatus.NEW;
        this.duration = duration;
        this.startTime = startTime;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subtask subtask)) return false;
        return super.equals(o) &&
                epicId == subtask.epicId &&
                Objects.equals(duration, subtask.duration) &&
                Objects.equals(startTime, subtask.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId, duration, startTime);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                ", duration=" + (duration != null ? duration.toMinutes() + " minutes" : "null") +
                ", startTime=" + (startTime != null ? startTime : "null") +
                ", endTime=" + (getEndTime() != null ? getEndTime() : "null") +
                '}';
    }
}
