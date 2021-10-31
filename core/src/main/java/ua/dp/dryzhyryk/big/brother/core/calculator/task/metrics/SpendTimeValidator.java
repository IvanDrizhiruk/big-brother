package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

public class SpendTimeValidator {

	private static final int EXTREMELY_FAST_LIMIT = 50;
	private static final int FAST_LIMIT = 80;
	private static final int SLOW_LIMIT = 120;
	private static final int EXTREMELY_SLOW_LIMIT = 200;

	public ValidatedValue<Float> validate(Float spentTimePercentageForPerson) {
		ValidatedValue<Float> spentTimePercentageForPersonWithStatus;
//		if (spentTimePercentageForPerson == null) {
		//			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithNotEvaluatedStatus(spentTimePercentageForPerson);
		//		} else
			if (FAST_LIMIT <= spentTimePercentageForPerson && spentTimePercentageForPerson <= SLOW_LIMIT) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithOkStatus(spentTimePercentageForPerson);
		} else if (EXTREMELY_FAST_LIMIT <= spentTimePercentageForPerson && spentTimePercentageForPerson <= FAST_LIMIT) {
			spentTimePercentageForPersonWithStatus =
					ValidatedValue.valueWithWarningStatus(spentTimePercentageForPerson, "Made too fast");
		} else if (spentTimePercentageForPerson <= EXTREMELY_FAST_LIMIT) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithErrorStatus(spentTimePercentageForPerson, "Made too fast");
		} else if (SLOW_LIMIT <= spentTimePercentageForPerson && spentTimePercentageForPerson <= EXTREMELY_SLOW_LIMIT) {
			spentTimePercentageForPersonWithStatus =
					ValidatedValue.valueWithWarningStatus(spentTimePercentageForPerson, "Made too slow");
		} else if (spentTimePercentageForPerson >= EXTREMELY_SLOW_LIMIT) {
			spentTimePercentageForPersonWithStatus =
					ValidatedValue.valueWithErrorStatus(spentTimePercentageForPerson, "Made too slow");
		} else {
			throw new IllegalStateException("Unexpected case " + spentTimePercentageForPerson);
		}

		return spentTimePercentageForPersonWithStatus;
	}
}
