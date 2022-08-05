package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TasksMetricsForPerson {

	String person;
	List<TaskMetrics> finishedTaskMetrics;

	List<TaskMetrics> unFunctionalTaskMetrics;
	int timeSpentOnTasksPersonByPeriodForFunctionalTasksInMinutes;

	List<TaskMetrics> inProgressTaskMetrics;

}
