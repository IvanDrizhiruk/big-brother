package ua.dp.dryzhyryk.big.brother.core.ports.model.person;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonMetrics {

	private final String person;

	private final List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics;

	//TODO total
}
