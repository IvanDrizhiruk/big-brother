package ua.dp.dryzhyryk.big.brother;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.PeopleViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

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
		PeopleViewMetricsCalculator peopleViewMetricsCalculator = new PeopleViewMetricsCalculator();
		SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
		bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator, peopleViewMetricsCalculator,
				sprintViewMetricsCalculator);
	}

	@Test
	public void dataShouldBeReceivedFromJiraResourceAndCashed() {
		//given
		SprintSearchConditions searchConditionsFirst = TestDoublesForProjectSprintMega.newSearchConditionsMega();
		SprintSearchConditions searchConditionsSecond = TestDoublesForProjectSprintMega.newSearchConditionsMega();

		when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
				.thenReturn(null);

		when(jiraResource.loadTasks(Mockito.any(SprintSearchConditions.class)))
				.thenReturn(TestDoublesForProjectSprintMega.newProjectSprintMega());

		TasksTreeView expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

		//when
		TasksTreeView actualFirst = bigJiraBrother.prepareTaskView(searchConditionsFirst);
		TasksTreeView actualSecond = bigJiraBrother.prepareTaskView(searchConditionsSecond);

		//then
		verify(jiraResource, times(1)).loadTasks(searchConditionsFirst);
		verify(jiraDataStorage, times(1)).loadTasks(searchConditionsFirst);

		Assertions.assertEquals(expectedResult, actualFirst);
		Assertions.assertEquals(actualFirst, actualSecond);
	}

	@Test
	public void dataShouldBeReceivedFromJiraStorageAndCashed() {
		//given
		SprintSearchConditions searchConditionsFirst = TestDoublesForProjectSprintMega.newSearchConditionsMega();
		SprintSearchConditions searchConditionsSecond = TestDoublesForProjectSprintMega.newSearchConditionsMega();

		when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
				.thenReturn(TestDoublesForProjectSprintMega.newProjectSprintMega());

		TasksTreeView expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

		//when
		TasksTreeView actualFirst = bigJiraBrother.prepareTaskView(searchConditionsFirst);
		TasksTreeView actualSecond = bigJiraBrother.prepareTaskView(searchConditionsSecond);

		//then
		verify(jiraDataStorage, times(1)).loadTasks(searchConditionsFirst);
		verify(jiraResource, never()).loadTasks(searchConditionsFirst);

		Assertions.assertEquals(expectedResult, actualFirst);
		Assertions.assertEquals(actualFirst, actualSecond);
	}

	@Test
	public void dataReceivedFromJiraResourceShouldBeStoredToJiraStorage() {
		//given
		SprintSearchConditions searchConditions = TestDoublesForProjectSprintMega.newSearchConditionsMega();

		List<Task> jiraTasks = TestDoublesForProjectSprintMega.newProjectSprintMega();

		when(jiraDataStorage.loadTasks(Mockito.any(SprintSearchConditions.class)))
				.thenReturn(null);

		when(jiraResource.loadTasks(Mockito.any(SprintSearchConditions.class)))
				.thenReturn(jiraTasks);

		TasksTreeView expectedResult = TestDoublesForProjectSprintMega.newTaskViewForProjectSprintMega();

		//when
		TasksTreeView actualFirst = bigJiraBrother.prepareTaskView(searchConditions);

		//then
		verify(jiraDataStorage, times(1)).saveProjectSprint(searchConditions, jiraTasks);
		verify(jiraResource, times(1)).loadTasks(searchConditions);

		Assertions.assertEquals(expectedResult, actualFirst);
	}
}
