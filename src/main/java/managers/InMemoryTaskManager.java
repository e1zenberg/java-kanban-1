package managers;

import managers.task.*;
import java.util.*;
import java.util.stream.*;
import java.time.LocalDateTime;

public class InMemoryTaskManager extends TaskManager {
    // коллекции для хранения сущностей
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // множество задач и подзадач с ненулевым startTime, поддерживаемое в порядке startTime
    private final NavigableSet<Task> prioritized = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );

    private int generateId() {
        return nextId++;
    }

    // Проверка на пересечение по времени с уже существующими задачами
    private boolean isTimeOverlap(Task newTask) {
        return prioritized.stream()
                .anyMatch(existing -> {
                    LocalDateTime es = existing.getStartTime();
                    LocalDateTime ee = existing.getEndTime();
                    LocalDateTime ns = newTask.getStartTime();
                    LocalDateTime ne = newTask.getEndTime();

                    if (es == null || ee == null || ns == null || ne == null) {
                        // нет времени — не учитываем
                        return false;
                    }
                    // пересекаются тогда, когда начало одной раньше конца другой
                    // и конец одной позже начала другой:
                    return ns.isBefore(ee) && ne.isAfter(es);
                });
    }

    // Получение задач и подзадач, отсортированных по времени начала — O(n)
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }

    @Override
    public void addTask(Task task) {
        if (isTimeOverlap(task)) {
            throw new IllegalArgumentException("Время задачи пересекается с существующей задачей.");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritized.add(task);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeAllTasks() {
        // очищаем историю и множество приоритетов
        tasks.keySet().forEach(historyManager::remove);
        prioritized.removeIf(tasks::containsKey);
        tasks.clear();
    }

    @Override
    public void removeTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            historyManager.remove(id);
            prioritized.remove(removed);
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        epic.updateEpicStatus();  // пересчёт статуса, времени и длительности
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubTasks().stream()
                    .map(Subtask::getId)
                    .forEach(subId -> {
                        subtasks.remove(subId);
                        historyManager.remove(subId);
                        // удаляем из приоритетов
                        prioritized.removeIf(t -> t.getId() == subId);
                    });
            historyManager.remove(id);
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (isTimeOverlap(subtask)) {
            throw new IllegalArgumentException("Время подзадачи пересекается с другой задачей.");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subtask);
            epic.updateEpicStatus();  // пересчёт статуса, времени и длительности
        }
        if (subtask.getStartTime() != null) {
            prioritized.add(subtask);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Optional.ofNullable(epics.get(subtask.getEpicId()))
                    .ifPresent(epic -> {
                        epic.removeSubtask(subtask);
                        epic.updateEpicStatus();  // пересчёт статуса, времени и длительности
                    });
            historyManager.remove(id);
            prioritized.remove(subtask);
        }
    }

    @Override
    public List<Subtask> getSubtasksForEpic(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubTasks)
                .orElse(Collections.emptyList());
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (isTimeOverlap(updatedTask)) {
            throw new IllegalArgumentException("Обновлённая задача пересекается по времени с другой задачей.");
        }
        Task existing = tasks.get(updatedTask.getId());
        if (existing != null) {
            // удаляем старую версию из приоритетов, если была
            prioritized.remove(existing);
            existing.setTitle(updatedTask.getTitle());
            existing.setDescription(updatedTask.getDescription());
            existing.setStatus(updatedTask.getStatus());
            existing.setDuration(updatedTask.getDuration());
            existing.setStartTime(updatedTask.getStartTime());
            // добавляем обратно при наличии startTime
            if (existing.getStartTime() != null) {
                prioritized.add(existing);
            }
        }
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        Epic epic = epics.get(updatedEpic.getId());
        if (epic != null) {
            epic.setTitle(updatedEpic.getTitle());
            epic.setDescription(updatedEpic.getDescription());
            epic.updateEpicStatus();  // пересчёт статуса, времени и длительности
        }
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (isTimeOverlap(updatedSubtask)) {
            throw new IllegalArgumentException("Обновлённая подзадача пересекается по времени с другой задачей.");
        }
        Subtask sub = subtasks.get(updatedSubtask.getId());
        if (sub != null) {
            prioritized.remove(sub);
            sub.setTitle(updatedSubtask.getTitle());
            sub.setDescription(updatedSubtask.getDescription());
            sub.setStatus(updatedSubtask.getStatus());
            sub.setDuration(updatedSubtask.getDuration());
            sub.setStartTime(updatedSubtask.getStartTime());

            Optional.ofNullable(epics.get(sub.getEpicId()))
                    .ifPresent(epic -> epic.updateEpicStatus());  // пересчёт статуса, времени и длительности
            if (sub.getStartTime() != null) {
                prioritized.add(sub);
            }
        }
    }

    @Override
    public HistoryManager getHistory() {
        return historyManager;
    }

    // Удаляет все эпики и связанные с ними подзадачи.
    // Также очищает историю этих задач.
    @Override
    public void removeAllEpics() {
        epics.values().stream()
                .flatMap(e -> e.getSubTasks().stream().map(Subtask::getId))
                .forEach(id -> {
                    subtasks.remove(id);
                    historyManager.remove(id);
                    prioritized.removeIf(t -> t.getId() == id);
                });

        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    // Удаляет все подзадачи из всех эпиков и общей коллекции.
    // Обновляет статусы эпиков и очищает историю подзадач.
    @Override
    public void removeAllSubtasks() {
        subtasks.values().stream()
                .map(Subtask::getId)
                .forEach(id -> {
                    Subtask sub = subtasks.get(id);
                    Optional.ofNullable(epics.get(sub.getEpicId()))
                            .ifPresent(epic -> {
                                epic.removeSubtask(sub);
                                epic.updateEpicStatus();  // пересчёт статуса, времени и длительности
                            });
                    historyManager.remove(id);
                });

        subtasks.clear();
        prioritized.removeIf(t -> t instanceof Subtask);
    }
}
