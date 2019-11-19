package ua.dp.dryzhyryk.big.brother.core.ports;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

public interface JiraStorage {
	void saveProjectSprint(SprintSearchConditions sprintSearchConditions, List<Task> tasks);
	List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions);
}
