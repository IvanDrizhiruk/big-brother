package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TaskWorkingLogsForPeopleCalculator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TaskWorkingLogsForPeopleValidator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TasksWorkingLogsForPersonsCalculator;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogs;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TasksWorkingLogsForPersonsCalculatorTest {

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

        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                        List.of(newTimeSpentByDay(day3, 100))),
                "person#2", newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                        List.of(newTimeSpentByDay(day3, 100))));
        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask2 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#2", "Task name 2", 100,
                        List.of(newTimeSpentByDay(day3, 100))));

        TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskWorkingLogsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<TasksWorkingLogsForPerson> expected = List.of(
                TasksWorkingLogsForPerson.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogs(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                                        List.of(newTimeSpentByDay(day3, 100))),
                                newTaskWorkingLogMetrics("#2", "Task name 2", 100,
                                        List.of(newTimeSpentByDay(day3, 100)))))
                        .totalTimeSpentByDays(List.of(
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day3, 200))))
                        .totalTimeSpentOnTaskInMinutesByPeriod(200)
                        .build(),
                TasksWorkingLogsForPerson.builder()
                        .person("person#2")
                        .dailyTaskWorkingLogs(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                                        List.of(newTimeSpentByDay(day3, 100)))))
                        .totalTimeSpentByDays(List.of(
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day3, 100))))
                        .totalTimeSpentOnTaskInMinutesByPeriod(100)
                        .build());

        //when
        TasksWorkingLogsForPersonsCalculator calculator = new TasksWorkingLogsForPersonsCalculator(
                taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<TasksWorkingLogsForPerson> actual = calculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

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

        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                        List.of(newTimeSpentByDay(day3, 100))),
                "person#2", newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                        List.of(newTimeSpentByDay(day3, 100))));

        TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskWorkingLogsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);

        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<TasksWorkingLogsForPerson> expected = List.of(
                TasksWorkingLogsForPerson.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogs(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1", 100,
                                        List.of(newTimeSpentByDay(day3, 100)))))
                        .totalTimeSpentByDays(List.of(
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day3, 100))))
                        .totalTimeSpentOnTaskInMinutesByPeriod(100)
                        .build()
        );

        //when
        TasksWorkingLogsForPersonsCalculator
                calculator = new TasksWorkingLogsForPersonsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<TasksWorkingLogsForPerson> actual = calculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

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

        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1", 205,
                        List.of(newTimeSpentByDay(day3, 100),
                                newTimeSpentByDay(day4, 25),
                                newTimeSpentByDay(day5, 77))
                ));
        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask2 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#2", "Task name 2", 87,
                        List.of(newTimeSpentByDay(day3, 10),
                                newTimeSpentByDay(day5, 77))
                ));

        TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskWorkingLogsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        List<TasksWorkingLogsForPerson> expected = List.of(
                TasksWorkingLogsForPerson.builder()
                        .person("person#1")
                        .dailyTaskWorkingLogs(List.of(
                                newTaskWorkingLogMetrics("#1", "Task name 1", 205,
                                        List.of(newTimeSpentByDay(day3, 100),
                                                newTimeSpentByDay(day4, 25),
                                                newTimeSpentByDay(day5, 77))
                                ),
                                newTaskWorkingLogMetrics("#2", "Task name 2", 87,
                                        List.of(newTimeSpentByDay(day3, 10),
                                                newTimeSpentByDay(day5, 77)))
                        ))
                        .totalTimeSpentByDays(List.of(
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day3, 110)),
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day4, 25)),
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day5, 154))))
                        .totalTimeSpentOnTaskInMinutesByPeriod(289)
                        .build());

        //when
        TasksWorkingLogsForPersonsCalculator calculator = new TasksWorkingLogsForPersonsCalculator(
                taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<TasksWorkingLogsForPerson> actual = calculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void personWithoutActiveWorkLogByPeriodShouldNotPresentInResult() {
        //given
        LocalDate day1 = LocalDate.of(2021, 1, 1);
        LocalDate day3 = LocalDate.of(2021, 1, 3);
        LocalDate day5 = LocalDate.of(2021, 1, 5);
        LocalDate day7 = LocalDate.of(2021, 1, 7);

        Task task1 = Task.builder().id("#1").build();

        List<Task> tasks = List.of(task1);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(List.of("person#1"))
                .startPeriod(day3)
                .endPeriod(day5)
                .build();

        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1",
                        List.of(newTimeSpentByDay(day1, 100),
                                newTimeSpentByDay(day7, 77))));

        TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskWorkingLogsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);

        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        //when
        TasksWorkingLogsForPersonsCalculator
                calculator = new TasksWorkingLogsForPersonsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<TasksWorkingLogsForPerson> actual = calculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

        //then
        Assertions.assertThat(actual)
                .isEmpty();
    }

    @Test
    public void taskWorkingLogMetricsWithoutActiveWorkLogByPeriodShouldNotPresentInResult() {
        //given
        LocalDate day1 = LocalDate.of(2021, 1, 1);
        LocalDate day3 = LocalDate.of(2021, 1, 3);
        LocalDate day5 = LocalDate.of(2021, 1, 5);
        LocalDate day7 = LocalDate.of(2021, 1, 7);

        Task task1 = Task.builder().id("#1").build();
        Task task2 = Task.builder().id("#2").build();

        List<Task> tasks = List.of(task1, task2);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(List.of("person#1", "person#2"))
                .startPeriod(day3)
                .endPeriod(day5)
                .build();

        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask1 = Map.of(
                "person#1", newTaskWorkingLogMetrics("#1", "Task name 1", 0,
                        List.of(newTimeSpentByDay(day1, 100),
                                newTimeSpentByDay(day7, 77))));
        Map<String, TaskWorkingLogs> personsMetricsForPeopleFromTask2 = Map.of(
                "person#2", newTaskWorkingLogMetrics("#2", "Task name 2", 100,
                        List.of(newTimeSpentByDay(day3, 100))));

        TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = mock(TaskWorkingLogsForPeopleCalculator.class);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task1), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask1);
        when(taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(eq(task2), any(), any()))
                .thenReturn(personsMetricsForPeopleFromTask2);

        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mockTaskMetricsForPeopleValidator();

        //when
        TasksWorkingLogsForPersonsCalculator
                calculator = new TasksWorkingLogsForPersonsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);
        List<TasksWorkingLogsForPerson> actual = calculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

        List<TasksWorkingLogsForPerson> expected = List.of(
                TasksWorkingLogsForPerson.builder()
                        .person("person#2")
                        .dailyTaskWorkingLogs(List.of(
                                newTaskWorkingLogMetrics("#2", "Task name 2", 100,
                                        List.of(newTimeSpentByDay(day3, 100)))
                        ))
                        .totalTimeSpentByDays(List.of(
                                ValidatedValue.valueWithNotEvaluatedStatus(newTimeSpentByDay(day3, 100))))
                        .totalTimeSpentOnTaskInMinutesByPeriod(100)
                        .build());

        //then
        Assertions.assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private TaskWorkingLogsForPeopleValidator mockTaskMetricsForPeopleValidator() {
        TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = mock(TaskWorkingLogsForPeopleValidator.class);
        when(taskMetricsForPeopleValidator.validate(any(), any()))
                .then(invocation -> ValidatedValue.valueWithNotEvaluatedStatus(invocation.getArgument(0)));

        return taskMetricsForPeopleValidator;
    }

    private TaskWorkingLogs newTaskWorkingLogMetrics(String taskId, String taskName) {
        return newTaskWorkingLogMetrics(taskId, taskName, Collections.emptyList());
    }

    private TaskWorkingLogs newTaskWorkingLogMetrics(String taskId, String taskName, List<TimeSpentByDay> timeSpentByDays) {
        return TaskWorkingLogs.builder()
                .taskId(taskId)
                .taskName(taskName)
                .timeSpentByDays(timeSpentByDays)
                .build();
    }

    private TaskWorkingLogs newTaskWorkingLogMetrics(
            String taskId,
            String taskName,
            int timeSpentOnTaskInMinutesByPeriod,
            List<TimeSpentByDay> timeSpentByDays) {
        return newTaskWorkingLogMetrics(taskId, taskName, timeSpentByDays).toBuilder()
                .timeSpentOnTaskByPeriodInMinutes(timeSpentOnTaskInMinutesByPeriod)
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