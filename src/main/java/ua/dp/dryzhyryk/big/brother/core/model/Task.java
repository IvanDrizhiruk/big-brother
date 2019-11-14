package ua.dp.dryzhyryk.big.brother.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class Task {

    private final String name;

    private final Integer originalEstimateMinutes;
    private final Integer remainingEstimateMinutes;
    private final Integer timeSpentMinutes;

    private final List<TaskWorkLog> workLogs;

    private final List<Task> subTasks;
}
