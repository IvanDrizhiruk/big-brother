package ua.dp.dryzhyryk.big.brother.core.ports;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

public interface JiraInformationStorage {
	List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions);
}
