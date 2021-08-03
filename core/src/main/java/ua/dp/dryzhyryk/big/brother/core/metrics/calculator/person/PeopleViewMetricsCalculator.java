package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
						taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(
										task,
										peopleSearchConditions.getStartPeriod(),
										peopleSearchConditions.getEndPeriod())
								.entrySet().stream()
				)
				.filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
						peopleSearchConditions.getPeopleNames()))
				.collect(Collectors.groupingBy(
						Entry::getKey,
						Collectors.mapping(Entry::getValue, Collectors.toList())));

		return personMetricsByUser.entrySet().stream()
				.map(entry -> PersonMetrics.builder()
						.person(entry.getKey())
						.dailyTaskWorkingLogMetrics(entry.getValue())
						.build())
				.collect(Collectors.toList());
	}

	private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
		return availablePersons.contains(person);
	}
}
