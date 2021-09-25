package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;

class TaskMetricsForPersonCalculatorTest {

	//TODO workLogs will be used on calculation of timeCoefficient
	@Test
	public void workLogsOutOfTimeRangeShouldBeIgnored() {
		//given
		LocalDate day3 = LocalDate.of(2021, 1, 3);
		LocalDate day6 = LocalDate.of(2021, 1, 6);

		LocalDate startPeriod = day3;
		LocalDate endPeriod = day6;

		List<String> teamMembers = List.of("person#1");

		TaskWorkLog workLog1 = buildTaskWorkLog("person#1", 51, LocalDateTime.of(2021, 1, 2, 12, 45));
		TaskWorkLog workLog2 = buildTaskWorkLog("person#1", 52, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog3 = buildTaskWorkLog("person#1", 53, LocalDateTime.of(2021, 1, 4, 17, 00));
		TaskWorkLog workLog4 = buildTaskWorkLog("person#1", 54, LocalDateTime.of(2021, 1, 5, 12, 45));
		TaskWorkLog workLog5 = buildTaskWorkLog("person#1", 55, LocalDateTime.of(2021, 1, 6, 12, 45));

		Task task = Task.builder()
				.id("#1")
				.name("Task name")
				.status("In progress")
				.originalEstimateMinutes(301)
				.remainingEstimateMinutes(51)
				.timeSpentMinutes(251)
				.workLogs(List.of(workLog1, workLog2, workLog3, workLog4, workLog5))
				.build();

		TaskMetrics taskMetrics = TaskMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.taskExternalStatus("In progress")
				.originalEstimateInMinutes(301)
				.timeSpendOnTaskInMinutes(251)
				.timeSpentOnTaskPersonByPeriodInMinutes(159)
				.spentTimePercentageForPerson(0f)
				.build();

		Map<String, TaskMetrics> expected = Map.of("person#1", taskMetrics);

		//when
		TaskMetricsForPersonCalculator calculator = new TaskMetricsForPersonCalculator();
		Map<String, TaskMetrics> actual = calculator.calculateTaskMetricsForPerson(task, startPeriod, endPeriod, teamMembers);

		//then
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void workLogsShouldBeAggregatedByDayForFewPerson() {
		//given
		LocalDate day3 = LocalDate.of(2021, 1, 3);
		LocalDate day6 = LocalDate.of(2021, 1, 6);

		LocalDate startPeriod = day3;
		LocalDate endPeriod = day6;

		List<String> teamMembers = List.of("person#1");

		TaskWorkLog workLog1 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog2 = buildTaskWorkLog("person#2", 51, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog3 = buildTaskWorkLog("person#2", 55, LocalDateTime.of(2021, 1, 4, 12, 45));
		TaskWorkLog workLog4 = buildTaskWorkLog("person#1", 53, LocalDateTime.of(2021, 1, 5, 12, 45));

		Task task = Task.builder()
				.id("#1")
				.name("Task name")
				.status("In progress")
				.originalEstimateMinutes(301)
				.remainingEstimateMinutes(51)
				.timeSpentMinutes(251)
				.workLogs(List.of(workLog1, workLog2, workLog3, workLog4))
				.build();

		TaskMetrics taskMetrics1 = TaskMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.taskExternalStatus("In progress")
				.originalEstimateInMinutes(301)
				.timeSpendOnTaskInMinutes(251)
				.timeSpentOnTaskPersonByPeriodInMinutes(103)
				.spentTimePercentageForPerson(0f)
				.build();

		TaskMetrics taskMetrics2 = TaskMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.taskExternalStatus("In progress")
				.originalEstimateInMinutes(301)
				.timeSpendOnTaskInMinutes(251)
				.timeSpentOnTaskPersonByPeriodInMinutes(106)
				.spentTimePercentageForPerson(0f)
				.build();

		Map<String, TaskMetrics> expected = Map.of(
				"person#1", taskMetrics1,
				"person#2", taskMetrics2);

		//when
		TaskMetricsForPersonCalculator calculator = new TaskMetricsForPersonCalculator();
		Map<String, TaskMetrics> actual = calculator.calculateTaskMetricsForPerson(task, startPeriod, endPeriod, teamMembers);

		//then
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	private TimeSpentByDay newTimeSpentByDay(LocalDate day, int timeSpentMinutes) {
		return TimeSpentByDay.builder()
				.day(day)
				.timeSpentMinutes(timeSpentMinutes)
				.build();
	}

	private TaskWorkLog buildTaskWorkLog(String person, int minutesSpent, LocalDateTime startDateTime) {
		return TaskWorkLog.builder()
				.person(person)
				.startDateTime(startDateTime)
				.minutesSpent(minutesSpent)
				.build();
	}
}