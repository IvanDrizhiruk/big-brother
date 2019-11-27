package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SprintSearchRequest;

import java.util.List;

public class ReportBySprintProcessor {
    private final BigJiraBrother bigJiraBrother;
    private final ExcelReportGenerator reportGenerator;

    public ReportBySprintProcessor(BigJiraBrother bigJiraBrother, ExcelReportGenerator reportGenerator) {
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
                    TasksTreeView tasksTreeView = bigJiraBrother.prepareTaskView(condition);
                    SprintView sprintView = bigJiraBrother.prepareSprintView(condition);
                    reportGenerator.generateReport(tasksTreeView, sprintView);
                });
    }

    private SprintSearchConditions toSprintSearchConditions(SprintSearchRequest sprintSearchRequest) {
        return SprintSearchConditions.builder()
                .project(sprintSearchRequest.getProject())
                .sprint(sprintSearchRequest.getSprint())
                .build();
    }
}