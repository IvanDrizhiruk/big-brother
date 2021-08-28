package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

public class TasksWorkingLogsForPersonsCalculator {

	private final TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator;
	private final TaskMetricsForPeopleValidator taskMetricsForPeopleValidator;

	public TasksWorkingLogsForPersonsCalculator(
			TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator,
			TaskMetricsForPeopleValidator taskMetricsForPeopleValidator) {
		this.taskMetricsForPeopleCalculator = taskMetricsForPeopleCalculator;
		this.taskMetricsForPeopleValidator = taskMetricsForPeopleValidator;
	}

	public List<TasksWorkingLogsForPerson> calculateTasksWorkingLogsForPersons(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
		Map<String, List<TaskWorkingLogMetrics>> personMetricsByUser = tasks.stream()
				.flatMap(task -> {
							Map<String, TaskWorkingLogMetrics> taskWorkingLogMetricsByPerson = taskMetricsForPeopleCalculator
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

		return personMetricsByUser.entrySet().stream()
				.map(entry -> {
					String person = entry.getKey();

					List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics = entry.getValue().stream()
							.sorted(Comparator.comparing(TaskWorkingLogMetrics::getTaskId))
							.collect(Collectors.toList());

					List<ValidatedValue<TimeSpentByDay>> timeSpentByDaysForAllTask =
							calculateTimeSpentByDaysForAllTask(dailyTaskWorkingLogMetrics, peopleSearchConditions.getTeamName());

					int totalTimeSpentOnTaskInMinutesByPeriod = timeSpentByDaysForAllTask.stream()
							.mapToInt(validatedTimeSpentByDay -> validatedTimeSpentByDay.getValue().getTimeSpentMinutes())
							.sum();

					return TasksWorkingLogsForPerson.builder()
							.person(person)
							.dailyTaskWorkingLogMetrics(dailyTaskWorkingLogMetrics)
							.totalTimeSpentByDays(timeSpentByDaysForAllTask)
							.totalTimeSpentOnTaskInMinutesByPeriod(totalTimeSpentOnTaskInMinutesByPeriod)
							.build();
				})
				.sorted(Comparator.comparing(TasksWorkingLogsForPerson::getPerson))
				.collect(Collectors.toList());
	}

	private boolean wasTimeSpentOnTaskByPeriod(TaskWorkingLogMetrics value) {
		return value.getTimeSpentOnTaskInMinutesByPeriod() > 0;
	}

	private List<ValidatedValue<TimeSpentByDay>> calculateTimeSpentByDaysForAllTask(
			List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics, String teamName) {
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
					return taskMetricsForPeopleValidator.validate(timeSpentByDay, teamName);
				})
				.collect(Collectors.toList());
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
