package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;

@Value
@Builder(toBuilder = true)
public class DayWorkLogForPerson {

	private final LocalDate dayOfWork;

	private final List<TaskWorkingLogMetrics> dailyTaskLogs;
}
