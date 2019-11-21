package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskMetrics {

	private final String taskId;
	private final TaskTimeMetrics timeMetrics;
	private final List<DayWorkLog> dailyWorkLog;
}
