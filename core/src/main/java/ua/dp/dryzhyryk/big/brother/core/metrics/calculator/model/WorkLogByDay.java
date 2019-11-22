package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkLogByDay {

	private final LocalDate workDate;
	private final List<PersonWorkLog> personWorkLogs;
}
