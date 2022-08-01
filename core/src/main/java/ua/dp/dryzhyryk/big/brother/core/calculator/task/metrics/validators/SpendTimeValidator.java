package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators;

import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

public interface SpendTimeValidator {

	ValidatedValue<Float> validateSpentTimePercentage(Float spentTimePercentageForPerson);

	ValidatedValue<Integer> validatedEstimation(Integer originalEstimateMinutes);

	ValidatedValue<Integer> validateTimeSpentOnTaskPersonInMinutesWithStatus(
			int timeSpentOnTaskPersonInMinutes, int timeSpendOnTaskByTeamByPeriodInMinutes, int timeSpentOnTaskPersonByPeriodInMinutes);
}
