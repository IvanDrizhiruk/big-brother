package ua.dp.dryzhyryk.big.brother;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksRootViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.util.List;

import static org.mockito.Mockito.*;
import static ua.dp.dryzhyryk.big.brother.TestDoublesForProjectSprintMega.*;

@ExtendWith(MockitoExtension.class)
class JiraCacheTests {

    @Mock
    private JiraResource jiraResource;
    @Mock
    private JiraDataStorage jiraDataStorage;

    private BigJiraBrother bigJiraBrother;

    @BeforeEach
    private void beforeEachTest() {
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraResource, jiraDataStorage);
        TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator = new TasksTreeViewMetricsCalculator();
        TasksRootViewMetricsCalculator tasksRootViewMetricsCalculator = new TasksRootViewMetricsCalculator();
        SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
        bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator,
                tasksRootViewMetricsCalculator, sprintViewMetricsCalculator);
    }

    @Test
    public void dataShouldBeReceivedFromJiraResourceAndCashed() {
        //given
        SprintSearchConditions searchConditionsFirst = newSearchConditionsMega();
        SprintSearchConditions searchConditionsSecond = newSearchConditionsMega();

        when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(null);

        when(jiraResource.loadTasks(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(newProjectSprintMega());

        TasksTreeView expectedResult = newTaskViewForProjectSprintMega();

        //when
        TasksTreeView actualFirst = bigJiraBrother.prepareTasksTreeView(searchConditionsFirst);
        TasksTreeView actualSecond = bigJiraBrother.prepareTasksTreeView(searchConditionsSecond);

        //then
        verify(jiraResource, times(1)).loadTasks(searchConditionsFirst);
        verify(jiraDataStorage, times(1)).loadTasks(searchConditionsFirst);

        Assertions.assertEquals(expectedResult, actualFirst);
        Assertions.assertEquals(actualFirst, actualSecond);
    }

    @Test
    public void dataShouldBeReceivedFromJiraStorageAndCashed() {
        //given
        SprintSearchConditions searchConditionsFirst = newSearchConditionsMega();
        SprintSearchConditions searchConditionsSecond = newSearchConditionsMega();

        when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(newProjectSprintMega());

        TasksTreeView expectedResult = newTaskViewForProjectSprintMega();

        //when
        TasksTreeView actualFirst = bigJiraBrother.prepareTasksTreeView(searchConditionsFirst);
        TasksTreeView actualSecond = bigJiraBrother.prepareTasksTreeView(searchConditionsSecond);

        //then
        verify(jiraDataStorage, times(1)).loadTasks(searchConditionsFirst);
        verify(jiraResource, never()).loadTasks(searchConditionsFirst);

        Assertions.assertEquals(expectedResult, actualFirst);
        Assertions.assertEquals(actualFirst, actualSecond);
    }

    @Test
    public void dataReceivedFromJiraResourceShouldBeStoredToJiraStorage() {
        //given
        SprintSearchConditions searchConditions = newSearchConditionsMega();
        List<Task> jiraTasks = newProjectSprintMega();
        when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(null);
        when(jiraResource.loadTasks(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(jiraTasks);

        TasksTreeView expectedResult = newTaskViewForProjectSprintMega();

        //when
        TasksTreeView actualFirst = bigJiraBrother.prepareTasksTreeView(searchConditions);

        //then
        verify(jiraDataStorage, times(1))
                .saveProjectSprint(searchConditions, jiraTasks);
        verify(jiraResource, times(1))
                .loadTasks(searchConditions);
        Assertions.assertEquals(expectedResult, actualFirst);
    }
}
