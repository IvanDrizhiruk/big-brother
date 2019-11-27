package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonMetrics {

	private final String person;

	private final List<TaskWorkingLogMetrics> dailyTaskLogs;
	private final List<TimeSpentByDay> totalTimeSpentByDay;
	//TODO
	private final int totalTimeSpentInCurrentPeriodInMinutes;
}
