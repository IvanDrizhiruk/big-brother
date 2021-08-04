package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class PeopleViewMetricsCalculatorTest {

    // ignore users absent in filter

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

        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = Mockito.mock(TaskMetricsForPeopleCalculator.class);
        Mockito.when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        Mockito.when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        List<PersonMetrics> expected = List.of(
                PersonMetrics.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogMetrics(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1"),
                                newTaskWorkingLogMetrics("#2", "Task name 2")))
                        .build(),
                PersonMetrics.builder()
                        .person("person#2")
                        .dailyTaskWorkingLogMetrics(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1")))
                        .build());

        //when
        PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(taskMetricsForPeopleCalculator);
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

        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = Mockito.mock(TaskMetricsForPeopleCalculator.class);
        Mockito.when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);


        List<PersonMetrics> expected = List.of(
                PersonMetrics.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogMetrics(List.of(newTaskWorkingLogMetrics("#1", "Task name 1")))
                        .build()
        );

        //when
        PeopleViewMetricsCalculator calculator = new PeopleViewMetricsCalculator(taskMetricsForPeopleCalculator);
        List<PersonMetrics> actual = calculator.calculatePersonsMetrics(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private TaskWorkingLogMetrics newTaskWorkingLogMetrics(String taskId, String taskName) {
        return TaskWorkingLogMetrics.builder()
                .taskId(taskId)
                .taskName(taskName)
                .build();
    }
}