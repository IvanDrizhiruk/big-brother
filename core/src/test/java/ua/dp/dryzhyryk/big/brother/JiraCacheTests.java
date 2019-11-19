package ua.dp.dryzhyryk.big.brother;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationCache;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraStorage;

import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class JiraCacheTests {

    @Mock
    private JiraResource jiraResource;
    @Mock
    private JiraStorage jiraStorage;

    private BigJiraBrother bigJiraBrother;

    @BeforeEach
    private void beforeEachTest() {
        JiraInformationCache jiraInformationCache = new JiraInformationCache(jiraResource, jiraStorage);
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraInformationCache);
        bigJiraBrother = new BigJiraBrother(jiraInformationHolder);
    }

    @Test
    public void dataShouldBeReceivedFromJiraResourceAndCashed() {
        //given
        SprintSearchConditions searchConditionsFirst = TestDoublesForProjectSprintMega.newSearchConditionsMega();
        SprintSearchConditions searchConditionsSecond = TestDoublesForProjectSprintMega.newSearchConditionsMega();

        when(jiraStorage.loadProjectSprint(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(null);

        when(jiraResource.loadProjectSprint(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(TestDoublesForProjectSprintMega.newProjectSprintMega());

        List<TasksTreeView> expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

        //when
        List<TasksTreeView> actualFirst = bigJiraBrother.prepareTaskView(searchConditionsFirst);
        List<TasksTreeView> actualSecond = bigJiraBrother.prepareTaskView(searchConditionsSecond);

        //then
        verify(jiraResource, times(1)).loadProjectSprint(searchConditionsFirst);
        verify(jiraStorage, times(1)).loadProjectSprint(searchConditionsFirst);

        Assertions.assertEquals(expectedResult, actualFirst);
        Assertions.assertEquals(actualFirst, actualSecond);
    }

    @Test
    public void dataShouldBeReceivedFromJiraStorageAndCashed() {
        //given
        SprintSearchConditions searchConditionsFirst = TestDoublesForProjectSprintMega.newSearchConditionsMega();
        SprintSearchConditions searchConditionsSecond = TestDoublesForProjectSprintMega.newSearchConditionsMega();

        when(jiraStorage.loadProjectSprint(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(TestDoublesForProjectSprintMega.newProjectSprintMega());

        List<TasksTreeView> expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

        //when
        List<TasksTreeView> actualFirst = bigJiraBrother.prepareTaskView(searchConditionsFirst);
        List<TasksTreeView> actualSecond = bigJiraBrother.prepareTaskView(searchConditionsSecond);

        //then
        verify(jiraStorage, times(1)).loadProjectSprint(searchConditionsFirst);
        verify(jiraResource, never()).loadProjectSprint(searchConditionsFirst);

        Assertions.assertEquals(expectedResult, actualFirst);
        Assertions.assertEquals(actualFirst, actualSecond);
    }

    @Test
    public void dataReceivedFromJiraResourceShouldBeStoredToJiraStorage() {
        //given
        SprintSearchConditions searchConditions = TestDoublesForProjectSprintMega.newSearchConditionsMega();

        List<Task> jiraTasks = TestDoublesForProjectSprintMega.newProjectSprintMega();

        when(jiraStorage.loadProjectSprint(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(null);

        when(jiraResource.loadProjectSprint(Mockito.any(SprintSearchConditions.class)))
                .thenReturn(jiraTasks);

        List<TasksTreeView> expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

        //when
        List<TasksTreeView> actualFirst = bigJiraBrother.prepareTaskView(searchConditions);

        //then
        verify(jiraStorage, times(1)).saveProjectSprint(searchConditions, jiraTasks);
        verify(jiraResource, times(1)).loadProjectSprint(searchConditions);

        Assertions.assertEquals(expectedResult, actualFirst);
    }
}
