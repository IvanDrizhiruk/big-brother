package ua.dp.dryzhyryk.big.brother.core.data.source;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraStorage;

import java.util.*;

public class JiraInformationCache {

    private final JiraResource jiraResource;
    private final JiraStorage jiraStorage;

    private final Map<SprintSearchConditions, List<Task>> tasksByProjectKeyAndDate = new HashMap<>();

    public JiraInformationCache(JiraResource jiraResource, JiraStorage jiraStorage) {
        this.jiraResource = jiraResource;
        this.jiraStorage = jiraStorage;
    }

    public List<Task> getRootTasks(SprintSearchConditions sprintSearchConditions) {
        return tasksByProjectKeyAndDate.computeIfAbsent(sprintSearchConditions, this::loadProjectSprintTasks);
    }

    private List<Task> loadProjectSprintTasks(SprintSearchConditions sprintSearchConditions) {
        List<Task> tasksFromStorage = jiraStorage.loadProjectSprint(sprintSearchConditions);
        if (null != tasksFromStorage) {
            return tasksFromStorage;
        }

        List<Task> loadedTaskFromResources = jiraResource.loadProjectSprint(sprintSearchConditions);
        List<Task> tasksFromResource = Optional.ofNullable(loadedTaskFromResources).orElse(Collections.emptyList());

        jiraStorage.saveProjectSprint(sprintSearchConditions, tasksFromResource);

        return tasksFromResource;
    }
}