package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class PersonMetrics {

    private final String person;

    private final List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics;

    private final List<ValidatedValue<TimeSpentByDay>> totalTimeSpentByDays;

    private final int totalTimeSpentOnTaskInMinutesByPeriod;
}