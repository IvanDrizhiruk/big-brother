package ua.dp.dryzhyryk.big.brother.core;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

@Service
public class JiraInformationHolder {


	private final JiraInformationCache jiraInformationCache;

	@Autowired
	public JiraInformationHolder(JiraInformationCache jiraInformationCache) {
		this.jiraInformationCache = jiraInformationCache;
	}

	public TasksTree getTasksAsTree(String projectsKey, LocalDate startDate, LocalDate endDate) {

		List<Task> rootTasks = jiraInformationCache.getRootTasks(projectsKey, startDate, endDate);

		return TasksTree.builder()
				.rootTasks(rootTasks)
				.build();
	}
}
