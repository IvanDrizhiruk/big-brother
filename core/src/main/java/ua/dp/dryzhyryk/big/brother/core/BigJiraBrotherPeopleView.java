package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.PeopleViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;

@Slf4j
public class BigJiraBrotherPeopleView {

	private final JiraInformationHolder jiraInformationHolder;
	private final TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator;
	private final PeopleViewMetricsCalculator peopleViewMetricsCalculator;
	private final SprintViewMetricsCalculator sprintViewMetricsCalculator;

	public BigJiraBrotherPeopleView(
			JiraInformationHolder jiraInformationHolder,
			TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator,
			PeopleViewMetricsCalculator peopleViewMetricsCalculator,
			SprintViewMetricsCalculator sprintViewMetricsCalculator) {
		this.jiraInformationHolder = jiraInformationHolder;
		this.tasksTreeViewMetricsCalculator = tasksTreeViewMetricsCalculator;
		this.peopleViewMetricsCalculator = peopleViewMetricsCalculator;
		this.sprintViewMetricsCalculator = sprintViewMetricsCalculator;
	}



	public PeopleView preparePeopleView(PersonSearchConditions personSearchConditions) {
		List<Task> tasks = jiraInformationHolder.getTasks(personSearchConditions);

		List<PersonMetrics> personMetrics = peopleViewMetricsCalculator.calculateFor(tasks);

		return PeopleView.builder()
				.project("person")
				.sprint("person")
				.personMetrics(personMetrics)
				.build();
	}


}
