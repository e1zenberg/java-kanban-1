package managers;

import managers.task.Epic;
import managers.task.Subtask;
import managers.task.Task;
import managers.task.TaskStatus;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
public abstract class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    private int generateId() {
        return nextId++;
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void removeEpicById(int id) {
        epics.remove(id);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subtask);
            updateEpicStatus(epic);
        }
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask);
                updateEpicStatus(epic);
            }
        }
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubTasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            boolean allDone = true;
            boolean allNew = true;

            for (Subtask subTask : epic.getSubTasks()) {
                if (subTask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
                if (subTask.getStatus() != TaskStatus.NEW) {
                    allNew = false;
                }
            }

            if (allDone) {
                epic.setStatus(TaskStatus.DONE);
            } else if (allNew) {
                epic.setStatus(TaskStatus.NEW);
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            }
        }
    }

    public void updateTask(Task updatedTask) {
        tasks.computeIfPresent(updatedTask.getId(), (id, task) -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setStatus(updatedTask.getStatus());
            return task;
        });
    }

    public void updateEpic(Epic updatedEpic) {
        epics.computeIfPresent(updatedEpic.getId(), (id, epic) -> {
            epic.setTitle(updatedEpic.getTitle());
            epic.setDescription(updatedEpic.getDescription());
            return epic;
        });
    }

    public void updateSubtask(Subtask updatedSubtask) {
        subtasks.computeIfPresent(updatedSubtask.getId(), (id, subtask) -> {
            subtask.setTitle(updatedSubtask.getTitle());
            subtask.setDescription(updatedSubtask.getDescription());
            subtask.setStatus(updatedSubtask.getStatus());
            updateEpicStatus(epics.get(subtask.getEpicId()));
            return subtask;
        });
    }

    public abstract HistoryManager getHistory();
}
