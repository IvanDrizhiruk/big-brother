package ua.dp.dryzhyryk.big.brother.core.data.source;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;

import java.util.*;

public class JiraInformationCache {

    private final JiraResource jiraResource;
    private final JiraDataStorage jiraDataStorage;

    private final Map<SprintSearchConditions, List<Task>> tasksByProjectKeyAndDate = new HashMap<>();

    public JiraInformationCache(JiraResource jiraResource, JiraDataStorage jiraDataStorage) {
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
