package managers;

import managers.task.*;

import java.util.*;
import java.util.stream.Collectors;

public abstract class TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected int nextId = 1;

    private int generateId() {
        return nextId++;
    }

    public void addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    public void addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Epic not found for subtask");
        }

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubTask(subtask);
        updateEpicStatus(epic);
    }

    public void updateTask(Task updatedTask) {
        if (tasks.containsKey(updatedTask.getId())) {
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    public void updateEpic(Epic updatedEpic) {
        if (epics.containsKey(updatedEpic.getId())) {
            Epic currentEpic = epics.get(updatedEpic.getId());
            currentEpic.setTitle(updatedEpic.getTitle());
            currentEpic.setDescription(updatedEpic.getDescription());
            updateEpicStatus(currentEpic);
        }
    }

    public void updateSubtask(Subtask updatedSubtask) {
        if (subtasks.containsKey(updatedSubtask.getId())) {
            subtasks.put(updatedSubtask.getId(), updatedSubtask);
            Epic epic = epics.get(updatedSubtask.getEpicId());
            updateEpicStatus(epic);
        }
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask subtask : new ArrayList<>(epic.getSubTasks())) {
                subtasks.remove(subtask.getId());
            }
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

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) getHistory().add(task);
        return task;
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) getHistory().add(epic);
        return epic;
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) getHistory().add(subtask);
        return subtask;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        return subtasks.values().stream()
                .filter(sub -> sub.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    protected void updateEpicStatus(Epic epic) {
        if (epic == null) return;
        epic.updateEpicStatus(); // делегируем логику Epic-у
    }

    public abstract HistoryManager getHistory();

    public void removeAllTasks() {
    }
}