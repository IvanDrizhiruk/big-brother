package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO delete me
public class PeopleViewMetricsCalculatorOld {

    public List<PersonMetrics> calculateFor(List<Task> tasks, PeopleSearchConditions peopleSearchConditions) {
        Map<String, PersonMetrics> personMetricsByUser = tasks.stream()
                .flatMap(task -> toPersonsMetricsForTask(task, peopleSearchConditions))
                .filter(personMetrics -> isExcludePersonFromPersonMetrics(personMetrics, peopleSearchConditions.getPeopleNames()))
                .collect(
                        Collectors.toMap(
                                PersonMetrics::getPerson,
                                Function.identity(),
                                this::mergePersonMetricsForOnePerson));

        return new ArrayList<>(personMetricsByUser.values());
    }

    private boolean isExcludePersonFromPersonMetrics(PersonMetrics personMetrics, List<String> availablePersons) {
        return availablePersons.contains(personMetrics.getPerson());
    }

    private Stream<PersonMetrics> toPersonsMetricsForTask(Task task, PeopleSearchConditions peopleSearchConditions) {

        Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = task.getWorkLogs().stream()
                .collect(
                        Collectors.groupingBy(
                                TaskWorkLog::getPerson,
                                Collectors.groupingBy(
                                        taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
                                        Collectors.summingInt(TaskWorkLog::getMinutesSpent))));

        return spendTimeByDayForPerson.entrySet().stream()
                .map(entry -> {
                    TaskWorkingLogMetrics dailyTaskLogs = toTaskWorkingLogMetrics(entry.getValue(), task, peopleSearchConditions);
                    List<TimeSpentByDay> totalTimeSpentByDay = dailyTaskLogs.getTimeSpentByDays();

                    int totalTimeSpentOnTaskInMinutes = dailyTaskLogs.getTotalTimeSpentOnTaskInMinutes();
                    int totalTimeSpentInCurrentPeriodInMinutes = dailyTaskLogs.getTotalTimeSpentByPeriodInMinutes();

                    return PersonMetrics.builder()
                            .person(entry.getKey())
                            .dailyTaskLogs(Collections.singletonList(dailyTaskLogs))
                            .totalTimeSpentByDay(totalTimeSpentByDay)
                            .totalTimeSpentInCurrentPeriodInMinutes(totalTimeSpentInCurrentPeriodInMinutes)
                            .totalTimeSpentOnTaskInMinutes(totalTimeSpentOnTaskInMinutes)
                            .build();
                });
    }

    private PersonMetrics mergePersonMetricsForOnePerson(PersonMetrics x, PersonMetrics y) {
        List<TaskWorkingLogMetrics> dailyTaskLogs = Stream.of(x.getDailyTaskLogs(), y.getDailyTaskLogs())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Collection<TimeSpentByDay> totalTimeSpentByDay = Stream.of(x.getTotalTimeSpentByDay(), y.getTotalTimeSpentByDay())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        TimeSpentByDay::getDay,
                        Function.identity(),
                        (a, b) -> a.toBuilder()
                                .timeSpentMinutes(a.getTimeSpentMinutes() + b.getTimeSpentMinutes())
                                .build())).values();

        int totalTimeSpentOnTaskInMinutes =
                x.getTotalTimeSpentOnTaskInMinutes() + y.getTotalTimeSpentOnTaskInMinutes();
        int totalTimeSpentInCurrentPeriodInMinutes =
                x.getTotalTimeSpentInCurrentPeriodInMinutes() + y.getTotalTimeSpentInCurrentPeriodInMinutes();

        return x.toBuilder()
                .dailyTaskLogs(dailyTaskLogs)
                .totalTimeSpentByDay(new ArrayList<>(totalTimeSpentByDay))
                .totalTimeSpentInCurrentPeriodInMinutes(totalTimeSpentInCurrentPeriodInMinutes)
                .totalTimeSpentOnTaskInMinutes(totalTimeSpentOnTaskInMinutes)
                .build();
    }

    private TaskWorkingLogMetrics toTaskWorkingLogMetrics(
            Map<LocalDate, Integer> spentMinutesForDay, Task task, PeopleSearchConditions peopleSearchConditions) {

        int minutesSpent = spentMinutesForDay.values().stream().mapToInt(i -> i).sum();

        int originalEstimateMinutes = Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0);
        int timeSpentMinutes = Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0);

        float timeCoefficient =
                0 == timeSpentMinutes
                        ? 0
                        : ((float) originalEstimateMinutes) / timeSpentMinutes;

        List<TimeSpentByDay> timeSpentByDays = spentMinutesForDay.entrySet().stream()
                .map(entry -> TimeSpentByDay.builder()
                        .day(entry.getKey())
                        .timeSpentMinutes(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        int totalTimeSpentOnTaskInMinutes = timeSpentByDays
                .stream()
                .mapToInt(TimeSpentByDay::getTimeSpentMinutes)
                .sum();

        int totalTimeSpentByPeriodInMinutes = timeSpentByDays
                .stream()
                .filter(timeSpentByDay -> (!timeSpentByDay.getDay().isBefore(peopleSearchConditions.getStartPeriod()))
                        && timeSpentByDay.getDay().isBefore(peopleSearchConditions.getEndPeriod()))
                .mapToInt(TimeSpentByDay::getTimeSpentMinutes)
                .sum();

        return TaskWorkingLogMetrics.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskExternalStatus(task.getStatus())
                .timeSpentByDays(timeSpentByDays)
                .totalTimeSpentByPeriodInMinutes(totalTimeSpentByPeriodInMinutes)
                .totalTimeSpentOnTaskInMinutes(totalTimeSpentOnTaskInMinutes)
                .timeSpentMinutes(minutesSpent)
                .originalEstimateMinutes(originalEstimateMinutes)
                .timeCoefficient(timeCoefficient)
                .build();
    }
}
