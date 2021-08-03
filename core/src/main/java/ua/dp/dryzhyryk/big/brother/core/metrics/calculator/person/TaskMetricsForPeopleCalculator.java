package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

public class TaskMetricsForPeopleCalculator {

	public List<PersonMetrics> toPersonsMetricsForTask(Task task, LocalDate startPeriod, LocalDate endPeriod) {

		Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = groupSpendTimeByDayForPerson(task);

		return spendTimeByDayForPerson.entrySet().stream()
				.map(entry -> {
					String person = entry.getKey();
					Map<LocalDate, Integer> spentMinutesForDay = entry.getValue();

					TaskWorkingLogMetrics dailyTaskLogs = toTaskWorkingLogMetrics(task, spentMinutesForDay, startPeriod, endPeriod);

					return PersonMetrics.builder()
							.person(person)
							.dailyTaskWorkingLogMetrics(List.of(dailyTaskLogs))
							.build();
				})
				.collect(Collectors.toList());
	}

	private Map<String, Map<LocalDate, Integer>> groupSpendTimeByDayForPerson(Task task) {
		return task.getWorkLogs().stream()
				.collect(
						Collectors.groupingBy(
								TaskWorkLog::getPerson,
								Collectors.groupingBy(
										taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
										Collectors.summingInt(TaskWorkLog::getMinutesSpent))));
	}

	private TaskWorkingLogMetrics toTaskWorkingLogMetrics(
			Task task,
			Map<LocalDate, Integer> spentMinutesForDay,
			LocalDate startPeriod,
			LocalDate endPeriod) {

		List<TimeSpentByDay> timeSpentByDays = spentMinutesForDay.entrySet().stream()
				.filter(entry -> !entry.getKey().isBefore(startPeriod) && entry.getKey().isBefore(endPeriod))
				.map(entry -> TimeSpentByDay.builder()
						.day(entry.getKey())
						.timeSpentMinutes(entry.getValue())
						.build())
				.sorted(Comparator.comparing(TimeSpentByDay::getDay))
				.collect(Collectors.toList());

		int timeSpentOnTaskInMinutes = spentMinutesForDay.values().stream()
				.mapToInt(Integer::intValue)
				.sum();

		int timeSpentOnTaskInMinutesByPeriod = timeSpentByDays.stream()
				.mapToInt(TimeSpentByDay::getTimeSpentMinutes)
				.sum();

		return TaskWorkingLogMetrics.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.timeSpentByDays(timeSpentByDays)
				.timeSpentOnTaskInMinutesByPeriod(ValueWithValidation.valueWithNotEvaluatedValidationStatus(timeSpentOnTaskInMinutesByPeriod))
				.timeSpentOnTaskInMinutes(ValueWithValidation.valueWithNotEvaluatedValidationStatus(timeSpentOnTaskInMinutes))
				.build();
	}
}
