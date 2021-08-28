package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidationStatus;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TaskWorkingLogs;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableCell;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<String> headerData = ListUtils.union(dateHeaders, List.of("By period", "Total", "Task id", "Task name"));

        List<List<String>> bodyData = dailyTaskLogs.stream()
                .map(dailyTaskLog -> {
                    List<String> bodyDataRow = new ArrayList<>();

                    Map<LocalDate, Integer> timeSpentByDays = dailyTaskLog.getTimeSpentByDays().stream()
                            .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));
                    daysWithoutFreeWeekends.forEach(day -> bodyDataRow.add(safeGetIntAsString(timeSpentByDays, day)));

                    bodyDataRow.add(String.valueOf(convertMinutesToHour(dailyTaskLog.getTimeSpentOnTaskInMinutesByPeriod())));
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

    private Style toStyle(ValidationStatus status) {
        switch (status) {
            case ERROR:
                return Style.ERROR;
            case WARNING:
                return Style.WARNING;
            default:
                return Style.OK;
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

        peopleView.getTasksWorkingLogsForPersons()
                .forEach(personMetric -> {
                    sheetWrapper
                            .row()
                            .withStyle(Style.H3)
                            .withHeightInPoints(25)
                            .cell(personMetric.getPerson())
                            .buildCell()
                            .buildRow()

                            .buildTable(builder -> weeklyTable(builder, days, personMetric))
                            .whiteLine()
                            //TODO second table
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

    private static Float convertMinutesToHour(Integer minutes) {
        return TimeUtils.convertMinutesToHour(minutes);
    }
}
