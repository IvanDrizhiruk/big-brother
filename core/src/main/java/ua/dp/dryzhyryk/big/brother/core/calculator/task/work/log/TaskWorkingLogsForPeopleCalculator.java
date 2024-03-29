package ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogs;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;

public class TaskWorkingLogsForPeopleCalculator {

	public Map<String, TaskWorkingLogs> calculatePersonsMetricsForPeopleFromTask(Task task, LocalDate startPeriod, LocalDate endPeriod) {

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
								entry -> toTaskWorkingLogMetrics(task, entry.getValue(), startPeriod, endPeriod)));
	}

	private TaskWorkingLogs toTaskWorkingLogMetrics(
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

		return TaskWorkingLogs.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.timeSpentByDays(timeSpentByDays)
				.timeSpentOnTaskByPeriodInMinutes(timeSpentOnTaskInMinutesByPeriod)
				.timeSpentOnTaskInMinutes(timeSpentOnTaskInMinutes)
				.build();
	}
}
