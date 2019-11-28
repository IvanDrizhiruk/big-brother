package ua.dp.dryzhyryk.big.brother.core.metrics.calculator;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TasksRootViewMetricsCalculator {

    public Map<String, TaskMetrics> calculateFor(List<Task> tasksTree) {
        return tasksTree.stream()
                .map(this::calculateMetricsForTask)
                .collect(Collectors.toMap(
                        TaskMetrics::getTaskId,
                        Function.identity()));
    }

    private TaskMetrics calculateMetricsForTask(Task task) {

        AtomicInteger originalEstimateMinutes = new AtomicInteger(Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0));
        AtomicInteger timeSpentMinutes = new AtomicInteger(Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0));

        task.getSubTasks()
                .forEach(subTask -> {
                    originalEstimateMinutes.addAndGet(Optional.ofNullable(subTask.getOriginalEstimateMinutes()).orElse(0));
                    timeSpentMinutes.addAndGet(Optional.ofNullable(subTask.getTimeSpentMinutes()).orElse(0));
                });

        float timeCoefficient =
                0 == timeSpentMinutes.get()
                        ? 0
                        : ((float) originalEstimateMinutes.get()) / timeSpentMinutes.get();

        TaskTimeMetrics timeMetrics = TaskTimeMetrics.builder()
                .originalEstimateMinutes(originalEstimateMinutes.get())
                .timeSpentMinutes(timeSpentMinutes.get())
                .timeCoefficient(timeCoefficient)
                .build();

        return TaskMetrics.builder()
                .taskId(task.getId())
                .timeMetrics(timeMetrics)
                .build();
    }
}
