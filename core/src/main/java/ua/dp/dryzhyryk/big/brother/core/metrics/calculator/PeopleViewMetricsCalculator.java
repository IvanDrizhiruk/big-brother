package ua.dp.dryzhyryk.big.brother.core.metrics.calculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.DayWorkLogForPerson;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskLog;

public class PeopleViewMetricsCalculator {

	public List<PersonMetrics> calculateFor(List<Task> tasksTree) {

		Map<String, PersonMetrics> personMetricsByUser = tasksTree.stream()
				.flatMap(rootTask -> {
					Stream<PersonMetrics> peopleMetricsForRootTask = toPersonMetrics(rootTask, null);

					Stream<PersonMetrics> peopleMetricsForSubTasks = rootTask.getSubTasks().stream()
							.flatMap(subTask -> toPersonMetrics(subTask, rootTask));

					return Stream.concat(peopleMetricsForRootTask, peopleMetricsForSubTasks);
				})
				.collect(
						Collectors.toMap(
								PersonMetrics::getPerson,
								Function.identity(),
								this::mergePersonMetricsForOnePerson)
				);

		return new ArrayList<>(personMetricsByUser.values());
	}

	private Stream<PersonMetrics> toPersonMetrics(Task task, Task rootTask) {
		Map<String, Map<LocalDate, Integer>> spendTimeByDayForPerson = task.getWorkLogs().stream()
				.collect(
						Collectors.groupingBy(
								TaskWorkLog::getPerson,
								Collectors.groupingBy(
										taskWorkLog -> taskWorkLog.getStartDateTime().toLocalDate(),
										Collectors.summingInt(TaskWorkLog::getMinutesSpent))));

		//TODO

		return spendTimeByDayForPerson.entrySet().stream()
				.map(entry -> {
					List<TaskLog> sprintTaskLogs = toSprintTaskLogs(entry.getValue(), rootTask, task);

					TaskLog totalSprintTaskLog = calculateTotalSprintTaskLogs(sprintTaskLogs);

					return PersonMetrics.builder()
							.person(entry.getKey())
							.dayWorkLogForPeople(toDayWorkLogForPeople(entry.getValue(), rootTask, task))
							.sprintTaskLogs(sprintTaskLogs)
							.totalSprintTaskLog(totalSprintTaskLog)
							.build();
				})
				.collect(Collectors.toMap(
						PersonMetrics::getPerson,
						Function.identity(),
						this::mergePersonMetricsForOnePerson
				)).values().stream();
	}

	private List<DayWorkLogForPerson> toDayWorkLogForPeople(Map<LocalDate, Integer> spentMinutesForDay, Task rootTask, Task task) {
		String parentTaskId = Optional.ofNullable(rootTask)
				.map(Task::getId)
				.orElse(task.getId());

		return spentMinutesForDay.entrySet().stream()
				.map(entry -> {
					List<TaskLog> dailyTaskLogs = new ArrayList<>();
					dailyTaskLogs.add(TaskLog.builder()
							.timeSpentMinutes(entry.getValue())
							.parentTaskId(parentTaskId)
							.taskId(task.getId())
							.taskName(task.getName())
							.build());

					return DayWorkLogForPerson.builder()
							.dayOfWork(entry.getKey())
							.dailyTaskLogs(dailyTaskLogs)
							.build();
				})
				.collect(Collectors.toList());
	}

	private PersonMetrics mergePersonMetricsForOnePerson(PersonMetrics x, PersonMetrics y) {
		Collection<DayWorkLogForPerson> dayWorkLogForPeople = Stream.of(x.getDayWorkLogForPeople(), y.getDayWorkLogForPeople())
				.flatMap(List::stream)
				.collect(Collectors.toMap(
						DayWorkLogForPerson::getDayOfWork,
						Function.identity(),
						(a, b) -> {

							List<TaskLog> logs = new ArrayList<>();
							logs.addAll(a.getDailyTaskLogs());
							logs.addAll(b.getDailyTaskLogs());

							return a.toBuilder()
									.dailyTaskLogs(logs)
									.build();
						}))
				.values();

		Collection<TaskLog> sprintWorkLogForPeople = Stream.of(x.getSprintTaskLogs(), y.getSprintTaskLogs())
				.flatMap(List::stream)
				.collect(Collectors.toMap(
						taskLog -> String.format("[%s] - [%s]", taskLog.getParentTaskId(), taskLog.getTaskId()),
						Function.identity(),
						(a, b) -> {
							return a.toBuilder()
									.timeSpentMinutes(a.getTimeSpentMinutes() + b.getTimeSpentMinutes())
									.build();
						}))
				.values();

		TaskLog totalSprintTaskLog = calculateTaskLog(
				x.getTotalSprintTaskLog().getTimeSpentMinutes() + y.getTotalSprintTaskLog().getTimeSpentMinutes(),
				x.getTotalSprintTaskLog().getOriginalEstimateMinutes() + y.getTotalSprintTaskLog().getOriginalEstimateMinutes());

		return x.toBuilder()
				.dayWorkLogForPeople(new ArrayList<>(dayWorkLogForPeople))
				.sprintTaskLogs(new ArrayList<>(sprintWorkLogForPeople))
				.totalSprintTaskLog(totalSprintTaskLog)
				.build();
	}

	private List<TaskLog> toSprintTaskLogs(Map<LocalDate, Integer> spentMinutesForDay, Task rootTask, Task task) {

		Integer minutesSpent = spentMinutesForDay.values().stream()
				.collect(Collectors.summingInt(i -> i));

		String parentTaskId = Optional.ofNullable(rootTask)
				.map(Task::getId)
				.orElse(task.getId());

		int originalEstimateMinutes = Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0);
		int timeSpentMinutes = Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0);

		float timeCoefficient =
				0 == timeSpentMinutes
						? 0
						: ((float) originalEstimateMinutes) / timeSpentMinutes;

		TaskLog taskLog = TaskLog.builder()
				.timeSpentMinutes(minutesSpent)
				.originalEstimateMinutes(originalEstimateMinutes)
				.timeCoefficient(timeCoefficient)
				.parentTaskId(parentTaskId)
				.taskId(task.getId())
				.taskName(task.getName())
				.build();

		return Collections.singletonList(taskLog);
	}

	private TaskLog calculateTotalSprintTaskLogs(List<TaskLog> sprintTaskLogs) {
		int timeSpentMinutes = sprintTaskLogs.stream().mapToInt(TaskLog::getTimeSpentMinutes).sum();
		int originalEstimateMinutes = sprintTaskLogs.stream().mapToInt(TaskLog::getOriginalEstimateMinutes).sum();

		return calculateTaskLog(timeSpentMinutes, originalEstimateMinutes);
	}

	private TaskLog calculateTaskLog(int timeSpentMinutes, int originalEstimateMinutes) {
		float timeCoefficient =
				0 == timeSpentMinutes
						? 0
						: ((float) originalEstimateMinutes) / timeSpentMinutes;

		return TaskLog.builder()
				.timeSpentMinutes(timeSpentMinutes)
				.originalEstimateMinutes(originalEstimateMinutes)
				.timeCoefficient(timeCoefficient)
				.build();
	}
}
