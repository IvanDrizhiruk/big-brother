package ua.dp.dryzhyryk.big.brother.core.ports;

import java.time.LocalDate;
import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.search.SprintSearchConditions;

public interface JiraResource {
	List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions);
}
