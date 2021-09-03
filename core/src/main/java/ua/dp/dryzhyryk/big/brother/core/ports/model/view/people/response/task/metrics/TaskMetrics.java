package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;

@Value
@Builder(toBuilder = true)
public class TaskMetrics {

	private final String taskId;
	private final String taskName;

	private final String taskExternalStatus;

	private final int originalEstimateInMinutes;
	private final int realSpendTimeInMinutes;
	private final int realSpendTimeByPersonInMinutes;

	private final float timeCoefficient;
}
