package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.search.SprintSearchConditions;

public interface JiraInformationStorage {
	List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions);
}
