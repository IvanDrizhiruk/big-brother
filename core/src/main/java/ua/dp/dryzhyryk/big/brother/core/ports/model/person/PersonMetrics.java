package ua.dp.dryzhyryk.big.brother.core.ports.model.person;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidationInfo;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

@Value
@Builder(toBuilder = true)
public class PersonMetrics {

	private final String person;

	private final List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics;

	private final List<ValueWithValidation<TimeSpentByDay>> timeSpentByDaysForAllTask;
}
