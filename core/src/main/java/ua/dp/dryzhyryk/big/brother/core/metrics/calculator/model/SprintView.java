package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SprintView {
	private final String project;
	private final String sprint;
	private final TaskTimeMetrics totalTasksTimeMetrics;
}
