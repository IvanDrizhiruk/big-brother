package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;

public class TaskMetricsForPersonCalculator {
	public Map<String, TaskMetrics> calculateTaskMetricsForPerson(Task task, LocalDate startPeriod, LocalDate endPeriod) {
		return Collections.emptyMap();
	}
}
