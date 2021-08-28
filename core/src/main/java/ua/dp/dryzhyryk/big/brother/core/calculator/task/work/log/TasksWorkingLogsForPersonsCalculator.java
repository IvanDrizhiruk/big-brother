package ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogs;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

public class TasksWorkingLogsForPersonsCalculator {

	private final TaskWorkingLogsForPeopleCalculator taskWorkingLogsForPeopleCalculator;
	private final TaskWorkingLogsForPeopleValidator taskWorkingLogsForPeopleValidator;

	public TasksWorkingLogsForPersonsCalculator(
			TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator,
			TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator) {
		this.taskWorkingLogsForPeopleCalculator = taskMetricsForPeopleCalculator;
		this.taskWorkingLogsForPeopleValidator = taskMetricsForPeopleValidator;
	}

	public List<TasksWorkingLogsForPerson> calculateTasksWorkingLogsForPersons(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
		Map<String, List<TaskWorkingLogs>> personTaskWorkingLogsByUser = tasks.stream()
				.flatMap(task -> {
							Map<String, TaskWorkingLogs> taskWorkingLogMetricsByPerson = taskWorkingLogsForPeopleCalculator
									.calculatePersonsMetricsForPeopleFromTask(
											task,
											peopleSearchConditions.getStartPeriod(),
											peopleSearchConditions.getEndPeriod());

							return taskWorkingLogMetricsByPerson
									.entrySet().stream();
						}
				)
				.filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
						peopleSearchConditions.getPeopleNames()))
				.filter(taskWorkingLogMetricsForUser -> wasTimeSpentOnTaskByPeriod(taskWorkingLogMetricsForUser.getValue()))
				.collect(Collectors.groupingBy(
						Entry::getKey,
						Collectors.mapping(Entry::getValue, Collectors.toList())));

		return personTaskWorkingLogsByUser.entrySet().stream()
				.map(entry -> {
					String person = entry.getKey();

					List<TaskWorkingLogs> dailyTaskWorkingLogMetrics = entry.getValue().stream()
							.sorted(Comparator.comparing(TaskWorkingLogs::getTaskId))
							.collect(Collectors.toList());

					List<ValidatedValue<TimeSpentByDay>> timeSpentByDaysForAllTask =
							calculateTimeSpentByDaysForAllTask(dailyTaskWorkingLogMetrics, peopleSearchConditions.getTeamName());

					int totalTimeSpentOnTaskInMinutesByPeriod = timeSpentByDaysForAllTask.stream()
							.mapToInt(validatedTimeSpentByDay -> validatedTimeSpentByDay.getValue().getTimeSpentMinutes())
							.sum();

					return TasksWorkingLogsForPerson.builder()
							.person(person)
							.dailyTaskWorkingLogs(dailyTaskWorkingLogMetrics)
							.totalTimeSpentByDays(timeSpentByDaysForAllTask)
							.totalTimeSpentOnTaskInMinutesByPeriod(totalTimeSpentOnTaskInMinutesByPeriod)
							.build();
				})
				.sorted(Comparator.comparing(TasksWorkingLogsForPerson::getPerson))
				.collect(Collectors.toList());
	}

	private boolean wasTimeSpentOnTaskByPeriod(TaskWorkingLogs value) {
		return value.getTimeSpentOnTaskInMinutesByPeriod() > 0;
	}

	private List<ValidatedValue<TimeSpentByDay>> calculateTimeSpentByDaysForAllTask(
			List<TaskWorkingLogs> dailyTaskWorkingLogMetrics, String teamName) {
		Map<LocalDate, Integer> timeSpentForAllTasksByDay = dailyTaskWorkingLogMetrics.stream()
				.flatMap(metrics -> metrics.getTimeSpentByDays().stream())
				.collect(Collectors.groupingBy(
						TimeSpentByDay::getDay,
						Collectors.summingInt(TimeSpentByDay::getTimeSpentMinutes)
				));

		return timeSpentForAllTasksByDay.entrySet().stream()
				.sorted(Entry.comparingByKey())
				.map(dayAndSpentTime -> {
					TimeSpentByDay timeSpentByDay = TimeSpentByDay.builder()
							.day(dayAndSpentTime.getKey())
							.timeSpentMinutes(dayAndSpentTime.getValue())
							.build();
					return taskWorkingLogsForPeopleValidator.validate(timeSpentByDay, teamName);
				})
				.collect(Collectors.toList());
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
