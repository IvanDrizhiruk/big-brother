package ua.dp.dryzhyryk.big.brother.core.ports;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

public interface JiraDataStorage {
	void saveProjectSprint(SearchConditions searchConditions, List<Task> tasks);
	List<Task> loadTasks(SearchConditions searchConditions);
}
