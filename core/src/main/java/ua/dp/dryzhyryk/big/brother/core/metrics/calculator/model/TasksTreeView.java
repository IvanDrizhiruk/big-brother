package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;

@Value
@Builder
public class TasksTreeView {
	private final String project;
	private final String sprint;
	private final List<Task> rootTasks;
	private final Map<String, TaskMetrics> taskMetricsByTaskId;
}
