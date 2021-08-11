package ua.dp.dryzhyryk.big.brother.core.data.source;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;

public interface JiraInformationHolder {

	List<Task> getTasks(JiraSearchConditions searchConditions);
}
