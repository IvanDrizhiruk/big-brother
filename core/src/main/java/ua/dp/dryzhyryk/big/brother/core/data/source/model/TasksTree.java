package ua.dp.dryzhyryk.big.brother.core.data.source.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TasksTree {
	private final String project;
	private final String sprint;
	private final List<Task> rootTasks;
}
