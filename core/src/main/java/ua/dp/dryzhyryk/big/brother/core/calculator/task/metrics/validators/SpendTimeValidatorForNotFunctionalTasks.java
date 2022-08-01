package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators;

import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

public class SpendTimeValidatorForNotFunctionalTasks implements SpendTimeValidator {

	@Override
	public ValidatedValue<Float> validateSpentTimePercentage(Float spentTimePercentageForPerson) {
		return ValidatedValue.valueWithNotEvaluatedStatus(spentTimePercentageForPerson);
	}

	@Override
	public ValidatedValue<Integer> validatedEstimation(Integer originalEstimateMinutes) {
		return ValidatedValue.valueWithNotEvaluatedStatus(originalEstimateMinutes);
	}

	@Override
	public ValidatedValue<Integer> validateTimeSpentOnTaskPersonInMinutesWithStatus(int timeSpentOnTaskPersonInMinutes,
			int timeSpendOnTaskByTeamByPeriodInMinutes, int timeSpentOnTaskPersonByPeriodInMinutes) {

		return ValidatedValue.valueWithNotEvaluatedStatus(timeSpentOnTaskPersonInMinutes);
	}
}
