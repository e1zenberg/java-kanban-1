package managers.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic Title", "Epic Description");
        baseTime = LocalDateTime.of(2025, 5, 20, 9, 0);
    }

    @Test
    void statusAllNew() {
        // Нет подзадач -> статус NEW
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Добавляем подзадачи, все статус NEW по умолчанию
        Subtask s1 = new Subtask("Sub1", "Desc1", epic.getId(), Duration.ofMinutes(10), baseTime);
        Subtask s2 = new Subtask("Sub2", "Desc2", epic.getId(), Duration.ofMinutes(20), baseTime.plusHours(1));
        epic.addSubTask(s1);
        epic.addSubTask(s2);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void statusAllDone() {
        // Добавляем подзадачи со статусом DONE
        Subtask s1 = new Subtask("Sub1", "Desc1", epic.getId(), Duration.ofMinutes(15), baseTime);
        Subtask s2 = new Subtask("Sub2", "Desc2", epic.getId(), Duration.ofMinutes(25), baseTime.plusHours(2));
        s1.setStatus(TaskStatus.DONE);
        s2.setStatus(TaskStatus.DONE);
        epic.addSubTask(s1);
        epic.addSubTask(s2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void statusNewAndDone() {
        // Добавляем подзадачи со статусами NEW и DONE
        Subtask s1 = new Subtask("Sub1", "Desc1", epic.getId(), Duration.ofMinutes(15), baseTime);
        Subtask s2 = new Subtask("Sub2", "Desc2", epic.getId(), Duration.ofMinutes(25), baseTime.plusHours(1));
        s1.setStatus(TaskStatus.NEW);
        s2.setStatus(TaskStatus.DONE);
        epic.addSubTask(s1);
        epic.addSubTask(s2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void statusInProgressWhenAnyInProgress() {
        // Добавляем хотя бы одну подзадачу со статусом IN_PROGRESS
        Subtask s1 = new Subtask("Sub1", "Desc1", epic.getId(), Duration.ofMinutes(15), baseTime);
        Subtask s2 = new Subtask("Sub2", "Desc2", epic.getId(), Duration.ofMinutes(25), baseTime.plusHours(1));
        s1.setStatus(TaskStatus.IN_PROGRESS);
        s2.setStatus(TaskStatus.NEW);
        epic.addSubTask(s1);
        epic.addSubTask(s2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void timeCalculations() {
        // Расчёт времени: суммируем длительности и определяем крайние метки времени
        Subtask s1 = new Subtask("Sub1", "Desc1", epic.getId(), Duration.ofMinutes(30), baseTime);
        Subtask s2 = new Subtask("Sub2", "Desc2", epic.getId(), Duration.ofMinutes(45), baseTime.plusHours(2));
        epic.addSubTask(s1);
        epic.addSubTask(s2);

        // Длительность эпика — сумма длительностей подзадач
        assertEquals(Duration.ofMinutes(75), epic.getDuration());

        // Время старта — самое раннее начало подзадач
        assertEquals(baseTime, epic.getStartTime());

        // Время окончания — самое позднее завершение подзадач
        LocalDateTime expectedEnd = baseTime.plusMinutes(30).isAfter(baseTime.plusHours(2).plusMinutes(45))
                ? baseTime.plusMinutes(30)
                : baseTime.plusHours(2).plusMinutes(45);
        assertEquals(expectedEnd, epic.getEndTime());
    }
}
