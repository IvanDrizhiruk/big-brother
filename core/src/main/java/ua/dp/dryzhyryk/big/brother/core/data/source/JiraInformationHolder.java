package ua.dp.dryzhyryk.big.brother.core.data.source;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

import java.util.List;

public class JiraInformationHolder {


    private final JiraInformationCache jiraInformationCache;

    public JiraInformationHolder(JiraInformationCache jiraInformationCache) {
        this.jiraInformationCache = jiraInformationCache;
    }

    public TasksTree getTasksAsTree(SprintSearchConditions sprintSearchConditions) {

        List<Task> rootTasks = jiraInformationCache.getRootTasks(sprintSearchConditions);

        return TasksTree.builder()
                .project(sprintSearchConditions.getProject())
                .sprint(sprintSearchConditions.getSprint())
                .rootTasks(rootTasks)
                .build();
    }
}
