package ua.dp.dryzhyryk.big.brother.core.data.source.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class Task {

    private final String id;
    private final String name;
    private final String status;
    private final String type;
    private final boolean isSubTask;

    private final Integer originalEstimateMinutes;
    private final Integer remainingEstimateMinutes;
    private final Integer timeSpentMinutes;

    private final List<TaskWorkLog> workLogs;

    private final List<Task> subTasks;
}
