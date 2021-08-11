package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ExcelReportGenerator implements ReportGenerator {

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

        String reportFileName =
                String.format("%s:%s-%s", peopleView.getTeamName(), peopleView.getStartPeriod(), peopleView.getEndPeriod());
        workbookBuilder.saveReportFile(reportRoot, reportFileName);
    }

    private void weeklyTable(TableBuilder tableBuilder, List<LocalDate> days, PersonMetrics personMetric) {
        List<TaskWorkingLogMetrics> dailyTaskLogs = personMetric.getDailyTaskLogs();
        if (dailyTaskLogs.isEmpty()) {
            return;
        }

        Map<LocalDate, Integer> totalTimeSpentByDay = personMetric.getTotalTimeSpentByDay().stream()
                .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));

        List<LocalDate> daysWithoutFreeWeekends = days.stream()
                .filter(day -> (day.getDayOfWeek() != DayOfWeek.SATURDAY
                        && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                        || (null != totalTimeSpentByDay.get(day)
                        && !totalTimeSpentByDay.get(day).equals(0)))
                .collect(Collectors.toList());
        List<String> dateHeaders = daysWithoutFreeWeekends.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        List<String> headerData = ListUtils.union(dateHeaders,
                Arrays.asList("Total", "Total", "-", "Task id", "Task name"));

        List<List<String>> bodyData = dailyTaskLogs.stream()
                .filter(dailyTaskLog -> dailyTaskLog.getTotalTimeSpentByPeriodInMinutes() != 0)
                .map(dailyTaskLog -> {
                    List<String> bodyDataRow = new ArrayList<>();

                    Map<LocalDate, Integer> timeSpentByDays = dailyTaskLog.getTimeSpentByDays().stream()
                            .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));
                    daysWithoutFreeWeekends.forEach(day -> bodyDataRow.add(safeGetIntAsString(timeSpentByDays, day)));

                    bodyDataRow.add(String.valueOf(convertMinutesToHour(dailyTaskLog.getTotalTimeSpentByPeriodInMinutes())));
                    bodyDataRow.add(String.valueOf(convertMinutesToHour(dailyTaskLog.getTotalTimeSpentOnTaskInMinutes())));
                    bodyDataRow.add("-");

                    bodyDataRow.add(dailyTaskLog.getTaskId());
                    bodyDataRow.add(dailyTaskLog.getTaskName());

                    return bodyDataRow;
                })
                .collect(Collectors.toList());

        List<String> footerData = daysWithoutFreeWeekends.stream()
                .map(day -> safeGetIntAsString(totalTimeSpentByDay, day))
                .collect(Collectors.toList());
        footerData.add(String.valueOf(convertMinutesToHour(personMetric.getTotalTimeSpentInCurrentPeriodInMinutes())));
        footerData.add(String.valueOf(convertMinutesToHour(personMetric.getTotalTimeSpentOnTaskInMinutes())));

        tableBuilder
                .header(headerData)
                .body(bodyData)
                .footer(footerData);
    }

    private void generatePeopleReport(PeopleView peopleView, WorkbookBuilder workbookBuilder) {
        SheetWrapper sheetWrapper = workbookBuilder.sheet("People view")
                .row()
                .cell("Team: ")
                .withStyle(Style.H1)
                .buildCell()
                .cell(peopleView.getTeamName())
                .buildCell()
                .withStyle(Style.H1)
                .withHeightInPoints(25)
                .buildRow()
                .row()
                .cell("Period:")
                .buildCell()
                .cell(peopleView.getStartPeriod().toString() + " " + peopleView.getEndPeriod().toString())
                .buildCell()
                .withStyle(Style.H2)
                .withHeightInPoints(25)
                .buildRow()
                .whiteLine();

        List<LocalDate> days = getDatesBetween(peopleView.getStartPeriod(), peopleView.getEndPeriod());

        peopleView.getPersonMetrics()
                .forEach(personMetric -> {
                    sheetWrapper.row()
                            .cell(personMetric.getPerson())
                            .buildCell()
                            .withStyle(Style.H3)
                            .withHeightInPoints(25)
                            .buildRow();

                    sheetWrapper.buildTable(builder -> weeklyTable(builder, days, personMetric));
                    sheetWrapper.whiteLine();
                    //TODO second table
                    sheetWrapper.whiteLine();
                });

        sheetWrapper.buildSheet();
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
