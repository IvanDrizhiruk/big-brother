package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.TaskTreeView;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTreeView;

public class TasksTreeViewAggregator {

	public TasksTreeView prepareTasksTreeView(TasksTree tasksTree) {
		List<Task> rootTasks = tasksTree.getRootTasks();

		List<TaskTreeView> rootTasksView = rootTasks.stream()
				.map(this::toTasksView)
				.collect(Collectors.toList());

		return TasksTreeView.builder()
				.rootTasksView(rootTasksView)
				.build();
	}

	private TaskTreeView toTasksView(Task task) {
		String name = task.getName();
		List<Task> subTasks = task.getSubTasks();

		List<TaskTreeView> subTasksView = subTasks.stream()
				.map(this::toTasksView)
				.collect(Collectors.toList());

		return TaskTreeView.builder()
				.subTasks(subTasksView)
				.name(name)
				.build();
	}
}
