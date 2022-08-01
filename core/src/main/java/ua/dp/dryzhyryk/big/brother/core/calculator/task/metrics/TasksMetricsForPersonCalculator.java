package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TasksMetricsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchUnfunctionalTasksConditions;

public class TasksMetricsForPersonCalculator {

	private final TaskMetricsForPersonCalculator taskMetricsForPersonCalculator;

	public TasksMetricsForPersonCalculator(TaskMetricsForPersonCalculator taskMetricsForPersonCalculator) {
		this.taskMetricsForPersonCalculator = taskMetricsForPersonCalculator;
	}

	public List<TasksMetricsForPerson> calculateTasksMetricsForPerson(
			List<Task> tasks,
			PeopleSearchConditions peopleSearchConditions,
			PeopleSearchUnfunctionalTasksConditions functionalTasksConditions) {

		Map<TaskMetaType, List<Task>> tasksByMetaType = tasks.stream()
				.collect(Collectors.groupingBy(task -> evaluateTaskMetaType(functionalTasksConditions, task)));

		Map<String, List<TaskMetrics>> finishedTaskMetricsByUser =
				prepareTaskMatrixByUser(tasksByMetaType, TaskMetaType.FINISHED, peopleSearchConditions);
		Map<String, List<TaskMetrics>> unFunctionalTaskMetricsByUser =
				prepareTaskMatrixByUser(tasksByMetaType, TaskMetaType.UN_FUNCTIONAL, peopleSearchConditions);

		return Stream.of(
						finishedTaskMetricsByUser.keySet(),
						unFunctionalTaskMetricsByUser.keySet())
				.flatMap(Set::stream)
				.distinct()
				.map(person -> {
					List<TaskMetrics> finishedTaskMetrics = finishedTaskMetricsByUser.get(person);
					List<TaskMetrics> unFunctionalTaskMetrics = unFunctionalTaskMetricsByUser.get(person);


					int timeSpentOnTasksPersonByPeriodForFunctionalTasksInMinutes = unFunctionalTaskMetrics.stream()
							.mapToInt(TaskMetrics::getTimeSpentOnTaskPersonByPeriodInMinutes)
							.sum();


					return TasksMetricsForPerson.builder()
							.person(person)
							.finishedTaskMetrics(finishedTaskMetrics)
							.unFunctionalTaskMetrics(unFunctionalTaskMetrics)
							//TODO add validation
							.timeSpentOnTasksPersonByPeriodForFunctionalTasksInMinutes(timeSpentOnTasksPersonByPeriodForFunctionalTasksInMinutes)
							.build();
				})
				.sorted(Comparator.comparing(TasksMetricsForPerson::getPerson))
				.collect(Collectors.toList());
	}

	private Map<String, List<TaskMetrics>> prepareTaskMatrixByUser(Map<TaskMetaType, List<Task>> grouped, TaskMetaType taskMetaType,
			PeopleSearchConditions peopleSearchConditions) {
		return grouped.getOrDefault(taskMetaType, Collections.emptyList()).stream()
				.flatMap(task -> {
							Map<String, TaskMetrics> personTaskMetricsByPerson = taskMetricsForPersonCalculator
									.calculateTaskMetricsForPerson(
											task,
											peopleSearchConditions.getStartPeriod(),
											peopleSearchConditions.getEndPeriod(),
											peopleSearchConditions.getPeopleNames(),
											taskMetaType);

							return personTaskMetricsByPerson
									.entrySet().stream();
						}
				)
				.filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
						peopleSearchConditions.getPeopleNames()))
				.collect(Collectors.groupingBy(
						Map.Entry::getKey,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	private TaskMetaType evaluateTaskMetaType(PeopleSearchUnfunctionalTasksConditions functionalTasksConditions, Task task) {
		if (functionalTasksConditions.getByStatus().contains(task.getStatus())) {
			return TaskMetaType.UN_FUNCTIONAL;
		}
		if (functionalTasksConditions.getByFields().stream()
				.anyMatch(entry ->
						task.getAdditionalFieldValues().getOrDefault(entry.getName(), "").equals(entry.getValue()))) {
			return TaskMetaType.UN_FUNCTIONAL;
		}

		return TaskMetaType.FINISHED;
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
