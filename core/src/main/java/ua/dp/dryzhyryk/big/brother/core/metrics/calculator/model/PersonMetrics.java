package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonMetrics {

	private final String person;

	List<DayWorkLogForPerson> dayWorkLogForPeople;

	private final List<TaskLog> sprintTaskLogs;
	private final TaskLog totalSprintTaskLog;
}
