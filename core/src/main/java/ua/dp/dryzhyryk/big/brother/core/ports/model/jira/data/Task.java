package ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Task {

	String id;
	String name;
	String status;
	String type;
	boolean isSubTask;

	Map<String, String> additionalFieldValues;

	Integer originalEstimateMinutes;
	Integer remainingEstimateMinutes;
	Integer timeSpentMinutes;

	List<TaskWorkLog> workLogs;

	List<Task> subTasks;
}
