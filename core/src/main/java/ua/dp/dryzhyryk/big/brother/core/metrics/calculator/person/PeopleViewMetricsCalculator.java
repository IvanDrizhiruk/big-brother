package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PeopleViewMetricsCalculator {

    private final TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator;
    private final TaskMetricsForPeopleValidator taskMetricsForPeopleValidator;

    public PeopleViewMetricsCalculator(
            TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator,
            TaskMetricsForPeopleValidator taskMetricsForPeopleValidator) {
        this.taskMetricsForPeopleCalculator = taskMetricsForPeopleCalculator;
        this.taskMetricsForPeopleValidator = taskMetricsForPeopleValidator;
    }

    public List<PersonMetrics> calculatePersonsMetrics(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
        Map<String, List<TaskWorkingLogMetrics>> personMetricsByUser = tasks.stream()
                .flatMap(task ->
                        {
                            Map<String, TaskWorkingLogMetrics> stringTaskWorkingLogMetricsMap = taskMetricsForPeopleCalculator.calculatePersonsMetricsForPeopleFromTask(
                                    task,
                                    peopleSearchConditions.getStartPeriod(),
                                    peopleSearchConditions.getEndPeriod());

                            return stringTaskWorkingLogMetricsMap
                                    .entrySet().stream();
                        }
                )
                .filter(taskWorkingLogMetricsForUser -> isExcludePersonFromPersonMetrics(taskWorkingLogMetricsForUser.getKey(),
                        peopleSearchConditions.getPeopleNames()))
                .collect(Collectors.groupingBy(
                        Entry::getKey,
                        Collectors.mapping(Entry::getValue, Collectors.toList())));

        return personMetricsByUser.entrySet().stream()
                .map(entry -> {
                    String person = entry.getKey();

                    List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics = entry.getValue().stream()
                            .sorted(Comparator.comparing(TaskWorkingLogMetrics::getTaskId))
                            .collect(Collectors.toList());

                    List<ValueWithValidation<TimeSpentByDay>> timeSpentByDaysForAllTask =
                            calculateTimeSpentByDaysForAllTask(dailyTaskWorkingLogMetrics);

                    return PersonMetrics.builder()
                            .person(person)
                            .dailyTaskWorkingLogMetrics(dailyTaskWorkingLogMetrics)
                            .timeSpentByDaysForAllTask(timeSpentByDaysForAllTask)
                            .build();
                })
                .sorted(Comparator.comparing(PersonMetrics::getPerson))
                .collect(Collectors.toList());
    }

    private List<ValueWithValidation<TimeSpentByDay>> calculateTimeSpentByDaysForAllTask(
            List<TaskWorkingLogMetrics> dailyTaskWorkingLogMetrics) {
        Map<LocalDate, Integer> timeSpentForAllTasksByDay = dailyTaskWorkingLogMetrics.stream()
                .flatMap(metrics -> metrics.getTimeSpentByDays().stream())
                .collect(Collectors.groupingBy(
                        TimeSpentByDay::getDay,
                        Collectors.summingInt(TimeSpentByDay::getTimeSpentMinutes)
                ));

        return timeSpentForAllTasksByDay.entrySet().stream()
                .sorted(Entry.comparingByKey())
                .map(dayAndSpentTime -> {
                    TimeSpentByDay timeSpentByDay = TimeSpentByDay.builder()
                            .day(dayAndSpentTime.getKey())
                            .timeSpentMinutes(dayAndSpentTime.getValue())
                            .build();
                    return taskMetricsForPeopleValidator.validate(timeSpentByDay);
                })
                .collect(Collectors.toList());
    }

    private boolean isExcludePersonFromPersonMetrics(String person, List<String> availablePersons) {
        return availablePersons.contains(person);
    }
}
