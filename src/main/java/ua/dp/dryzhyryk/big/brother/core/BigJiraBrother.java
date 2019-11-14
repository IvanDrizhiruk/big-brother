package ua.dp.dryzhyryk.big.brother.core;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.dp.dryzhyryk.big.brother.core.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTreeView;

@Service
public class BigJiraBrother {

	private final JiraInformationHolder jiraInformationHolder;

	@Autowired
	public BigJiraBrother(JiraInformationHolder jiraInformationHolder) {
		this.jiraInformationHolder = jiraInformationHolder;
	}

	public List<TasksTreeView> prepareTaskView(Set<String> projectsKeys, LocalDate startDate, LocalDate endDate) {

		List<TasksTree> tasksTrees = projectsKeys.stream()
				.map(projectKey -> jiraInformationHolder.getTasksAsTree(projectKey, startDate, endDate))
				.collect(Collectors.toList());

		TasksTreeViewAggregator tasksTreeViewAggregator = new TasksTreeViewAggregator();

		return tasksTrees.stream()
				.map(tasksTreeViewAggregator::prepareTasksTreeView)
				.collect(Collectors.toList());
	}

	public PeopleView preparePeopleView() {
		return new PeopleView();
	}

	public void prepareDayView() {
	}
}
