package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class TaskWorkingLogMetrics {

	private final String taskId;
	private final String taskName;
	private final String taskExternalStatus;

	private final List<TimeSpentByDay> timeSpentByDays;
	private final int totalTimeSpentByPeriodInMinutes;
	private final int totalTimeSpentOnTaskInMinutes;

	private final int timeSpentMinutes;
	private final int originalEstimateMinutes;

	private final float timeCoefficient;
}
