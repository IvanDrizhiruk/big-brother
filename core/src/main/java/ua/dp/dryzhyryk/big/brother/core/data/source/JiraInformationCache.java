package ua.dp.dryzhyryk.big.brother.core.data.source;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiraInformationCache {

    private final JiraResource jiraResource;

    private final Map<SprintSearchConditions, List<Task>> tasksByProjectKeyAndDate = new HashMap<>();

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
