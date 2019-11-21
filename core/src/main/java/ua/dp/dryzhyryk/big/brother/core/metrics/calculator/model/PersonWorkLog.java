package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonWorkLog {
	private final String person;
	private final int minutesSpent;
}
