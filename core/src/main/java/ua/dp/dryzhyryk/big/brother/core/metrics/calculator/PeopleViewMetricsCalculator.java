package ua.dp.dryzhyryk.big.brother.core.metrics.calculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TimeSpentByDay;

public class PeopleViewMetricsCalculator {

	public List<PersonMetrics> calculateFor(List<Task> tasks) {

		Map<String, PersonMetrics> personMetricsByUser = tasks.stream()
				.flatMap(this::toPersonMetrics)
				.collect(
						Collectors.toMap(
								PersonMetrics::getPerson,
								Function.identity(),
								this::mergePersonMetricsForOnePerson)
				);

		return new ArrayList<>(personMetricsByUser.values());
	}

	private Stream<PersonMetrics> toPersonMetrics(Task task) {

		Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = task.getWorkLogs().stream()
				.collect(
						Collectors.groupingBy(
								TaskWorkLog::getPerson,
								Collectors.groupingBy(
										taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
										Collectors.summingInt(TaskWorkLog::getMinutesSpent))));

		return spendTimeByDayForPerson.entrySet().stream()
				.map(entry -> {
					TaskWorkingLogMetrics dailyTaskLogs = toTaskWorkingLogMetrics(entry.getValue(), task);
					List<TimeSpentByDay> totalTimeSpentByDay = dailyTaskLogs.getTimeSpentByDays();
					int totalTimeSpentInCurrentPeriodInMinutes = totalTimeSpentByDay
							.stream()
							.mapToInt(TimeSpentByDay::getTimeSpentMinutes)
							.sum();

					return PersonMetrics.builder()
							.person(entry.getKey())
							.dailyTaskLogs(Collections.singletonList(dailyTaskLogs))
							.totalTimeSpentByDay(totalTimeSpentByDay)
							.totalTimeSpentInCurrentPeriodInMinutes(totalTimeSpentInCurrentPeriodInMinutes)
							.build();
				});
	}

	private PersonMetrics mergePersonMetricsForOnePerson(PersonMetrics x, PersonMetrics y) {
		List<TaskWorkingLogMetrics> dailyTaskLogs = Stream.of(x.getDailyTaskLogs(), y.getDailyTaskLogs())
				.flatMap(List::stream)
				.collect(Collectors.toList());

		Collection<TimeSpentByDay> totalTimeSpentByDay = Stream.of(x.getTotalTimeSpentByDay(), y.getTotalTimeSpentByDay())
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(
						TimeSpentByDay::getDay,
						Function.identity(),
						(a, b) -> a.toBuilder()
								.timeSpentMinutes(a.getTimeSpentMinutes() + b.getTimeSpentMinutes())
								.build())).values();

		int totalTimeSpentInCurrentPeriodInMinutes =
				x.getTotalTimeSpentInCurrentPeriodInMinutes() + y.getTotalTimeSpentInCurrentPeriodInMinutes();

		return x.toBuilder()
				.dailyTaskLogs(dailyTaskLogs)
				.totalTimeSpentByDay(new ArrayList<>(totalTimeSpentByDay))
				.totalTimeSpentInCurrentPeriodInMinutes(totalTimeSpentInCurrentPeriodInMinutes)
				.build();
	}

	private TaskWorkingLogMetrics toTaskWorkingLogMetrics(Map<LocalDate, Integer> spentMinutesForDay, Task task) {

		int minutesSpent = spentMinutesForDay.values().stream().mapToInt(i -> i).sum();

		int originalEstimateMinutes = Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0);
		int timeSpentMinutes = Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0);

		float timeCoefficient =
				0 == timeSpentMinutes
						? 0
						: ((float) originalEstimateMinutes) / timeSpentMinutes;

		List<TimeSpentByDay> timeSpentByDays = spentMinutesForDay.entrySet().stream()
				.map(entry -> TimeSpentByDay.builder()
						.day(entry.getKey())
						.timeSpentMinutes(entry.getValue())
						.build())
				.collect(Collectors.toList());

		int totalTimeSpentByDaysInMinutes = timeSpentByDays
				.stream()
				.mapToInt(TimeSpentByDay::getTimeSpentMinutes)
				.sum();

		return TaskWorkingLogMetrics.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.taskExternalStatus(task.getStatus())
				.timeSpentByDays(timeSpentByDays)
				.totalTimeSpentByDaysInMinutes(totalTimeSpentByDaysInMinutes)
				.timeSpentMinutes(minutesSpent)
				.originalEstimateMinutes(originalEstimateMinutes)
				.timeCoefficient(timeCoefficient)
				.build();
	}
}
