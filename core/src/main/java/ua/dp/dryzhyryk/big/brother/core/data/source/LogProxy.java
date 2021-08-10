package ua.dp.dryzhyryk.big.brother.core.data.source;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.utils.PrintUtils;

@Slf4j
public class LogProxy implements JiraInformationHolder {
	private final JiraInformationHolder jiraInformationHolder;

	public LogProxy(JiraInformationHolder jiraInformationHolder) {
		this.jiraInformationHolder = jiraInformationHolder;
	}

	@Override
	public List<Task> getTasks(SearchConditions searchConditions) {
		List<Task> tasks = jiraInformationHolder.getTasks(searchConditions);
		PrintUtils.printTasks(tasks, log);
		return tasks;
	}
}
