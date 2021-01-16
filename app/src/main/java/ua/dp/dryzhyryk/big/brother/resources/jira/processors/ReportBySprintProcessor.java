package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksRootView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SprintSearchRequest;

import java.util.List;

//FIXME move to core
public class ReportBySprintProcessor {
    private final BigJiraBrother bigJiraBrother;
    private final ReportGenerator reportGenerator;

    public ReportBySprintProcessor(BigJiraBrother bigJiraBrother, ReportGenerator reportGenerator) {
        this.bigJiraBrother = bigJiraBrother;
        this.reportGenerator = reportGenerator;
    }

    public void prepareReportBySprint(List<SprintSearchRequest> sprintSearchRequest) {
        if (null == sprintSearchRequest) {
            return;
        }

        sprintSearchRequest.stream()
                .map(this::toSprintSearchConditions)
                .forEach(condition -> {
                    TasksTreeView tasksTreeView = bigJiraBrother.prepareTasksTreeView(condition);
                    SprintView sprintView = bigJiraBrother.prepareSprintView(condition);
                    TasksRootView tasksRootView = bigJiraBrother.prepareTasksRootView(condition);
                    reportGenerator.generateReport(tasksTreeView, sprintView, tasksRootView);
                });
    }

    private SprintSearchConditions toSprintSearchConditions(SprintSearchRequest sprintSearchRequest) {
        return SprintSearchConditions.builder()
                .project(sprintSearchRequest.getProject())
                .sprint(sprintSearchRequest.getSprint())
                .build();
    }
}