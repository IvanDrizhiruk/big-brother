package ua.dp.dryzhyryk.big.brother.core.utils;

import org.slf4j.Logger;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public final class PrintUtils {

    public static final String TABLE_COLUMN_DELIMITER = "|";

    private PrintUtils() {
        //do nothing
    }

    public static void printTasks(List<Task> tasksFromResource, Logger log) {
        if (log.isDebugEnabled()) {
            tasksFromResource.forEach(task -> {
                log.debug("===== Task =====");

                printTask(task, log);

                log.debug("===== Work log =====");
                printLogs(task.getWorkLogs(), log);

                if (!task.getSubTasks().isEmpty()) {
                    log.debug("===== Sub Task =====");
                    printSubTasks(task.getSubTasks(), log);
                }
            });
        }
    }

    private static void printTask(Task task, Logger log) {
        String taskTableHeaders = join(
                "Id",
                "Name",
                "Status",
                "Type",
                "SubTask",
                "OriginalEstimateMinutes",
                "RemainingEstimateMinutes",
                "TimeSpentMinutes");

        String taskTableValue = join(
                task.getId(),
                task.getName(),
                task.getStatus(),
                task.getType(),
                String.valueOf(task.isSubTask()),
                String.valueOf(task.getOriginalEstimateMinutes()),
                String.valueOf(task.getRemainingEstimateMinutes()),
                String.valueOf(task.getTimeSpentMinutes()));

        log.debug(taskTableHeaders);
        log.debug(taskTableValue);
    }

    private static void printSubTasks(List<Task> subTasks, Logger log) {
        subTasks.forEach(subTask -> {
            printTask(subTask, log);
            printLogs(subTask.getWorkLogs(), log);
        });
    }

    private static void printLogs(List<TaskWorkLog> workLogs, Logger log) {
        workLogs.forEach(workLog -> {
                    String workLogTableHeader = join(
                            "Person",
                            "StartDateTime",
                            "MinutesSpent");

                    String workLogTableValue = join(
                            workLog.getPerson(),
                            workLog.getStartDateTime().toString(),
                            String.valueOf(workLog.getMinutesSpent()));

                    log.debug(workLogTableHeader);
                    log.debug(workLogTableValue);
                }
        );
    }

    public static void printPeopleView(PeopleView peopleView, Logger log) {
        log.debug("===== People View =====");

        printPeopleViewCoreFields(peopleView, log);

        log.debug("===== Person Metrics =====");

        printPersonMetrics(peopleView.getPersonMetrics(), log);
    }

    private static void printPeopleViewCoreFields(PeopleView peopleView, Logger log) {
        String peopleViewTableHeader = join(
                "TeamName",
                "StartPeriod",
                "EndPeriod");

        String peopleViewTableValue = join(
                peopleView.getTeamName(),
                peopleView.getStartPeriod().toString(),
                peopleView.getEndPeriod().toString());

        log.debug(peopleViewTableHeader);
        log.debug(peopleViewTableValue);
    }

    private static void printPersonMetrics(List<PersonMetrics> personMetrics, Logger log) {

        personMetrics.forEach(personMetric -> {

            log.debug("===== Person =====");

            log.debug(join(
                    "Person",
                    "TotalTimeSpentInCurrentPeriodInMinutes",
                    "TotalTimeSpentOnTaskInMinutes"));

            log.debug(join(
                    personMetric.getPerson(),
                    String.valueOf(personMetric.getTotalTimeSpentInCurrentPeriodInMinutes()),
                    String.valueOf(personMetric.getTotalTimeSpentOnTaskInMinutes())));

            printTaskWorkingLogMetrics(personMetric.getDailyTaskLogs(), log);

            log.debug("===== Person. TimeSpentByDay Totals =====");
            printTimeSpentByDay(personMetric.getTotalTimeSpentByDay(), log);
        });

    }

    private static void printTaskWorkingLogMetrics(List<TaskWorkingLogMetrics> dailyTaskLogs, Logger log) {
        log.debug("=== Person. DailyTaskLogs ===");
        String dailyTaskLogTableHeader = join(
                "TaskId",
                "TotalTimeSpentByPeriodInMinutes",
                "TotalTimeSpentOnTaskInMinutes",
                "TimeSpentMinutes",
                "OriginalEstimateMinutes",
                "TimeCoefficient",
                "TaskName",
                "TaskExternalStatus");

        log.debug(dailyTaskLogTableHeader);

        dailyTaskLogs.forEach(dailyTaskLog ->
                log.debug(join(
                        dailyTaskLog.getTaskId(),
                        String.valueOf(dailyTaskLog.getTotalTimeSpentByPeriodInMinutes()),
                        String.valueOf(dailyTaskLog.getTotalTimeSpentOnTaskInMinutes()),
                        String.valueOf(dailyTaskLog.getTimeSpentMinutes()),
                        String.valueOf(dailyTaskLog.getOriginalEstimateMinutes()),
                        String.valueOf(dailyTaskLog.getTimeCoefficient()),
                        dailyTaskLog.getTaskName(),
                        dailyTaskLog.getTaskExternalStatus(),
                        ""
                )));

        log.debug("===== Person. DailyTaskLogs. TimeSpentByDay in Minutes =====");

        List<LocalDate> sortedAvailableDates = dailyTaskLogs.stream()
                .flatMap(dailyTaskLog -> dailyTaskLog.getTimeSpentByDays().stream())
                .map(TimeSpentByDay::getDay)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String dailySpentByDayTableHeader = join(
                "TaskId",
                sortedAvailableDates);

        log.debug(dailySpentByDayTableHeader);

        dailyTaskLogs.forEach(dailyTaskLog -> {
            Map<LocalDate, Integer> timeSpentByDays = dailyTaskLog.getTimeSpentByDays().stream()
                    .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));

            List<String> dailyTaskLogValues = sortedAvailableDates.stream()
                    .map(day -> Optional.ofNullable(timeSpentByDays.get(day)).map(String::valueOf).orElse(""))
                    .collect(Collectors.toList());

            log.debug(join(
                    dailyTaskLog.getTaskId(),
                    dailyTaskLogValues));
        });
    }

    private static void printTimeSpentByDay(List<TimeSpentByDay> totalTimeSpentByDay, Logger log) {

        List<LocalDate> sortedAvailableDates = totalTimeSpentByDay.stream()
                .map(TimeSpentByDay::getDay)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String tableHeaders = join(sortedAvailableDates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList()));

        log.debug(tableHeaders);

        Map<LocalDate, Integer> totalTimeSpentByDays = totalTimeSpentByDay.stream()
                .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));

        String totalTimeSpent = join(
                sortedAvailableDates.stream()
                        .map(day -> Optional.ofNullable(totalTimeSpentByDays.get(day)).map(String::valueOf).orElse(""))
                        .collect(Collectors.toList()));
        log.debug(totalTimeSpent);
    }

    public static String join(String element, Collection<?> elements) {

        List<String> elementsAsString = elements.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        List<String> joinedElements = new ArrayList<>();
        joinedElements.add(element);
        joinedElements.addAll(elementsAsString);
        return join(joinedElements);
    }

    public static String join(String... elements) {
        return join(Arrays.asList(elements));
    }

    public static String join(Collection<String> elements) {
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(TABLE_COLUMN_DELIMITER, TABLE_COLUMN_DELIMITER, TABLE_COLUMN_DELIMITER);
        elements.forEach(joiner::add);
        return joiner.toString();
    }
}
