package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

class PeopleViewMetricsCalculatorTest {

	// ignore users absent in filter
	// aggregate data by user

	@Test
	public void workLogsOutOfTimeRangeShouldBeIgnored() {
		//given

		TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = Mockito.mock(TaskMetricsForPeopleCalculator.class);

		Task task1 = Task.builder().id("#1").build();
		Task task2 = Task.builder().id("#1").build();

		List<Task> tasks = List.of(task1, task2);

		PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder().build();





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
		PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(taskMetricsForPeopleCalculator);

		List<PersonMetrics> actual = calculator.calculatePersonsMetrics(tasks, peopleSearchConditions);

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

}