package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TaskMetrics {

    private final String taskId;
    private final String taskName;

    private final String taskExternalStatus;

    private final Integer originalEstimateInMinutes;
    private final int realSpendTimeInMinutes;
    private final int timeSpentOnTaskByPeriodInMinutes;

    private final float timeCoefficient;
}
