package ua.dp.dryzhyryk.big.brother.tests;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.model.ExamplesTable;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.base.BaseSteps;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ua.dp.dryzhyryk.big.brother.Utilities.valueAs;

public class JiraInformationHolderMockingSteps extends BaseSteps {

    private final JiraInformationHolderImpl jiraInformationHolderMock;
    private Map<String, Task> tasks = new HashMap<>();

    public JiraInformationHolderMockingSteps(JiraInformationHolderImpl jiraInformationHolderMock) {
        this.jiraInformationHolderMock = jiraInformationHolderMock;
    }

    @BeforeScenario
    public void beforeScenarioCleanup() {
        tasks = new HashMap<>();
        Mockito.clearInvocations(jiraInformationHolderMock);
    }

    @Given("cleanup jira response")
    public void cleanupJiraResponse() {
        tasks = new HashMap<>();
        Mockito.clearInvocations(jiraInformationHolderMock);
    }

    @Given("task: $task with work log: $workLog")
    public void setFileName(
            @Named("task") ExamplesTable taskTable,
            @Named("workLog") ExamplesTable workLogsTable) {

        List<TaskWorkLog> workLogs = workLogsTable.getRowsAsParameters().stream()
                .map(parameter -> TaskWorkLog.builder()
                        .person(parameter.valueAs("Person", String.class))
                        .startDateTime(parameter.valueAs("StartDateTime", LocalDateTime.class))
                        .minutesSpent(parameter.valueAs("MinutesSpent", Integer.class))
                        .build())
                .collect(Collectors.toList());

        Task task = taskTable.getRowsAsParameters().stream()
                .map(parameter -> Task.builder()
                        .id(parameter.valueAs("Id", String.class))
                        .name(parameter.valueAs("Name", String.class))
                        .status(parameter.valueAs("Status", String.class))
                        .type(parameter.valueAs("Type", String.class))
                        .isSubTask(parameter.valueAs("IsSubTask", Boolean.class))
                        .originalEstimateMinutes(valueAs(parameter, "OriginalEstimateMinutes", Integer.class, null))
                        .remainingEstimateMinutes(valueAs(parameter, "RemainingEstimateMinutes", Integer.class, null))
                        .timeSpentMinutes(valueAs(parameter, "TimeSpentMinutes", Integer.class, null))
                        .workLogs(workLogs)
                        .build())
                .findFirst()
                .orElseThrow();

        this.tasks.put(task.getId(), task);
    }

    @Given("request with response")
    public void givenRequestWithResponse() {
        List<Task> value = new ArrayList<>(tasks.values());

        Mockito.when(jiraInformationHolderMock.getTasks(Mockito.any(SearchConditions.class)))
                .thenReturn(value);
    }
}
