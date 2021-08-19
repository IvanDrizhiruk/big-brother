package ua.dp.dryzhyryk.big.brother.core.data.source;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.DataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.util.*;

@Slf4j
//TODO rework with interface and file cash implementation
public class JiraInformationHolderImpl implements JiraInformationHolder {

    private final JiraResource jiraResource;
    private final DataStorage dataStorage;

    private final Map<JiraSearchConditions, List<Task>> tasksBySearchConditions = new HashMap<>();

    public JiraInformationHolderImpl(JiraResource jiraResource, DataStorage jiraDataStorage) {
        this.jiraResource = jiraResource;
        this.dataStorage = jiraDataStorage;
    }

    @Override
    public List<Task> getTasks(JiraSearchConditions searchConditions) {
        return tasksBySearchConditions.computeIfAbsent(searchConditions, this::loadTask);
    }

    private List<Task> loadTask(JiraSearchConditions searchConditions) {
        List<Task> tasksFromStorage = dataStorage.loadTasks(searchConditions);
        if (null != tasksFromStorage) {
            return tasksFromStorage;
        }

        List<Task> loadedTaskFromResources = jiraResource.loadTasks(searchConditions);
        List<Task> tasksFromResource = Optional.ofNullable(loadedTaskFromResources).orElse(Collections.emptyList());

        dataStorage.saveProjectSprint(searchConditions, tasksFromResource);

        return tasksFromResource;
    }
}