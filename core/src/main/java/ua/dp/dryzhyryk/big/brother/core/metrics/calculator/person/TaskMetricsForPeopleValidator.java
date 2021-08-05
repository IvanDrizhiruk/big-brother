package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

public class TaskMetricsForPeopleValidator {

    public ValueWithValidation<TimeSpentByDay> validate(TimeSpentByDay timeSpentByDay) {
        //TODO need implementation
        return ValueWithValidation.valueWithNotEvaluatedValidationStatus(timeSpentByDay);
    }
}
