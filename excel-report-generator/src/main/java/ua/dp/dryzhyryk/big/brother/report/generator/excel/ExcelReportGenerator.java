package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import static ua.dp.dryzhyryk.big.brother.report.generator.utils.Formatter.convertMinutesToHour;
import static ua.dp.dryzhyryk.big.brother.report.generator.utils.Formatter.stringValueOrEmpty;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidationStatus;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TasksMetricsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogs;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableCell;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.utils.Formatter;

@Slf4j
public class ExcelReportGenerator {

	private final File reportRoot;
	private final WorkbookBuilderFactory workbookBuilderFactory;

	public ExcelReportGenerator(String reportRoot) {
		this.reportRoot = new File(reportRoot);
		this.workbookBuilderFactory = new WorkbookBuilderFactoryImpl();

		if (!this.reportRoot.isDirectory()) {
			throw new IllegalArgumentException("Report root directory does not exist. Path: " + reportRoot);
		}
	}

	public void generatePeopleReport(PeopleView peopleView) {
		WorkbookBuilder workbookBuilder = workbookBuilderFactory.prepareBuilder(ReportFileExtension.XLSX);

		generatePeopleReport(peopleView, workbookBuilder);

		String reportFileName = String.format("%s:%s-%s",
				peopleView.getTeamName(),
				peopleView.getStartPeriod(),
				peopleView.getEndPeriod());

		workbookBuilder.saveReportFile(reportRoot, reportFileName);
	}

