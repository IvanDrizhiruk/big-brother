package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class TasksWorkingLogsForPerson {

    private final String person;

    private final List<TaskWorkingLogs> dailyTaskWorkingLogs;

    private final List<ValidatedValue<TimeSpentByDay>> totalTimeSpentByDays;

    private final int totalTimeSpentOnTaskInMinutesByPeriod;
}
