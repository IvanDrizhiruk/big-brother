package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	public List<PersonMetrics> calculateFor(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
		Map<String, PersonMetrics> personMetricsByUser = tasks.stream()
				.flatMap(task ->
						taskMetricsForPeopleCalculator.toPersonsMetricsForTask(
										task,
										peopleSearchConditions.getStartPeriod(),
										peopleSearchConditions.getEndPeriod())
								.stream()
				)
				.filter(personMetrics -> isExcludePersonFromPersonMetrics(personMetrics, peopleSearchConditions.getPeopleNames()))
				.collect(
						Collectors.toMap(
								PersonMetrics::getPerson,
								Function.identity(),
								this::mergePersonMetricsForOnePerson));

		return new ArrayList<>(personMetricsByUser.values());
	}

	private boolean isExcludePersonFromPersonMetrics(PersonMetrics personMetrics, List<String> availablePersons) {
		return availablePersons.contains(personMetrics.getPerson());
	}

	private PersonMetrics mergePersonMetricsForOnePerson(PersonMetrics first, PersonMetrics second) {
		List<TaskWorkingLogMetrics> dailyTaskLogs = Stream.of(first.getDailyTaskWorkingLogMetrics(), second.getDailyTaskWorkingLogMetrics())
				.flatMap(List::stream)
				.collect(Collectors.toList());

		return first.toBuilder()
				.dailyTaskWorkingLogMetrics(dailyTaskLogs)
				.build();
	}
}
