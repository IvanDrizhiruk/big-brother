package ua.dp.dryzhyryk.big.brother.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

@Service
public class JiraInformationCache {

	private final JiraResource jiraResource;

	private final Map<SprintSearchConditions, List<Task>> tasksByProjectKeyAndDate = new HashMap<>();

	@Autowired
	public JiraInformationCache(JiraResource jiraResource) {
		this.jiraResource = jiraResource;
	}

	public List<Task> getRootTasks(SprintSearchConditions sprintSearchConditions) {
		return tasksByProjectKeyAndDate.computeIfAbsent(sprintSearchConditions, this::loadProjectSprintTasks);
	}

	private List<Task> loadProjectSprintTasks(SprintSearchConditions sprintSearchConditions) {
		return jiraResource.loadProjectSprint(sprintSearchConditions);
	}
}
