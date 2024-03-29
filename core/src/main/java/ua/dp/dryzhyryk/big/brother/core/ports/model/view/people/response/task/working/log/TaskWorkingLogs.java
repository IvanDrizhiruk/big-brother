package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaskWorkingLogs {

	private final String taskId;
	private final String taskName;

	private final List<TimeSpentByDay> timeSpentByDays;
	private final int timeSpentOnTaskInMinutes;
	private final int timeSpentOnTaskByPeriodInMinutes;
}
