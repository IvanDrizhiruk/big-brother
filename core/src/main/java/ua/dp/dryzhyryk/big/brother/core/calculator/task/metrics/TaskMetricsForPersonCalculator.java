package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;

public class TaskMetricsForPersonCalculator {
	public Map<String, TaskMetrics> calculateTaskMetricsForPerson(Task task, LocalDate startPeriod, LocalDate endPeriod,
			List<String> teamMembers) {
		Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = task.getWorkLogs().stream()
				.collect(
						Collectors.groupingBy(
								TaskWorkLog::getPerson,
								Collectors.groupingBy(
										taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
										Collectors.summingInt(TaskWorkLog::getMinutesSpent))));

		int timeSpendOnTaskByTeamInMinutes = task.getWorkLogs().stream()
				.filter(workLog -> teamMembers.contains(workLog.getPerson()))
				.mapToInt(TaskWorkLog::getMinutesSpent)
				.sum();

		return spendTimeByDayForPerson.entrySet().stream()
				.collect(
						Collectors.toMap(
								Map.Entry::getKey,
								entry -> toTaskMetrics(task, entry.getValue(), startPeriod, endPeriod, timeSpendOnTaskByTeamInMinutes)));
	}

	private TaskMetrics toTaskMetrics(
			Task task,
			Map<LocalDate, Integer> spentMinutesForDay,
			LocalDate startPeriod,
			LocalDate endPeriod,
			int timeSpendOnTaskByTeamInMinutes) {

		int timeSpentOnTaskPersonByPeriodInMinutes = spentMinutesForDay.entrySet().stream()
				.filter(entry -> !entry.getKey().isBefore(startPeriod) && entry.getKey().isBefore(endPeriod))
				.mapToInt(Map.Entry::getValue)
				.sum();

		int timeSpentOnTaskPersonInMinutes = spentMinutesForDay.values().stream()
				.mapToInt(i -> i)
				.sum();



		Float spentTimePercentageForPerson = null;
		Float spentTimePercentageForTeam = null;

		if (task.getOriginalEstimateMinutes() != null) {
			float estimation = task.getOriginalEstimateMinutes();
			spentTimePercentageForPerson = 100f * timeSpentOnTaskPersonInMinutes / estimation;
			spentTimePercentageForTeam = 100f * timeSpendOnTaskByTeamInMinutes / estimation;
		}
//TODO support statuses and validation
//
//		if ("DONE".isEmpty()) {
//			String status;
//		} else if ("IN_PROGRESS".isEmpty()) {
//			String status;
//		} else if ("TODO".isEmpty()) {
//			String status = "error or rework";
//		}

		return TaskMetrics.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.taskExternalStatus(task.getStatus())
				.originalEstimateInMinutes(task.getOriginalEstimateMinutes())
				.timeSpentOnTaskPersonByPeriodInMinutes(timeSpentOnTaskPersonByPeriodInMinutes)
				.timeSpentOnTaskPersonInMinutes(timeSpentOnTaskPersonInMinutes)
				.timeSpendOnTaskByTeamInMinutes(timeSpendOnTaskByTeamInMinutes)
				.timeSpendOnTaskInMinutes(task.getTimeSpentMinutes())
				.spentTimePercentageForPerson(spentTimePercentageForPerson)
				.spentTimePercentageForTeam(spentTimePercentageForTeam)
				.build();
	}
}