	private void weeklyTable(TableBuilder tableBuilder, List<LocalDate> days, TasksWorkingLogsForPerson personMetric) {
		List<TaskWorkingLogs> dailyTaskLogs = personMetric.getDailyTaskWorkingLogs();
		if (dailyTaskLogs.isEmpty()) {
			return;
		}

		Map<LocalDate, ValidatedValue<TimeSpentByDay>> totalTimeSpentByDay = personMetric.getTotalTimeSpentByDays().stream()
				.collect(Collectors.toMap(
						x -> x.getValue().getDay(),
						y -> y));

		List<LocalDate> daysWithoutFreeWeekends = days.stream()
				.filter(day -> (day.getDayOfWeek() != DayOfWeek.SATURDAY
						&& day.getDayOfWeek() != DayOfWeek.SUNDAY)
						|| (null != totalTimeSpentByDay.get(day)
						&& totalTimeSpentByDay.get(day).getValue().getTimeSpentMinutes() != 0))
				.collect(Collectors.toList());

		List<String> dateHeaders = daysWithoutFreeWeekends.stream()
				.map(LocalDate::toString)
				.collect(Collectors.toList());
		List<String> headerData = ListUtils.union(dateHeaders, List.of("Spent by period by person", "Spent by person", "Task id", "Task name"));

		List<List<String>> bodyData = dailyTaskLogs.stream()
				.map(dailyTaskLog -> {
					List<String> bodyDataRow = new ArrayList<>();

					Map<LocalDate, Integer> timeSpentByDays = dailyTaskLog.getTimeSpentByDays().stream()
							.collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));
					daysWithoutFreeWeekends.forEach(day -> bodyDataRow.add(safeGetIntAsString(timeSpentByDays, day)));

					bodyDataRow.add(String.valueOf(convertMinutesToHour(dailyTaskLog.getTimeSpentOnTaskByPeriodInMinutes())));
					bodyDataRow.add(String.valueOf(convertMinutesToHour(dailyTaskLog.getTimeSpentOnTaskInMinutes())));

					bodyDataRow.add(dailyTaskLog.getTaskId());
					bodyDataRow.add(dailyTaskLog.getTaskName());

					return bodyDataRow;
				})
				.collect(Collectors.toList());

		List<TableCell> footerData = daysWithoutFreeWeekends.stream()
				.map(day -> {

					ValidatedValue<TimeSpentByDay> validatedValue = totalTimeSpentByDay.get(day);
					if (null == validatedValue) {
						return TableCell.builder()
								.build();
					}

					String value = safeGetIntAsString2(totalTimeSpentByDay, day);

					ValidationStatus status = validatedValue.getValidationStatus(); //=> Style.ERROR
					Style style = toStyle(status);

					String notes = Optional.ofNullable(validatedValue.getValidationNotes())
							.map(data -> data.stream()
									.map(note -> note.getNoteType() + ": " + note.getNote())
									.collect(Collectors.joining("\n")))
							.orElse(null);

					return TableCell.builder()
							.value(value)
							.style(style)
							.cellComment(notes)
							.build();
				})
				.collect(Collectors.toList());

		footerData.add(TableCell.builder()
				.value(String.valueOf(convertMinutesToHour(personMetric.getTotalTimeSpentOnTaskInMinutesByPeriod())))
				.build());

		tableBuilder
				.header(headerData)
				.body(bodyData)
				.footerCells(footerData);
	}

	private void finishedTasksMetricsTable(TableBuilder tableBuilder, TasksMetricsForPerson tasksMetricForPerson) {
		if (tasksMetricForPerson == null) {
			return;
		}
		List<TaskMetrics> taskMetrics = tasksMetricForPerson.getFinishedTaskMetrics();
		if (taskMetrics.isEmpty()) {
			return;
		}

		List<String> headerData = List.of(
				"Spent by person",
				"Spent by team",
				"Estimated",
				"% Spent time by person",
				"% Spent time by team",
				"-",
				"Status",
				"Task id",
				"Task name");
		List<List<TableCell>> bodyData = taskMetrics.stream()
				.map(taskMetric ->
						List.of(
								newTableCell(taskMetric.getTimeSpentOnTaskPersonInMinutes(), Formatter::convertMinutesToHour),
								newTableCell(convertMinutesToHour(taskMetric.getTimeSpendOnTaskByTeamInMinutes())),
								newTableCell(taskMetric.getEstimationInMinutes(), Formatter::convertMinutesToHour),
								newTableCell(taskMetric.getSpentTimePercentageForPerson()),
								newTableCell(taskMetric.getSpentTimePercentageForTeam()),
								newTableCell("-"),
								newTableCell(taskMetric.getTaskExternalStatus()),
								newTableCell(taskMetric.getTaskId()),
								newTableCell(taskMetric.getTaskName())))
				.collect(Collectors.toList());
		tableBuilder
				.header(headerData)
				.bodyCells(bodyData);
	}

	private void inProgressMetricsTable(TableBuilder tableBuilder, TasksMetricsForPerson tasksMetricForPerson) {
		if (tasksMetricForPerson == null) {
			return;
		}
		List<TaskMetrics> taskMetrics = tasksMetricForPerson.getInProgressTaskMetrics();
		if (taskMetrics.isEmpty()) {
			return;
		}

		List<String> headerData = List.of(
				"Spent by person",
				"Spent by team",
				"Estimated",
				"% Spent time by person",
				"% Spent time by team",
				"-",
				"Status",
				"Task id",
				"Task name");
		List<List<TableCell>> bodyData = taskMetrics.stream()
				.map(taskMetric ->
						List.of(
								newTableCell(taskMetric.getTimeSpentOnTaskPersonInMinutes(), Formatter::convertMinutesToHour),
								newTableCell(convertMinutesToHour(taskMetric.getTimeSpendOnTaskByTeamInMinutes())),
								newTableCell(taskMetric.getEstimationInMinutes(), Formatter::convertMinutesToHour),
								newTableCell(taskMetric.getSpentTimePercentageForPerson()),
								newTableCell(taskMetric.getSpentTimePercentageForTeam()),
								newTableCell("-"),
								newTableCell(taskMetric.getTaskExternalStatus()),
								newTableCell(taskMetric.getTaskId()),
								newTableCell(taskMetric.getTaskName())))
				.collect(Collectors.toList());
		tableBuilder
				.header(headerData)
				.bodyCells(bodyData);
	}

	private void unFunctionalTasksMetricsTable(TableBuilder tableBuilder, TasksMetricsForPerson tasksMetricForPerson) {
		if (tasksMetricForPerson == null) {
			return;
		}
		List<TaskMetrics> taskMetrics = tasksMetricForPerson.getUnFunctionalTaskMetrics();
		if (taskMetrics.isEmpty()) {
			return;
		}

		List<String> headerData = List.of(
				"Spent by period by person",
				"-",
				"Status",
				"Task id",
				"Task name");
		List<List<TableCell>> bodyData = taskMetrics.stream()
				.filter(taskMetric -> taskMetric.getTimeSpentOnTaskPersonByPeriodInMinutes() != 0)
				.map(taskMetric ->
						List.of(
								newTableCell(convertMinutesToHour(taskMetric.getTimeSpentOnTaskPersonByPeriodInMinutes())),
								newTableCell("-"),
								newTableCell(taskMetric.getTaskExternalStatus()),
								newTableCell(taskMetric.getTaskId()),
								newTableCell(taskMetric.getTaskName())))
				.collect(Collectors.toList());
		List<TableCell> footerCells = List.of(
				newTableCell(
						convertMinutesToHour(
								tasksMetricForPerson.getTimeSpentOnTasksPersonByPeriodForFunctionalTasksInMinutes())),
				newTableCell(""),
				newTableCell(""),
				newTableCell(""),
				newTableCell(""));

		tableBuilder
				.header(headerData)
				.bodyCells(bodyData)
				.footerCells(footerCells);
	}

	private TableCell newTableCell(Object obj) {
		return TableCell.builder()
				.value(stringValueOrEmpty(obj))
				.build();
	}

	private TableCell newTableCell(ValidatedValue<?> value) {

		Style style = toStyle(value.getValidationStatus());

		String notes = Optional.ofNullable(value.getValidationNotes())
				.map(data -> data.stream()
						.map(note -> note.getNoteType() + ": " + note.getNote())
						.collect(Collectors.joining("\n")))
				.orElse(null);

		return TableCell.builder()
				.value(stringValueOrEmpty(value.getValue()))
				.style(style)
				.cellComment(notes)
				.build();
	}

	private <T> TableCell newTableCell(ValidatedValue<T> validatedValue, Function<T, ?> valueConverter) {

		Style style = toStyle(validatedValue.getValidationStatus());

		String notes = Optional.ofNullable(validatedValue.getValidationNotes())
				.map(data -> data.stream()
						.map(note -> note.getNoteType() + ": " + note.getNote())
						.collect(Collectors.joining("\n")))
				.orElse(null);

		Object value = Optional.ofNullable(validatedValue.getValue())
				.map(valueConverter)
				.orElse(null);

		return TableCell.builder()
				.value(stringValueOrEmpty(value))
				.style(style)
				.cellComment(notes)
				.build();
	}

	private Style toStyle(ValidationStatus status) {
		switch (status) {
			case ERROR:
				return Style.ERROR;
			case WARNING:
				return Style.WARNING;
			case OK:
				return Style.OK;
			default:
				return Style.NONE;
		}
	}

	private void generatePeopleReport(PeopleView peopleView, WorkbookBuilder workbookBuilder) {
		SheetWrapper sheetWrapper = workbookBuilder.sheet("People view");

		sheetWrapper
				.row()
				.withStyle(Style.H1)
				.withHeightInPoints(25)

				.cell("Team: ")
				.withStyle(Style.H1)
				.buildCell()

				.cell(peopleView.getTeamName()).buildCell()

				.buildRow()

				.row()
				.withStyle(Style.H2)
				.withHeightInPoints(25)

				.cell("Period:")
				.buildCell()

				.cell(peopleView.getStartPeriod() + " " + peopleView.getEndPeriod())
				.buildCell()

				.buildRow()
				.whiteLine();

		List<LocalDate> days = getDatesBetween(peopleView.getStartPeriod(), peopleView.getEndPeriod());

		Map<String, TasksMetricsForPerson> tasksMetricsForPersons = peopleView.getTasksMetricsForPersons().stream()
				.collect(Collectors.toMap(TasksMetricsForPerson::getPerson, Function.identity()));

		peopleView.getTasksWorkingLogsForPersons()
				.forEach(personMetric -> {

					TasksMetricsForPerson tasksMetricForPerson = tasksMetricsForPersons.get(personMetric.getPerson());

					sheetWrapper
							.row()
							.withStyle(Style.H3)
							.withHeightInPoints(25)
							.cell(personMetric.getPerson())
							.buildCell()
							.buildRow()

							.buildTable(builder -> weeklyTable(builder, days, personMetric))
							.whiteLine()
							.row()
							//							.withStyle(Style.H4)
							.withHeightInPoints(20)
							.cell("Finished tasks:")
							.buildCell()
							.buildRow()
							.buildTable(builder -> finishedTasksMetricsTable(builder, tasksMetricForPerson))
							.whiteLine()
							.row()
							//							.withStyle(Style.H4)
							.withHeightInPoints(20)
							.cell("In progress tasks:")
							.buildCell()
							.buildRow()
							.buildTable(builder -> inProgressMetricsTable(builder, tasksMetricForPerson))
							.whiteLine()
							.row()
							//							.withStyle(Style.H4)
							.withHeightInPoints(20)
							.cell("Un functional tasks:")
							.buildCell()
							.buildRow()
							.buildTable(builder -> unFunctionalTasksMetricsTable(builder, tasksMetricForPerson))
							.whiteLine();
				});

		sheetWrapper.buildSheet();
	}

	private String safeGetIntAsString2(Map<LocalDate, ValidatedValue<TimeSpentByDay>> timeSpentByDays, LocalDate day) {
		int res = timeSpentByDays.get(day).getValue().getTimeSpentMinutes();

		return res == 0
				? ""
				: convertMinutesToHour(res).toString();
	}

	private String safeGetIntAsString(Map<LocalDate, Integer> timeSpentByDays, LocalDate day) {
		Integer res = timeSpentByDays.get(day);

		return res == null || res.equals(0)
				? ""
				: convertMinutesToHour(res).toString();
	}

	private static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {

		long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
		return IntStream.iterate(0, i -> i + 1)
				.limit(numOfDaysBetween)
				.mapToObj(startDate::plusDays)
				.collect(Collectors.toList());
	}
}
