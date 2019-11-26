package ua.dp.dryzhyryk.big.brother.core.data.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

public class JiraInformationHolder {

	private final JiraResource jiraResource;
	private final JiraDataStorage jiraDataStorage;

	private final Map<SearchConditions, List<Task>> tasksBySearchConditions = new HashMap<>();

	public JiraInformationHolder(JiraResource jiraResource, JiraDataStorage jiraDataStorage) {
		this.jiraResource = jiraResource;
		this.jiraDataStorage = jiraDataStorage;
	}

	public List<Task> getTasks(SearchConditions searchConditions) {
		return tasksBySearchConditions.computeIfAbsent(searchConditions, this::loadTask);
	}

	private List<Task> loadTask(SearchConditions searchConditions) {
		List<Task> tasksFromStorage = jiraDataStorage.loadTasks(searchConditions);
		if (null != tasksFromStorage) {
			return tasksFromStorage;
		}

		List<Task> loadedTaskFromResources = jiraResource.loadTasks(searchConditions);
		List<Task> tasksFromResource = Optional.ofNullable(loadedTaskFromResources).orElse(Collections.emptyList());

		jiraDataStorage.saveProjectSprint(searchConditions, tasksFromResource);

		return tasksFromResource;
	}
}
