package ua.dp.dryzhyryk.big.brother.core;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksRootViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.*;

import java.util.List;
import java.util.Map;

@Slf4j
public class BigJiraBrother {

    private final JiraInformationHolder jiraInformationHolder;
    private final TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator;
    private final TasksRootViewMetricsCalculator tasksRootViewMetricsCalculator;
    private final SprintViewMetricsCalculator sprintViewMetricsCalculator;

    public BigJiraBrother(
            JiraInformationHolder jiraInformationHolder,
            TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator,
            TasksRootViewMetricsCalculator tasksRootViewMetricsCalculator,
            SprintViewMetricsCalculator sprintViewMetricsCalculator) {
        this.jiraInformationHolder = jiraInformationHolder;
        this.tasksTreeViewMetricsCalculator = tasksTreeViewMetricsCalculator;
        this.tasksRootViewMetricsCalculator = tasksRootViewMetricsCalculator;
        this.sprintViewMetricsCalculator = sprintViewMetricsCalculator;
    }

    public TasksTreeView prepareTasksTreeView(SprintSearchConditions sprintSearchConditions) {
        List<Task> rootTasks = jiraInformationHolder.getTasks(sprintSearchConditions);

        Map<String, TaskMetrics> taskMetricsByTaskId = tasksTreeViewMetricsCalculator.calculateFor(rootTasks);

        return TasksTreeView.builder()
                .project(sprintSearchConditions.getProject())
                .sprint(sprintSearchConditions.getSprint())
                .rootTasks(rootTasks)
                .taskMetricsByTaskId(taskMetricsByTaskId)
                .build();
    }

    public SprintView prepareSprintView(SprintSearchConditions sprintSearchConditions) {
        List<Task> rootTasks = jiraInformationHolder.getTasks(sprintSearchConditions);

        TaskTimeMetrics totalTasksTimeMetrics = sprintViewMetricsCalculator.calculateFor(rootTasks);


        return SprintView.builder()
                .project(sprintSearchConditions.getProject())
                .sprint(sprintSearchConditions.getSprint())
                .totalTasksTimeMetrics(totalTasksTimeMetrics)
                .build();
    }

    public TasksRootView prepareTasksRootView(SprintSearchConditions sprintSearchConditions) {
        List<Task> rootTasks = jiraInformationHolder.getTasks(sprintSearchConditions);

        Map<String, TaskMetrics> taskMetricsByTaskId = tasksRootViewMetricsCalculator.calculateFor(rootTasks);

        return TasksRootView.builder()
                .project(sprintSearchConditions.getProject())
                .sprint(sprintSearchConditions.getSprint())
                .rootTasks(rootTasks)
                .taskMetricsByTaskId(taskMetricsByTaskId)
                .build();
    }
}
