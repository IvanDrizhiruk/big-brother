package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

@Value
@Builder(toBuilder = true)
public class TaskMetrics {

	String taskId;
	String taskName;

	String taskExternalStatus;

	ValidatedValue<Integer> estimationInMinutes;

	ValidatedValue<Integer> timeSpentOnTaskPersonInMinutes;
	int timeSpentOnTaskPersonByPeriodInMinutes;
	int timeSpendOnTaskByTeamInMinutes;

	ValidatedValue<Float> spentTimePercentageForPerson;
	ValidatedValue<Float> spentTimePercentageForTeam;
}
