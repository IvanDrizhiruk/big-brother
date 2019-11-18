package ua.dp.dryzhyryk.big.brother.core.data.source.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskTreeView {

	private final String name;
	private final List<TaskTreeView> subTasks;
}
