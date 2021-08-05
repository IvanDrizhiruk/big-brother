package ua.dp.dryzhyryk.big.brother.core.ports.model.person;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaskWorkingLogMetrics {

	private final String taskId;
	private final String taskName;

	private final List<TimeSpentByDay> timeSpentByDays;
	private final int timeSpentOnTaskInMinutes;
	private final int timeSpentOnTaskInMinutesByPeriod;
}
