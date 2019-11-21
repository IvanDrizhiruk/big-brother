package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskTimeMetrics {
	private final int originalEstimateMinutes;
	private final int remainingEstimateMinutes;
	private final int timeSpentMinutes;

	private final float timeCoefficient;
}
