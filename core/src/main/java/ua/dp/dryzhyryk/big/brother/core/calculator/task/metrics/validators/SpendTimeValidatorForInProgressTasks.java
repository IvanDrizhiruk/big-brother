package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators;

import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

public class SpendTimeValidatorForInProgressTasks implements SpendTimeValidator {


	private static final int OK_LIMIT = 120;
	private static final int SLOW_LIMIT = 150;

	@Override
	public ValidatedValue<Float> validateSpentTimePercentage(Float spentTimePercentageForPerson) {
		if (spentTimePercentageForPerson == null) {
			return ValidatedValue.valueWithErrorStatus(null, "Unknown spent time");
		}

		ValidatedValue<Float> spentTimePercentageForPersonWithStatus;
		if (OK_LIMIT >= spentTimePercentageForPerson) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithNotEvaluatedStatus(spentTimePercentageForPerson);
		} else if (OK_LIMIT < spentTimePercentageForPerson && spentTimePercentageForPerson <= SLOW_LIMIT) {
			spentTimePercentageForPersonWithStatus =
					ValidatedValue.valueWithWarningStatus(spentTimePercentageForPerson, "Made too slow");
		} else {
			spentTimePercentageForPersonWithStatus =
					ValidatedValue.valueWithErrorStatus(spentTimePercentageForPerson, "Made too slow");
		}

		return spentTimePercentageForPersonWithStatus;
	}

	@Override
	public ValidatedValue<Integer> validatedEstimation(Integer originalEstimateMinutes) {
		return originalEstimateMinutes == null
				? ValidatedValue.valueWithErrorStatus(originalEstimateMinutes, "Task does not have an estimation")
				: ValidatedValue.valueWithNotEvaluatedStatus(originalEstimateMinutes);
	}

	@Override
	public ValidatedValue<Integer> validateTimeSpentOnTaskPersonInMinutesWithStatus(int timeSpentOnTaskPersonInMinutes,
			int timeSpendOnTaskByTeamByPeriodInMinutes, int timeSpentOnTaskPersonByPeriodInMinutes) {

		boolean wasPersonWorkOnTaskAtPeriod = timeSpentOnTaskPersonByPeriodInMinutes != 0;
		if (wasPersonWorkOnTaskAtPeriod) {
			return ValidatedValue.valueWithNotEvaluatedStatus(timeSpentOnTaskPersonInMinutes);
		}

		boolean wasTeamWorkOnTaskAtPeriod = timeSpendOnTaskByTeamByPeriodInMinutes != 0;
		return wasTeamWorkOnTaskAtPeriod
				? ValidatedValue.valueWithErrorStatus(timeSpentOnTaskPersonInMinutes,
				"No work was made by person on task by period. But team mates worked")
				: ValidatedValue.valueWithWarningStatus(timeSpentOnTaskPersonInMinutes, "No work was made on task by period");
	}
}
