package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TasksMetricsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchTaskExcludeConditions;

public class TasksMetricsForPersonCalculator {

	private final TaskMetricsForPersonCalculator taskMetricsForPersonCalculator;
	private final TaskMetricsForPersonValidator taskMetricsForPersonValidator;

	public TasksMetricsForPersonCalculator(
			TaskMetricsForPersonCalculator taskMetricsForPersonCalculator,
			TaskMetricsForPersonValidator taskMetricsForPersonValidator) {
		this.taskMetricsForPersonCalculator = taskMetricsForPersonCalculator;
		this.taskMetricsForPersonValidator = taskMetricsForPersonValidator;
	}

	public List<TasksMetricsForPerson> calculateTasksMetricsForPerson(
			List<Task> tasks,
			PeopleSearchConditions peopleSearchConditions,
			PeopleSearchTaskExcludeConditions taskExcludeConditions) {

		List<Task> tasksForProcessing = tasks.stream()
				.filter(task -> {
					if (taskExcludeConditions.getByStatus().contains(task.getStatus())) {
						return false;
					}

					return taskExcludeConditions.getByFields().stream()
							.noneMatch(entry ->
									task.getAdditionalFieldValues().getOrDefault(entry.getName(), "").equals(entry.getValue()));
				})
				.collect(Collectors.toList());

		Map<String, List<TaskMetrics>> personTaskMetricsByUser = tasksForProcessing.stream()
				.flatMap(task -> {
							Map<String, TaskMetrics> personTaskMetricsByPerson = taskMetricsForPersonCalculator
									.calculateTaskMetricsForPerson(
											task,
											peopleSearchConditions.getStartPeriod(),
											peopleSearchConditions.getEndPeriod(),
											peopleSearchConditions.getPeopleNames());

							return personTaskMetricsByPerson
									.entrySet().stream();
						}
				)
				.filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
						peopleSearchConditions.getPeopleNames()))
				.collect(Collectors.groupingBy(
						Map.Entry::getKey,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		return personTaskMetricsByUser.entrySet().stream()
				.map(entry -> {
					String person = entry.getKey();

					List<TaskMetrics> taskMetrics = entry.getValue();

					return TasksMetricsForPerson.builder()
							.person(person)
							.taskMetrics(taskMetrics)
							.build();
				})
				.sorted(Comparator.comparing(TasksMetricsForPerson::getPerson))
				.collect(Collectors.toList());
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
