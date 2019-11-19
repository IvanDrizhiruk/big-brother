package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.aggregators.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

@Slf4j
public class BigJiraBrother {

	private final JiraInformationHolder jiraInformationHolder;

	public BigJiraBrother(JiraInformationHolder jiraInformationHolder) {
		this.jiraInformationHolder = jiraInformationHolder;
	}

	public List<TasksTreeView> prepareTaskView(SprintSearchConditions sprintSearchConditions) {
		TasksTree tasksTree = jiraInformationHolder.getTasksAsTree(sprintSearchConditions);

		//		List<TasksTree> tasksTrees = projectsKeys.stream()
		//				.map(projectKey -> jiraInformationHolder.getTasksAsTree(projectKey, startDate, endDate))
		//				.collect(Collectors.toList());

		//		TasksTreeViewAggregator tasksTreeViewAggregator = new TasksTreeViewAggregator();

		//		return tasksTrees.stream()
		//				.map(tasksTreeViewAggregator::prepareTasksTreeView)
		//				.collect(Collectors.toList());
		return null;
	}

	public PeopleView preparePeopleView() {
		return new PeopleView();
	}

	public void prepareDayView() {
	}
}
