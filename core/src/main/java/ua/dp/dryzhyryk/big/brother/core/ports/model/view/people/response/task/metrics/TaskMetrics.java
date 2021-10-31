package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

@Value
@Builder(toBuilder = true)
public class TaskMetrics {

	private final String taskId;
	private final String taskName;

	private final String taskExternalStatus;

	private final ValidatedValue<Integer> estimationInMinutes;

	private final int timeSpentOnTaskPersonByPeriodInMinutes;
	private final ValidatedValue<Integer> timeSpentOnTaskPersonInMinutes;
	private final int timeSpendOnTaskByTeamInMinutes;
	private final int timeSpendOnTaskInMinutes;

	private final ValidatedValue<Float> spentTimePercentageForPerson;
	private final ValidatedValue<Float> spentTimePercentageForTeam;
}
