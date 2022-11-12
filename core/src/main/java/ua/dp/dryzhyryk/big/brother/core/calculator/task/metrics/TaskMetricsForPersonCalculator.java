package ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForFinishedTasks;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForInProgressTasks;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForNotFunctionalTasks;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;

public class TaskMetricsForPersonCalculator {

	private final Map<TaskMetaType, SpendTimeValidator> spendTimeValidatorsByTaskMetaType;

	public TaskMetricsForPersonCalculator(
			SpendTimeValidatorForInProgressTasks spendTimeValidatorForInProgressTasks,
			SpendTimeValidatorForFinishedTasks spendTimeValidatorForFinishedTasks,
			SpendTimeValidatorForNotFunctionalTasks spendTimeValidatorForNotFunctionalTasks) {

		this.spendTimeValidatorsByTaskMetaType = Map.of(
				TaskMetaType.IN_PROGRESS, spendTimeValidatorForInProgressTasks,
				TaskMetaType.FINISHED, spendTimeValidatorForFinishedTasks,
				TaskMetaType.UN_FUNCTIONAL, spendTimeValidatorForNotFunctionalTasks);
	}

	public Map<String, TaskMetrics> calculateTaskMetricsForPerson(
			Task task, LocalDate startPeriod, LocalDate endPeriod, List<String> teamMembers, TaskMetaType taskMetaType) {
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
										timeSpendOnTaskByTeamByPeriodInMinutes,
										taskMetaType)));
	}

	private TaskMetrics toTaskMetrics(
			Task task,
			Map<LocalDate, Integer> spentMinutesForDay,
			LocalDate startPeriod,
			LocalDate endPeriod,
			int timeSpendOnTaskByTeamInMinutes,
			int timeSpendOnTaskByTeamByPeriodInMinutes,
			TaskMetaType taskMetaType) {

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

		final Integer originalEstimateMinutes = task.getOriginalEstimateMinutes();

		SpendTimeValidator spendTimeValidatorForFinishedTasks = spendTimeValidatorsByTaskMetaType.get(taskMetaType);

		ValidatedValue<Integer> estimationMinutes = spendTimeValidatorForFinishedTasks.validatedEstimation(originalEstimateMinutes);
		ValidatedValue<Integer> timeSpentOnTaskPersonInMinutesWithStatus = spendTimeValidatorForFinishedTasks
						.validateTimeSpentOnTaskPersonInMinutesWithStatus(timeSpentOnTaskPersonInMinutes, timeSpendOnTaskByTeamByPeriodInMinutes, timeSpentOnTaskPersonByPeriodInMinutes);
		ValidatedValue<Float> spentTimePercentageForPersonWithStatus =
				spendTimeValidatorForFinishedTasks.validateSpentTimePercentage(spentTimePercentageForPerson);
		ValidatedValue<Float> spentTimePercentageForTeamWithStatus =
				spendTimeValidatorForFinishedTasks.validateSpentTimePercentage(spentTimePercentageForTeam);

		return TaskMetrics.builder()
				.taskId(task.getId())
				.taskName(task.getName())
				.taskExternalStatus(task.getStatus())
				.estimationInMinutes(estimationMinutes)
				.timeSpentOnTaskPersonInMinutes(timeSpentOnTaskPersonInMinutesWithStatus)
				.timeSpentOnTaskPersonByPeriodInMinutes(timeSpentOnTaskPersonByPeriodInMinutes)
				.timeSpendOnTaskByTeamInMinutes(timeSpendOnTaskByTeamInMinutes)
				.spentTimePercentageForPerson(spentTimePercentageForPersonWithStatus)
				.spentTimePercentageForTeam(spentTimePercentageForTeamWithStatus)
				.build();
	}
}
