package ua.dp.dryzhyryk.big.brother.core.ports;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;

public interface DataStorage {

	void saveProjectSprint(JiraSearchConditions searchConditions, List<Task> tasks);

	List<Task> loadTasks(JiraSearchConditions searchConditions);
}
