package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PeopleViewMetricsCalculatorTest {

    @Test
    public void taskWorkingLogMetricsShouldBeAggregatedByUser() {
        //given
        LocalDate day3 = LocalDate.of(2021, 1, 3);
        LocalDate day5 = LocalDate.of(2021, 1, 5);

        Task task1 = Task.builder().id("#1").build();
        Task task2 = Task.builder().id("#2").build();

        List<Task> tasks = List.of(task1, task2);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(List.of("person#1", "person#2"))
                .startPeriod(day3)
                .endPeriod(day5)
                .build();

        Map<String, TaskWorkingLogMetrics> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1"),
                "person#2", newTaskWorkingLogMetrics("#1", "Task name 1"));
        Map<String, TaskWorkingLogMetrics> personsMetricsForPeopleFromTask2 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#2", "Task name 2"));

        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskMetricsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        TaskMetricsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<PersonMetrics> expected = List.of(
                PersonMetrics.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogMetrics(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1"),
                                newTaskWorkingLogMetrics("#2", "Task name 2")))
                        .timeSpentByDaysForAllTask(Collections.emptyList())
                        .build(),
                PersonMetrics.builder()
                        .person("person#2")
                        .dailyTaskWorkingLogMetrics(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1")))
                        .timeSpentByDaysForAllTask(Collections.emptyList())
                        .build());

        //when
        PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(
                taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<PersonMetrics> actual = calculator.calculatePersonsMetrics(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void personsAbsentInFilterShouldBeExcludedFromPersonMetrics() {
        //given
        LocalDate day3 = LocalDate.of(2021, 1, 3);
        LocalDate day5 = LocalDate.of(2021, 1, 5);

        Task task1 = Task.builder().id("#1").build();

        List<Task> tasks = List.of(task1);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(List.of("person#1"))
                .startPeriod(day3)
                .endPeriod(day5)
                .build();

        Map<String, TaskWorkingLogMetrics> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1"),
                "person#2", newTaskWorkingLogMetrics("#1", "Task name 1"));

        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskMetricsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);

        TaskMetricsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<PersonMetrics> expected = List.of(
                PersonMetrics.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogMetrics(List.of(newTaskWorkingLogMetrics("#1", "Task name 1")))
                        .timeSpentByDaysForAllTask(Collections.emptyList())
                        .build()
        );

        //when
        PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<PersonMetrics> actual = calculator.calculatePersonsMetrics(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void timeSpentByDaysShouldBeAggregatedAndValidationServiceShouldBeCalled() {
        //given
        LocalDate day3 = LocalDate.of(2021, 1, 3);
        LocalDate day4 = LocalDate.of(2021, 1, 4);
        LocalDate day5 = LocalDate.of(2021, 1, 5);

        Task task1 = Task.builder().id("#1").build();
        Task task2 = Task.builder().id("#2").build();

        List<Task> tasks = List.of(task1, task2);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(List.of("person#1", "person#2"))
                .startPeriod(day3)
                .endPeriod(day5)
                .build();

        Map<String, TaskWorkingLogMetrics> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1",
                        List.of(newTimeSpentByDay(day3, 100),
                                newTimeSpentByDay(day4, 25),
                                newTimeSpentByDay(day5, 77))
                ));
        Map<String, TaskWorkingLogMetrics> personsMetricsForPeopleFromTask2 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#2", "Task name 2",
                        List.of(newTimeSpentByDay(day3, 10),
                                newTimeSpentByDay(day5, 77))
                ));

        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskMetricsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        TaskMetricsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<PersonMetrics> expected = List.of(
                PersonMetrics.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogMetrics(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1",
                                        List.of(newTimeSpentByDay(day3, 100),
                                                newTimeSpentByDay(day4, 25),
                                                newTimeSpentByDay(day5, 77))
                                ),
                                newTaskWorkingLogMetrics("#2", "Task name 2",
                                        List.of(newTimeSpentByDay(day3, 10),
                                                newTimeSpentByDay(day5, 77)))
                        ))
                        .timeSpentByDaysForAllTask(List.of(
                                ValueWithValidation.valueWithNotEvaluatedValidationStatus(newTimeSpentByDay(day3, 110)),
                                ValueWithValidation.valueWithNotEvaluatedValidationStatus(newTimeSpentByDay(day4, 25)),
                                ValueWithValidation.valueWithNotEvaluatedValidationStatus(newTimeSpentByDay(day5, 154))))
                        .build());

        //when
        PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(
                taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<PersonMetrics> actual = calculator.calculatePersonsMetrics(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private TaskMetricsForPeopleValidator mockTaskMetricsForPeopleValidator() {
        TaskMetricsForPeopleValidator taskMetricsForPeopleValidator = mock(TaskMetricsForPeopleValidator.class);
        when(taskMetricsForPeopleValidator.validate(any()))
                .then(invocation -> ValueWithValidation.valueWithNotEvaluatedValidationStatus(invocation.getArgument(0)));

        return taskMetricsForPeopleValidator;
    }

    private TaskWorkingLogMetrics newTaskWorkingLogMetrics(String taskId, String taskName) {
        return newTaskWorkingLogMetrics(taskId, taskName, Collections.emptyList());
    }

    private TaskWorkingLogMetrics newTaskWorkingLogMetrics(String taskId, String taskName, List<TimeSpentByDay> timeSpentByDays) {
        return TaskWorkingLogMetrics.builder()
                .taskId(taskId)
                .taskName(taskName)
                .timeSpentByDays(timeSpentByDays)
                .build();
    }

    private TimeSpentByDay newTimeSpentByDay(LocalDate day, int timeSpentMinutes) {
        return TimeSpentByDay.builder()
                .day(day)
                .timeSpentMinutes(timeSpentMinutes)
                .build();
    }
}