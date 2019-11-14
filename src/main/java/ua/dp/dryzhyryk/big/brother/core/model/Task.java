package ua.dp.dryzhyryk.big.brother.core.model;

import java.util.List;

import lombok.Value;

@Value
public class Task {

	private final String name;
	private final List<Task> subTasks;
}
