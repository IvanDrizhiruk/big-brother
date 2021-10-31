package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
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

		int timeSpendOnTaskByTeamByPeriodInMinutes = task.getWorkLogs().stream()
				.filter(workLog -> teamMembers.contains(workLog.getPerson()))
				.filter(workLog -> !workLog.getStartDateTime().toLocalDate().isBefore(startPeriod)
						&& workLog.getStartDateTime().toLocalDate().isBefore(endPeriod))
				.mapToInt(TaskWorkLog::getMinutesSpent)
				.sum();

		return spendTimeByDayForPerson.entrySet().stream()
				.collect(
						Collectors.toMap(
								Map.Entry::getKey,
								entry -> toTaskMetrics(
										task,
										entry.getValue(),
										startPeriod,
										endPeriod,
										timeSpendOnTaskByTeamInMinutes,
										timeSpendOnTaskByTeamByPeriodInMinutes)));
	}

	private TaskMetrics toTaskMetrics(
			Task task,
			Map<LocalDate, Integer> spentMinutesForDay,
			LocalDate startPeriod,
			LocalDate endPeriod,
			int timeSpendOnTaskByTeamInMinutes,
			int timeSpendOnTaskByTeamByPeriodInMinutes) {

		int timeSpentOnTaskPersonByPeriodInMinutes = spentMinutesForDay.entrySet().stream()
				.filter(entry -> !entry.getKey().isBefore(startPeriod) && entry.getKey().isBefore(endPeriod))
				.mapToInt(Map.Entry::getValue)
				.sum();

		int timeSpentOnTaskPersonInMinutes = spentMinutesForDay.values().stream()
				.mapToInt(i -> i)
				.sum();

		Float spentTimePercentageForPerson = null;
		Float spentTimePercentageForTeam = null;

		if (task.getOriginalEstimateMinutes() != null && task.getOriginalEstimateMinutes() != 0) {
			float estimation = task.getOriginalEstimateMinutes();
			spentTimePercentageForPerson = 100f * timeSpentOnTaskPersonInMinutes / estimation;
			spentTimePercentageForTeam = 100f * timeSpendOnTaskByTeamInMinutes / estimation;
		}

		//TODO support statuses and validation
		// validate spend time
		//
		//		if ("DONE".isEmpty()) {
		//			String status;
		//		} else if ("IN_PROGRESS".isEmpty()) {
		//			String status;
		//		} else if ("TODO".isEmpty()) {
		//			String status = "error or rework";
		//		}

		boolean isEstimationPresent = task.getOriginalEstimateMinutes() == null;

		ValidatedValue<Integer> estimationMinutes = isEstimationPresent
				? ValidatedValue.valueWithErrorStatus(task.getOriginalEstimateMinutes(), "Task does not have an estimation")
				: ValidatedValue.valueWithNotEvaluatedStatus(task.getOriginalEstimateMinutes());

		boolean wasPersonWorkOnTaskAtPeriod = timeSpentOnTaskPersonByPeriodInMinutes != 0;
		boolean wasTeamWorkOnTaskAtPeriod = timeSpendOnTaskByTeamByPeriodInMinutes != 0;

		ValidatedValue<Integer> timeSpentOnTaskPersonInMinutesWithStatus;
		if (!wasPersonWorkOnTaskAtPeriod) {
			timeSpentOnTaskPersonInMinutesWithStatus = wasTeamWorkOnTaskAtPeriod
					? ValidatedValue.valueWithErrorStatus(timeSpentOnTaskPersonInMinutes,
					"No work was made by person on task by period. But team mates worked")
					: ValidatedValue.valueWithWarningStatus(timeSpentOnTaskPersonInMinutes, "No work was made on task by period");
		} else {
			timeSpentOnTaskPersonInMinutesWithStatus = ValidatedValue.valueWithNotEvaluatedStatus(timeSpentOnTaskPersonInMinutes);
		}

		ValidatedValue<Float> spentTimePercentageForPersonWithStatus;
		if (spentTimePercentageForPerson == null) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithNotEvaluatedStatus(spentTimePercentageForPerson);
		} else if (80 <= spentTimePercentageForPerson && spentTimePercentageForPerson <= 120) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithOkStatus(spentTimePercentageForPerson);
		} else if (50 <= spentTimePercentageForPerson && spentTimePercentageForPerson <= 80) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithWarningStatus(spentTimePercentageForPerson, "Made too fast");
		} else if (spentTimePercentageForPerson <= 50) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithErrorStatus(spentTimePercentageForPerson, "Made too fast");
		} else if (120 <= spentTimePercentageForPerson && spentTimePercentageForPerson <= 200) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithWarningStatus(spentTimePercentageForPerson, "Made too slow");
		} else if (spentTimePercentageForPerson >= 200) {
			spentTimePercentageForPersonWithStatus = ValidatedValue.valueWithErrorStatus(spentTimePercentageForPerson, "Made too slow");
		} else {
			throw new IllegalStateException("Unexpected case " + spentTimePercentageForPerson);
		}

		ValidatedValue<Float> spentTimePercentageForTeamWithStatus = ValidatedValue.valueWithNotEvaluatedStatus(spentTimePercentageForTeam);

		return TaskMetrics.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.taskExternalStatus(task.getStatus())
				.estimationInMinutes(estimationMinutes)
				.timeSpentOnTaskPersonInMinutes(timeSpentOnTaskPersonInMinutesWithStatus)
				.timeSpendOnTaskByTeamInMinutes(timeSpendOnTaskByTeamInMinutes)
				.spentTimePercentageForPerson(spentTimePercentageForPersonWithStatus)
				.spentTimePercentageForTeam(spentTimePercentageForTeamWithStatus)
				.build();
	}
}
