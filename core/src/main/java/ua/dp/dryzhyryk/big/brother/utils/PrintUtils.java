package ua.dp.dryzhyryk.big.brother.utils;

import org.slf4j.Logger;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;

import java.util.List;

public class PrintUtils {

    public static void printTasks(List<Task> tasksFromResource, Logger log) {

        tasksFromResource.forEach(task -> {
            log.debug("\n"
                    + "===================================================\n"
                    + "=== Task \n"
                    + "===================================================\n");
            printTask(task, log);

            log.debug("\n=== Worklog");
            printLogs(task.getWorkLogs(), log);

            if (!task.getSubTasks().isEmpty()) {
                log.debug("\n=== Sub Task \n");
                printSubTasks(task.getSubTasks(), log);
            }
            log.debug("\n");
        });
    }

    private static void printTask(Task task, Logger log) {
        log.debug(
                String.join("|",
                        "",
                        "Id",
                        "Name",
                        "Status",
                        "Type",
                        "SubTask",
                        "OriginalEstimateMinutes",
                        "RemainingEstimateMinutes",
                        "TimeSpentMinutes",
                        ""
                ));

        log.debug(
                String.join("|",
                        "",
                        task.getId(),
                        task.getName(),
                        task.getStatus(),
                        task.getType(),
                        String.valueOf(task.isSubTask()),
                        String.valueOf(task.getOriginalEstimateMinutes()),
                        String.valueOf(task.getRemainingEstimateMinutes()),
                        String.valueOf(task.getTimeSpentMinutes()),
                        ""
                ));
    }

    private static void printSubTasks(List<Task> subTasks, Logger log) {
        subTasks.forEach(subTask -> {
            printTask(subTask, log);
            printLogs(subTask.getWorkLogs(), log);
        });
    }

    private static void printLogs(List<TaskWorkLog> workLogs, Logger log) {
        workLogs.forEach(workLog -> {
                    log.debug(
                            String.join("|",
                                    "",
                                    "Person",
                                    "StartDateTime",
                                    "MinutesSpent",
                                    ""
                            ));
                    log.debug(
                            String.join("|",
                                    "",
                                    workLog.getPerson(),
                                    workLog.getStartDateTime().toString(),
                                    String.valueOf(workLog.getMinutesSpent()),
                                    ""
                            ));
                }
        );
    }
}
