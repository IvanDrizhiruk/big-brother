package ua.dp.dryzhyryk.big.brother.core.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TasksTree {

	private final List<Task> rootTasks;
}
