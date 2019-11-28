package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class TasksRootView {
	private final String project;
	private final String sprint;
	private final List<Task> rootTasks;
	private final Map<String, TaskMetrics> taskMetricsByTaskId;
}
