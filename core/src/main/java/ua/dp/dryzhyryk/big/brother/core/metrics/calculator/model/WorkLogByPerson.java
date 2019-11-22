package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkLogByPerson {
	private final String person;
	private final int minutesSpent;
}
