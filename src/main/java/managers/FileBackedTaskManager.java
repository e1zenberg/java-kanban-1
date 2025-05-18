package managers;

import managers.task.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    // ПЕРЕГРУЗКИ GET-методов, чтобы сохранять историю в файл
    @Override
    public Task getTaskById(int id) {
        Task t = super.getTaskById(id);
        save();
        return t;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic e = super.getEpicById(id);
        save();
        return e;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask s = super.getSubtaskById(id);
        save();
        return s;
    }

    // Проверка пересечения по времени (касание не считается пересечением)
    private boolean isTimeOverlap(Task newTask) {
        return getPrioritizedTasks().stream()
                .anyMatch(existing -> {
                    LocalDateTime es = existing.getStartTime();
                    LocalDateTime ee = existing.getEndTime();
                    LocalDateTime ns = newTask.getStartTime();
                    LocalDateTime ne = newTask.getEndTime();
                    if (es == null || ee == null || ns == null || ne == null) {
                        return false;
                    }
                    // пересечение: начало новой до конца существующей
                    // и конец новой после начала существующей
                    return ns.isBefore(ee) && ne.isAfter(es);
                });
    }

    // Загрузка задач и истории из файла
    private void load() {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isHistory = false;
            Map<Integer, Task> loaded = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    isHistory = true;
                    continue;
                }
                if (!isHistory) {
                    Task task = fromString(line);
                    int id = task.getId();
                    if (task instanceof Epic) {
                        super.addEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        super.addSubtask((Subtask) task);
                    } else {
                        super.addTask(task);
                    }
                    loaded.put(id, task);
                } else {
                    // секция истории
                    for (String idStr : line.split(",")) {
                        Task t = loaded.get(Integer.parseInt(idStr));
                        if (t != null) {
                            getHistory().add(t);
                        }
                    }
                }
            }
            // пересчитать эпики
            getAllEpics().forEach(Epic::updateEpicStatus);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка при загрузке из файла: " + file.getName(), e);
        }
    }

    // Сохранение задач и истории в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8))) {
            for (Task t : getAllTasks()) {
                writer.write(taskToString(t));
                writer.newLine();
            }
            for (Epic e : getAllEpics()) {
                writer.write(taskToString(e));
                writer.newLine();
            }
            for (Subtask s : getAllSubtasks()) {
                writer.write(taskToString(s));
                writer.newLine();
            }
            writer.newLine();
            writer.write(historyToString(getHistory()));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка при сохранении в файл: " + file.getName(), e);
        }
    }

    private static String historyToString(HistoryManager history) {
        return history.getHistory().stream()
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));
    }


    // Преобразовать одну CSV-строку в объект Task/Epic/Subtask
    private static Task fromString(String csvLine) {
        // Формат: id,type,title,status,description,duration,startTime[,epicId]
        String[] fields = csvLine.split(",", -1);

        int id           = Integer.parseInt(fields[0]);
        TaskType type    = TaskType.valueOf(fields[1]);
        String title     = fields[2];
        TaskStatus status= TaskStatus.valueOf(fields[3]);
        String desc      = fields[4];
        Duration dur     = fields[5].isEmpty()
                ? null
                : Duration.ofMinutes(Long.parseLong(fields[5]));
        LocalDateTime start = fields[6].isEmpty()
                ? null
                : LocalDateTime.parse(fields[6]);

        return switch (type) {
            case TASK -> {
                Task t = new Task(title, desc);
                t.setId(id);
                t.setStatus(status);
                t.setDuration(dur);
                t.setStartTime(start);
                yield t;
            }
            case EPIC -> {
                Epic e = new Epic(title, desc);
                e.setId(id);
                e.setStatus(status);
                yield e;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[7]);
                Subtask s = new Subtask(title, desc, epicId);
                s.setId(id);
                s.setStatus(status);
                s.setDuration(dur);
                s.setStartTime(start);
                yield s;
            }
        };
    }

    // Преобразование задачи в строку
    private static String taskToString(Task task) {
        String base = String.join(",",
                String.valueOf(task.getId()),
                getType(task).name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                task.getDuration() != null
                        ? String.valueOf(task.getDuration().toMinutes())
                        : "",
                task.getStartTime() != null
                        ? task.getStartTime().toString()
                        : ""
        );
        if (task instanceof Subtask st) {
            return base + "," + st.getEpicId();
        }
        return base;
    }

    private static TaskType getType(Task task) {
        if (task instanceof Epic)   return TaskType.EPIC;
        if (task instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }


    // Переопределения CRUD-методов для сохранения после изменений
    @Override
    public void addTask(Task task) {
        if (isTimeOverlap(task)) {
            throw new IllegalArgumentException(
                    "Время задачи пересекается с существующей задачей.");
        }
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (isTimeOverlap(subtask)) {
            throw new IllegalArgumentException(
                    "Невозможно добавить подзадачу: её интервал выполнения пересекается с интервалом существующей задачи.");
        }
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        if (isTimeOverlap(task)) {
            throw new IllegalArgumentException(
                    "Обновлённая задача пересекается по времени с другой задачей.");
        }
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (isTimeOverlap(subtask)) {
            throw new IllegalArgumentException(
                    "Обновлённая подзадача пересекается по времени с другой задачей.");
        }
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }
}
