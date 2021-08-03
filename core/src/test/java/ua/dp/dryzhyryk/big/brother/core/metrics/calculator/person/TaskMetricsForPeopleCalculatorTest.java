package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

class TaskMetricsForPeopleCalculatorTest {

	@Test
	public void workLogsOutOfTimeRangeShouldBeIgnored() {
		//given
		LocalDate day3 = LocalDate.of(2021, 1, 3);
		LocalDate day4 = LocalDate.of(2021, 1, 4);
		LocalDate day5 = LocalDate.of(2021, 1, 5);
		LocalDate day6 = LocalDate.of(2021, 1, 6);

		LocalDate startPeriod = day3;
		LocalDate endPeriod = day6;

		TaskWorkLog workLog1 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 2, 12, 45));
		TaskWorkLog workLog2 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog3 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 4, 17, 00));
		TaskWorkLog workLog4 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 5, 12, 45));
		TaskWorkLog workLog5 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 6, 12, 45));

		Task task = Task.builder()
				.id("#1")
				.name("Task name")
				.originalEstimateMinutes(301)
				.remainingEstimateMinutes(51)
				.timeSpentMinutes(251)
				.workLogs(List.of(workLog1, workLog2, workLog3, workLog4, workLog5))
				.build();

		TaskWorkingLogMetrics taskWorkingLogMetrics1 = TaskWorkingLogMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.timeSpentByDays(List.of(
						newTimeSpentByDay(day3, 50),
						newTimeSpentByDay(day4, 50),
						newTimeSpentByDay(day5, 50)))
				.timeSpentOnTaskInMinutes(ValueWithValidation.valueWithNotEvaluatedValidationStatus(250))
				.timeSpentOnTaskInMinutesByPeriod(ValueWithValidation.valueWithNotEvaluatedValidationStatus(150))
				.build();

		PersonMetrics personMetrics1 = PersonMetrics.builder()
				.person("person#1")
				.dailyTaskWorkingLogMetrics(
						List.of(taskWorkingLogMetrics1))
				.build();

		List<PersonMetrics> expected = List.of(personMetrics1);

		//when
		TaskMetricsForPeopleCalculator calculator = new TaskMetricsForPeopleCalculator();
		List<PersonMetrics> actual = calculator.toPersonsMetricsForTask(task, startPeriod, endPeriod);

		//then
		Assertions.assertThat(actual)
				.containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void workLogsShouldBeAggregatedByDayForFewPerson() {
		//given
		LocalDate day3 = LocalDate.of(2021, 1, 3);
		LocalDate day4 = LocalDate.of(2021, 1, 4);
		LocalDate day5 = LocalDate.of(2021, 1, 5);
		LocalDate day6 = LocalDate.of(2021, 1, 6);

		LocalDate startPeriod = day3;
		LocalDate endPeriod = day6;

		TaskWorkLog workLog1 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog2 = buildTaskWorkLog("person#2", 50, LocalDateTime.of(2021, 1, 3, 12, 45));
		TaskWorkLog workLog3 = buildTaskWorkLog("person#2", 50, LocalDateTime.of(2021, 1, 4, 12, 45));
		TaskWorkLog workLog4 = buildTaskWorkLog("person#1", 50, LocalDateTime.of(2021, 1, 5, 12, 45));

		Task task = Task.builder()
				.id("#1")
				.name("Task name")
				.originalEstimateMinutes(301)
				.remainingEstimateMinutes(51)
				.timeSpentMinutes(251)
				.workLogs(List.of(workLog1, workLog2, workLog3, workLog4))
				.build();

		TaskWorkingLogMetrics taskWorkingLogMetrics1 = TaskWorkingLogMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.timeSpentByDays(List.of(
						newTimeSpentByDay(day3, 50),
						newTimeSpentByDay(day5, 50)))
				.timeSpentOnTaskInMinutes(ValueWithValidation.valueWithNotEvaluatedValidationStatus(100))
				.timeSpentOnTaskInMinutesByPeriod(ValueWithValidation.valueWithNotEvaluatedValidationStatus(100))
				.build();

		PersonMetrics personMetrics1 = PersonMetrics.builder()
				.person("person#1")
				.dailyTaskWorkingLogMetrics(
						List.of(taskWorkingLogMetrics1))
				.build();

		TaskWorkingLogMetrics taskWorkingLogMetrics2 = TaskWorkingLogMetrics.builder()
				.taskId("#1")
				.taskName("Task name")
				.timeSpentByDays(List.of(
						newTimeSpentByDay(day3, 50),
						newTimeSpentByDay(day4, 50)))
				.timeSpentOnTaskInMinutes(ValueWithValidation.valueWithNotEvaluatedValidationStatus(100))
				.timeSpentOnTaskInMinutesByPeriod(ValueWithValidation.valueWithNotEvaluatedValidationStatus(100))
				.build();

		PersonMetrics personMetrics2 = PersonMetrics.builder()
				.person("person#2")
				.dailyTaskWorkingLogMetrics(
						List.of(taskWorkingLogMetrics2))
				.build();

		List<PersonMetrics> expected = List.of(personMetrics1, personMetrics2);

		//when
		TaskMetricsForPeopleCalculator calculator = new TaskMetricsForPeopleCalculator();
		List<PersonMetrics> actual = calculator.toPersonsMetricsForTask(task, startPeriod, endPeriod);

		//then
		Assertions.assertThat(actual)
				.containsExactlyInAnyOrderElementsOf(expected);
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