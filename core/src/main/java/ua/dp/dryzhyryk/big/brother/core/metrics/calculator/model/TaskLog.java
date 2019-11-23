package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaskLog {

	private final int timeSpentMinutes;
	private final int originalEstimateMinutes;

	private final float timeCoefficient;


	private final String parentTaskId;
	private final String taskId;
	private final String taskName;
}
