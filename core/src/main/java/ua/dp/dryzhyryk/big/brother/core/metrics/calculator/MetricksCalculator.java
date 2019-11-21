package ua.dp.dryzhyryk.big.brother.core.metrics.calculator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.DayWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PersonWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;

public class MetricksCalculator {

	public Map<String, TaskMetrics> calculateFor(List<Task> tasksTree) {
		return tasksTree.stream()
				.flatMap(rootTask -> {
					TaskMetrics rootTaskMetrics = calculateMetricsForTask(rootTask);

					Stream<TaskMetrics> subtasksMetrics = rootTask.getSubTasks().stream()
							.map(this::calculateMetricsForTask);

					return Stream.concat(Stream.of(rootTaskMetrics), subtasksMetrics);
				})
				.collect(Collectors.toMap(
						TaskMetrics::getTaskId,
						Function.identity()));
	}

	private TaskMetrics calculateMetricsForTask(Task task) {
		TaskTimeMetrics timeMetrics = calculateTaskTimeMetrics(task);

		List<DayWorkLog> dailyWorkLog = aggregateWorklog(task.getWorkLogs());

		return TaskMetrics.builder()
				.taskId(task.getId())
				.timeMetrics(timeMetrics)
				.dailyWorkLog(dailyWorkLog)
				.build();
	}

	private TaskTimeMetrics calculateTaskTimeMetrics(Task task) {

		int originalEstimateMinutes = Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0);
		int remainingEstimateMinutes = Optional.ofNullable(task.getRemainingEstimateMinutes()).orElse(0);
		int timeSpentMinutes = Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0);

		float timeCoefficient =
				0 == timeSpentMinutes
						? 0
						: ((float) originalEstimateMinutes) / timeSpentMinutes;

		return TaskTimeMetrics.builder()
				.originalEstimateMinutes(originalEstimateMinutes)
				.remainingEstimateMinutes(remainingEstimateMinutes)
				.timeSpentMinutes(timeSpentMinutes)
				.timeCoefficient(timeCoefficient)
				.build();
	}

	private List<DayWorkLog> aggregateWorklog(List<TaskWorkLog> workLogs) {

		Map<LocalDate, Map<String, Integer>> timeByDateAndUser = workLogs.stream()
				.collect(Collectors.groupingBy(
						log -> log.getStartDateTime().toLocalDate(),
						Collectors.groupingBy(
								TaskWorkLog::getPerson,
								Collectors.summingInt(TaskWorkLog::getMinutesSpent))
				));

		return timeByDateAndUser.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.map(entry -> DayWorkLog.builder()
						.workDate(entry.getKey())
						.personWorkLogs(toPersonsWorkLog(entry.getValue()))
						.build())
				.collect(Collectors.toList());

	}

	private List<PersonWorkLog> toPersonsWorkLog(Map<String, Integer> minutesSpentByPerson) {
		return minutesSpentByPerson.entrySet().stream()
				.map(e -> PersonWorkLog.builder()
						.person(e.getKey())
						.minutesSpent(e.getValue())
						.build())
				.collect(Collectors.toList());
	}

}
