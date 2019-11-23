package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DayWorkLogForPerson {

	private final LocalDate dayOfWork;

	private final List<TaskLog> dailyTaskLogs;
}
