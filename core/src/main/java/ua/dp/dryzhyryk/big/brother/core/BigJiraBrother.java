package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.MetricksCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTree;

@Slf4j
public class BigJiraBrother {

	private final JiraInformationHolder jiraInformationHolder;
	private final MetricksCalculator metricksCalculator;

	public BigJiraBrother(JiraInformationHolder jiraInformationHolder, MetricksCalculator metricksCalculator) {
		this.jiraInformationHolder = jiraInformationHolder;
		this.metricksCalculator = metricksCalculator;
	}

	public TasksTree prepareTaskView(SprintSearchConditions sprintSearchConditions) {
		List<Task> rootTasks = jiraInformationHolder.getRootTasks(sprintSearchConditions);

		Map<String, TaskMetrics> taskMetricsByTaskId = metricksCalculator.calculateFor(rootTasks);

		return TasksTree.builder()
				.project(sprintSearchConditions.getProject())
				.sprint(sprintSearchConditions.getSprint())
				.rootTasks(rootTasks)
				.taskMetricsByTaskId(taskMetricsByTaskId)
				.build();
	}

	public PeopleView preparePeopleView() {
		return new PeopleView();
	}

	public void prepareDayView() {
	}
}
