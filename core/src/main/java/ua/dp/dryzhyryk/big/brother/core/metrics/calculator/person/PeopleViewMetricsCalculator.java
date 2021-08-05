package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;

public class PeopleViewMetricsCalculator {

	private final TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator;

	public PeopleViewMetricsCalculator(
			TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator) {
		this.taskMetricsForPeopleCalculator = taskMetricsForPeopleCalculator;
	}

	public List<PersonMetrics> calculatePersonsMetrics(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
		Map<String, List<TaskWorkingLogMetrics>> personMetricsByUser = tasks.stream()
				.flatMap(task ->
						{
							Map<String, TaskWorkingLogMetrics> stringTaskWorkingLogMetricsMap = taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(
									task,
									peopleSearchConditions.getStartPeriod(),
									peopleSearchConditions.getEndPeriod());

							return stringTaskWorkingLogMetricsMap
									.entrySet().stream();
						}
				)
				.filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
						peopleSearchConditions.getPeopleNames()))
				.collect(Collectors.groupingBy(
						Entry::getKey,
						Collectors.mapping(Entry::getValue, Collectors.toList())));

		//TODO totals
		//todo validation

		return personMetricsByUser.entrySet().stream()
				.map(entry -> PersonMetrics.builder()
						.person(entry.getKey())
						.dailyTaskWorkingLogMetrics(entry.getValue().stream()
								.sorted(Comparator.comparing(TaskWorkingLogMetrics::getTaskId))
								.collect(Collectors.toList()))
						.build())
				.sorted(Comparator.comparing(PersonMetrics::getPerson))
				.collect(Collectors.toList());
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
