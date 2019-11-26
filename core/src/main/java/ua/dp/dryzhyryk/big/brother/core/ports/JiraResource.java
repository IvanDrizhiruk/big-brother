package ua.dp.dryzhyryk.big.brother.core.ports;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

import java.util.List;

public interface JiraResource {
    List<Task> loadTasks(SearchConditions searchConditions);
}
