package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaskWorkingLogMetrics {

	private final String taskId;
	private final String taskName;
	private final String taskExternalStatus;

	private final List<TimeSpentByDay> timeSpentByDays;
	private final int totalTimeSpentByPeriodInMinutes;       // total time spend by person for current period
	private final int totalTimeSpentOnTaskInMinutes;         // total time spend by person

	private final int timeSpentMinutes;                      //TODO duplicate with totalTimeSpentOnTaskInMinutes
	private final int originalEstimateMinutes;

	private final float timeCoefficient;
}
