package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskMetricsForPersonCalculator {
    public Map<String, TaskMetrics> calculateTaskMetricsForPerson(Task task, LocalDate startPeriod, LocalDate endPeriod) {
        Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = task.getWorkLogs().stream()
                .collect(
                        Collectors.groupingBy(
                                TaskWorkLog::getPerson,
                                Collectors.groupingBy(
                                        taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
                                        Collectors.summingInt(TaskWorkLog::getMinutesSpent))));

        return spendTimeByDayForPerson.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> toTaskMetrics(task, entry.getValue(), startPeriod, endPeriod)));
    }

    private TaskMetrics toTaskMetrics(
            Task task,
            Map<LocalDate, Integer> spentMinutesForDay,
            LocalDate startPeriod,
            LocalDate endPeriod) {

        int realSpendTimeByPersonInMinutes = spentMinutesForDay.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(startPeriod) && entry.getKey().isBefore(endPeriod))
                .mapToInt(Map.Entry::getValue)
                .sum();

        //TODO calculate
        float timeCoefficient = 0f;

        return TaskMetrics.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskExternalStatus(task.getStatus())
                .originalEstimateInMinutes(task.getOriginalEstimateMinutes())
                .realSpendTimeInMinutes(task.getTimeSpentMinutes())
                .timeSpentOnTaskByPeriodInMinutes(realSpendTimeByPersonInMinutes)
                .timeCoefficient(timeCoefficient)
                .build();
    }
}
