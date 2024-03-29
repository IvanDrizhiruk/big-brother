package ua.dp.dryzhyryk.big.brother.core.ports;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;

import java.util.List;

public interface JiraResource {
    List<Task> loadTasks(JiraSearchConditions searchConditions);
}
