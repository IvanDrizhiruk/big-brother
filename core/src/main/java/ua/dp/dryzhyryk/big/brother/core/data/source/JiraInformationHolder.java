package ua.dp.dryzhyryk.big.brother.core.data.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

public class JiraInformationHolder {

	private final JiraResource jiraResource;
	private final JiraDataStorage jiraDataStorage;

	private final Map<SprintSearchConditions, List<Task>> tasksByProjectKeyAndDate = new HashMap<>();

	public JiraInformationHolder(JiraResource jiraResource, JiraDataStorage jiraDataStorage) {
		this.jiraResource = jiraResource;
		this.jiraDataStorage = jiraDataStorage;
	}

	public List<Task> getRootTasks(SprintSearchConditions sprintSearchConditions) {
		return tasksByProjectKeyAndDate.computeIfAbsent(sprintSearchConditions, this::loadProjectSprintTasks);
	}

	private List<Task> loadProjectSprintTasks(SprintSearchConditions sprintSearchConditions) {
		List<Task> tasksFromStorage = jiraDataStorage.loadProjectSprint(sprintSearchConditions);
		if (null != tasksFromStorage) {
			return tasksFromStorage;
		}

		List<Task> loadedTaskFromResources = jiraResource.loadProjectSprint(sprintSearchConditions);
		List<Task> tasksFromResource = Optional.ofNullable(loadedTaskFromResources).orElse(Collections.emptyList());

		jiraDataStorage.saveProjectSprint(sprintSearchConditions, tasksFromResource);

		return tasksFromResource;
	}
}
