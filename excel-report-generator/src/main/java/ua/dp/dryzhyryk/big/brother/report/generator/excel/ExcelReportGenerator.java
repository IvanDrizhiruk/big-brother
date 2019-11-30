package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.*;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.core.validator.ReportByPersonValidator;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ExcelReportGenerator {

    private final File reportRoot;
    private ReportByPersonValidator reportValidator;

    public ExcelReportGenerator(String reportRoot, ReportByPersonValidator reportByPersonValidator) {
        this.reportRoot = new File(reportRoot);
        this.reportValidator = reportByPersonValidator;

        if (!this.reportRoot.isDirectory()) {
            throw new IllegalArgumentException("Report root directory does not exist. Path: " + reportRoot);
        }
    }

    public void generateReport(TasksTreeView tasksTreeView, SprintView sprintView, TasksRootView tasksRootView) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        if (tasksTreeView != null) {
            XSSFSheet taskTreeSheet = workbook.createSheet(String.format("Task tree view"));
            generateTaskTreeReport(tasksTreeView, workbook, taskTreeSheet);
        }

        if (tasksRootView != null) {
            XSSFSheet taskSheet = workbook.createSheet(String.format("Task view"));
            generateTaskRootReport(tasksRootView, workbook, taskSheet);
        }


        if (sprintView != null) {
            XSSFSheet sprintSheet = workbook.createSheet(String.format("Sprint view"));
            generateSprintReport(sprintView, workbook, sprintSheet);
        }

        String reportFileName = String.format("[%s]-[%s].xlsx", tasksTreeView.getProject(), tasksTreeView.getSprint());
        File reportFile = new File(reportRoot, reportFileName);

        try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {

            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to generate report", e);
        }
    }

    public void generatePeopleReport(PeopleView peopleView) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet peopleSheet = workbook.createSheet(String.format("People view"));
        generatePeopleReport(peopleView, workbook, peopleSheet);

        String reportFileName =
                String.format("[%s]: %s-%s.xlsx", peopleView.getTeamName(), peopleView.getStartPeriod(), peopleView.getEndPeriod());
        File reportFile = new File(reportRoot, reportFileName);

        try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to generate report", e);
        }
    }

    private void generateTaskTreeReport(TasksTreeView tasksTreeView, XSSFWorkbook workbook, XSSFSheet sheet) {

        Map<Styles, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Styles.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowProjectName.setRowStyle(styles.get(Styles.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowSprintName.setRowStyle(styles.get(Styles.H2));
        rowSprintName.setHeightInPoints(25);

        tasksTreeView.getRootTasks()
                .forEach(rootTask -> {
                    Row rowTaskName = sheet.createRow(rowNum.getAndIncrement());
                    rowTaskName.createCell(0).setCellValue("Task:");
                    rowTaskName.createCell(1).setCellValue(rootTask.getId());
                    rowTaskName.createCell(3).setCellValue(rootTask.getName());
                    rowTaskName.setRowStyle(styles.get(Styles.H3));
                    rowTaskName.setHeightInPoints(25);

                    TaskMetrics taskMetrics = tasksTreeView.getTaskMetricsByTaskId().get(rootTask.getId());

                    TaskTimeMetrics timeMetrics = taskMetrics.getTimeMetrics();
                    Row rowMetricHeader = sheet.createRow(rowNum.getAndIncrement());
                    rowMetricHeader.createCell(0).setCellValue("Estimated");
                    rowMetricHeader.createCell(1).setCellValue("Remaining");
                    rowMetricHeader.createCell(2).setCellValue("Real");
                    rowMetricHeader.createCell(3).setCellValue("Coefficient");
                    Row rowMetric = sheet.createRow(rowNum.getAndIncrement());
                    rowMetric.createCell(0).setCellValue(convertMinutesToHour(timeMetrics.getOriginalEstimateMinutes()));
                    rowMetric.createCell(1).setCellValue(convertMinutesToHour(timeMetrics.getRemainingEstimateMinutes()));
                    rowMetric.createCell(2).setCellValue(convertMinutesToHour(timeMetrics.getTimeSpentMinutes()));
                    rowMetric.createCell(3).setCellValue(timeMetrics.getTimeCoefficient());

                    List<WorkLogByDay> dailyWorkLogMetrics = taskMetrics.getWorkLogByDay();
                    if (!dailyWorkLogMetrics.isEmpty()) {
                        newRowSeparator(sheet, rowNum);

                        Row rowDailyWorkLogMetricsHeader = sheet.createRow(rowNum.getAndIncrement());
                        rowDailyWorkLogMetricsHeader.createCell(0).setCellValue("Day");
                        rowDailyWorkLogMetricsHeader.createCell(1).setCellValue("Person");
                        rowDailyWorkLogMetricsHeader.createCell(2).setCellValue("Spent hours");
                        dailyWorkLogMetrics.forEach(dailyWorkLogMetric -> {
                            dailyWorkLogMetric.getPersonWorkLogs().forEach(personWorkLog -> {
                                Row rowDailyWorkLogMetrics = sheet.createRow(rowNum.getAndIncrement());
                                rowDailyWorkLogMetrics.createCell(0).setCellValue(dailyWorkLogMetric.getWorkDate().toString());
                                rowDailyWorkLogMetrics.createCell(1).setCellValue(personWorkLog.getPerson());
                                rowDailyWorkLogMetrics.createCell(2).setCellValue(convertMinutesToHour(personWorkLog.getMinutesSpent()));
                            });
                        });
                    }

                    List<WorkLogByPerson> workLogByDayMetrics = taskMetrics.getWorkLogByPerson();
                    if (!workLogByDayMetrics.isEmpty()) {
                        newRowSeparator(sheet, rowNum);

                        Row rowWorkLogByDayMetricsHeader = sheet.createRow(rowNum.getAndIncrement());
                        rowWorkLogByDayMetricsHeader.createCell(0).setCellValue("Person");
                        rowWorkLogByDayMetricsHeader.createCell(1).setCellValue("Spent hours");

                        workLogByDayMetrics.forEach(personWorkLog -> {
                            Row rowWorkLogByDayMetrics = sheet.createRow(rowNum.getAndIncrement());
                            rowWorkLogByDayMetrics.createCell(0).setCellValue(personWorkLog.getPerson());
                            rowWorkLogByDayMetrics.createCell(1).setCellValue(convertMinutesToHour(personWorkLog.getMinutesSpent()));
                        });
                    }

                    rootTask.getSubTasks()
                            .forEach(subTask -> {
                                newRowSeparator(sheet, rowNum);

                                Row rowSubTaskName = sheet.createRow(rowNum.getAndIncrement());
                                rowSubTaskName.createCell(2).setCellValue("Sub task:");
                                rowSubTaskName.createCell(3).setCellValue(subTask.getId());
                                rowSubTaskName.createCell(4).setCellValue(subTask.getName());
                                rowSubTaskName.setRowStyle(styles.get(Styles.H3));
                                rowSubTaskName.setHeightInPoints(25);

                                newRowSeparator(sheet, rowNum);

                                TaskMetrics subTaskMetrics = tasksTreeView.getTaskMetricsByTaskId().get(subTask.getId());

                                TaskTimeMetrics subTimeMetrics = subTaskMetrics.getTimeMetrics();
                                Row rowSubMetricHeader = sheet.createRow(rowNum.getAndIncrement());
                                rowSubMetricHeader.createCell(2).setCellValue("Estimated");
                                rowSubMetricHeader.createCell(3).setCellValue("Remaining");
                                rowSubMetricHeader.createCell(4).setCellValue("Real");
                                rowSubMetricHeader.createCell(5).setCellValue("Coefficient");
                                Row rowSubMetric = sheet.createRow(rowNum.getAndIncrement());
                                rowSubMetric.createCell(2).setCellValue(convertMinutesToHour(subTimeMetrics.getOriginalEstimateMinutes()));
                                rowSubMetric.createCell(3).setCellValue(convertMinutesToHour(subTimeMetrics.getRemainingEstimateMinutes()));
                                rowSubMetric.createCell(4).setCellValue(convertMinutesToHour(subTimeMetrics.getTimeSpentMinutes()));
                                rowSubMetric.createCell(5).setCellValue(subTimeMetrics.getTimeCoefficient());

                                newRowSeparator(sheet, rowNum);

                                List<WorkLogByDay> subDailyWorkLogMetrics = subTaskMetrics.getWorkLogByDay();
                                if (!subDailyWorkLogMetrics.isEmpty()) {
                                    newRowSeparator(sheet, rowNum);
                                    Row rowSubDailyWorkLogMetricsHeader = sheet.createRow(rowNum.getAndIncrement());
                                    rowSubDailyWorkLogMetricsHeader.createCell(2).setCellValue("Day");
                                    rowSubDailyWorkLogMetricsHeader.createCell(3).setCellValue("Person");
                                    rowSubDailyWorkLogMetricsHeader.createCell(4).setCellValue("Spent hours");
                                    subDailyWorkLogMetrics.forEach(subDailyWorkLogMetric -> {
                                        subDailyWorkLogMetric.getPersonWorkLogs().forEach(subPersonWorkLog -> {
                                            Row rowSubDailyWorkLogMetrics = sheet.createRow(rowNum.getAndIncrement());
                                            rowSubDailyWorkLogMetrics.createCell(2).setCellValue(subDailyWorkLogMetric.getWorkDate().toString());
                                            rowSubDailyWorkLogMetrics.createCell(3).setCellValue(subPersonWorkLog.getPerson());
                                            rowSubDailyWorkLogMetrics.createCell(4)
                                                    .setCellValue(convertMinutesToHour(subPersonWorkLog.getMinutesSpent()));
                                        });
                                    });
                                }

                                List<WorkLogByPerson> workSubLogByDayMetrics = subTaskMetrics.getWorkLogByPerson();
                                if (!workSubLogByDayMetrics.isEmpty()) {
                                    newRowSeparator(sheet, rowNum);

                                    Row rowSubWorkLogByDayMetricsHeader = sheet.createRow(rowNum.getAndIncrement());
                                    rowSubWorkLogByDayMetricsHeader.createCell(2).setCellValue("Person");
                                    rowSubWorkLogByDayMetricsHeader.createCell(3).setCellValue("Spent hours");

                                    workSubLogByDayMetrics.forEach(personWorkLog -> {
                                        Row rowSubWorkLogByDayMetrics = sheet.createRow(rowNum.getAndIncrement());
                                        rowSubWorkLogByDayMetrics.createCell(2).setCellValue(personWorkLog.getPerson());
                                        rowSubWorkLogByDayMetrics.createCell(3)
                                                .setCellValue(convertMinutesToHour(personWorkLog.getMinutesSpent()));
                                    });
                                }

                            });

                    newRowSeparator(sheet, rowNum);

                });
    }

    private void generateTaskRootReport(TasksRootView tasksTreeView, XSSFWorkbook workbook, XSSFSheet sheet) {

        Map<Styles, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Styles.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowProjectName.setRowStyle(styles.get(Styles.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowSprintName.setRowStyle(styles.get(Styles.H2));
        rowSprintName.setHeightInPoints(25);

        Row rowMetricHeader = sheet.createRow(rowNum.getAndIncrement());
        rowMetricHeader.createCell(0).setCellValue("Estimated");
        rowMetricHeader.createCell(1).setCellValue("Real");
        rowMetricHeader.createCell(2).setCellValue("Coefficient");
        rowMetricHeader.createCell(3).setCellValue("-");
        rowMetricHeader.createCell(4).setCellValue("Status");
        rowMetricHeader.createCell(5).setCellValue("Type");
        rowMetricHeader.createCell(6).setCellValue("-");
        rowMetricHeader.createCell(7).setCellValue("Task id");
        rowMetricHeader.createCell(8).setCellValue("Task name");

        tasksTreeView.getRootTasks()
                .forEach(rootTask -> {
                    TaskMetrics taskMetrics = tasksTreeView.getTaskMetricsByTaskId().get(rootTask.getId());
                    TaskTimeMetrics timeMetrics = taskMetrics.getTimeMetrics();
                    Row rowMetric = sheet.createRow(rowNum.getAndIncrement());
                    rowMetric.createCell(0).setCellValue(convertMinutesToHour(timeMetrics.getOriginalEstimateMinutes()));
                    rowMetric.createCell(1).setCellValue(convertMinutesToHour(timeMetrics.getTimeSpentMinutes()));
                    rowMetric.createCell(2).setCellValue(timeMetrics.getTimeCoefficient());
                    rowMetric.createCell(3).setCellValue("-");
                    rowMetric.createCell(4).setCellValue(rootTask.getStatus());
                    rowMetric.createCell(5).setCellValue(rootTask.getType());
                    rowMetric.createCell(6).setCellValue("-");
                    rowMetric.createCell(7).setCellValue(rootTask.getId());
                    rowMetric.createCell(8).setCellValue(rootTask.getName());
                });
    }

    private void generatePeopleReport(PeopleView peopleView, XSSFWorkbook workbook, XSSFSheet sheet) {
        Map<Styles, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Styles.H1));
        cell.setCellValue("Team: ");
        rowProjectName.createCell(1).setCellValue(peopleView.getTeamName());
        rowProjectName.setRowStyle(styles.get(Styles.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Period:");
        rowSprintName.createCell(1).setCellValue(peopleView.getStartPeriod().toString() + " " + peopleView.getEndPeriod().toString());
        rowSprintName.setRowStyle(styles.get(Styles.H2));
        rowSprintName.setHeightInPoints(25);

        List<LocalDate> days = getDatesBetween(peopleView.getStartPeriod(), peopleView.getEndPeriod());

        peopleView.getPersonMetrics()
                .forEach(personMetric -> {
                    Row rowTaskName = sheet.createRow(rowNum.getAndIncrement());
                    rowTaskName.createCell(0).setCellValue(personMetric.getPerson());
                    rowTaskName.setRowStyle(styles.get(Styles.H3));
                    rowTaskName.setHeightInPoints(25);

                    List<TaskWorkingLogMetrics> dailyTaskLogs = personMetric.getDailyTaskLogs();
                    if (!dailyTaskLogs.isEmpty()) {

                        Map<LocalDate, Integer> totalTimeSpentByDay = personMetric.getTotalTimeSpentByDay().stream()
                                .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));

                        List<LocalDate> daysWithoutFreeWeekends = days.stream()
                                .filter(day -> (day.getDayOfWeek() != DayOfWeek.SATURDAY
                                        && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                                        || (null != totalTimeSpentByDay.get(day)
                                        && !totalTimeSpentByDay.get(day).equals(0)))
                                .collect(Collectors.toList());

                        AtomicInteger cellHeaderCount = new AtomicInteger();
                        Row rowDailyTaskLogsHeader = sheet.createRow(rowNum.getAndIncrement());
                        daysWithoutFreeWeekends.forEach(day -> {
                            //TODO valisate weekends
                            rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue(day.toString());
                        });
                        rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue("Total");
                        rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue("Total");
                        rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue("-");
                        rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue("Task id");
                        rowDailyTaskLogsHeader.createCell(cellHeaderCount.getAndIncrement()).setCellValue("Task name");

                        dailyTaskLogs
                                .forEach(dailyTaskLog -> {

                                    if (dailyTaskLog.getTotalTimeSpentByPeriodInMinutes() == 0) {
                                        return;
                                    }

                                    Row rowDailyTaskLogMetrics = sheet.createRow(rowNum.getAndIncrement());

                                    Map<LocalDate, Integer> timeSpentByDays = dailyTaskLog.getTimeSpentByDays().stream()
                                            .collect(Collectors.toMap(TimeSpentByDay::getDay, TimeSpentByDay::getTimeSpentMinutes));

                                    AtomicInteger cellCount = new AtomicInteger();
                                    daysWithoutFreeWeekends.forEach(day -> {
                                        rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement())
                                                .setCellValue(safeGetIntAsString(timeSpentByDays, day));
                                    });

                                    rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement()).setCellValue(
                                            convertMinutesToHour(dailyTaskLog.getTotalTimeSpentByPeriodInMinutes()));
                                    rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement()).setCellValue(
                                            convertMinutesToHour(dailyTaskLog.getTotalTimeSpentOnTaskInMinutes()));
                                    rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement()).setCellValue("-");

                                    rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement()).setCellValue(dailyTaskLog.getTaskId());
                                    rowDailyTaskLogMetrics.createCell(cellCount.getAndIncrement()).setCellValue(dailyTaskLog.getTaskName());
                                });

                        AtomicInteger cellTotalCount = new AtomicInteger();
                        Row rowTotalDailyTaskLogs = sheet.createRow(rowNum.getAndIncrement());

                        daysWithoutFreeWeekends.forEach(day -> {
                            Cell cellTotalForDay = rowTotalDailyTaskLogs.createCell(cellTotalCount.getAndIncrement());

                            processValidationInfo(
                                    workbook,
                                    sheet,
                                    cellTotalForDay,
                                    reportValidator.validateTotalTimeSpentByDay(totalTimeSpentByDay.get(day)),
                                    styles);

                            cellTotalForDay.setCellValue(safeGetIntAsString(totalTimeSpentByDay, day));
                        });
                        Cell cellTotalTimeSpentInDay = rowTotalDailyTaskLogs.createCell(cellTotalCount.getAndIncrement());
                        cellTotalTimeSpentInDay.setCellValue(convertMinutesToHour(personMetric.getTotalTimeSpentInCurrentPeriodInMinutes()));

                        processValidationInfo(
                                workbook,
                                sheet,
                                cellTotalTimeSpentInDay,
                                reportValidator.validateTotalTimeSpentByPeriod(
                                        personMetric.getTotalTimeSpentInCurrentPeriodInMinutes(),
                                        daysWithoutFreeWeekends.size()),
                                styles);

                        rowTotalDailyTaskLogs.createCell(cellTotalCount.getAndIncrement()).setCellValue(
                                convertMinutesToHour(personMetric.getTotalTimeSpentOnTaskInMinutes()));


                        newRowSeparator(sheet, rowNum);

                        Row rowTaskLogsHeader = sheet.createRow(rowNum.getAndIncrement());
                        rowTaskLogsHeader.createCell(0).setCellValue("Real");
                        rowTaskLogsHeader.createCell(1).setCellValue("Estimated");
                        rowTaskLogsHeader.createCell(2).setCellValue("TC");
                        rowTaskLogsHeader.createCell(3).setCellValue("-");
                        rowTaskLogsHeader.createCell(4).setCellValue("Status");
                        rowTaskLogsHeader.createCell(5).setCellValue("Task id");
                        rowTaskLogsHeader.createCell(6).setCellValue("Task name");
                        dailyTaskLogs
                                .forEach(dailyTaskLog -> {

                                    Row rowDailyTaskLogMetrics = sheet.createRow(rowNum.getAndIncrement());
                                    rowDailyTaskLogMetrics.createCell(0).setCellValue(convertMinutesToHour(dailyTaskLog.getTimeSpentMinutes()));
                                    rowDailyTaskLogMetrics.createCell(1)
                                            .setCellValue(convertMinutesToHour(dailyTaskLog.getOriginalEstimateMinutes()));
                                    rowDailyTaskLogMetrics.createCell(2).setCellValue(dailyTaskLog.getTimeCoefficient());
                                    rowDailyTaskLogMetrics.createCell(3).setCellValue("-");
                                    rowDailyTaskLogMetrics.createCell(4).setCellValue(dailyTaskLog.getTaskExternalStatus());
                                    rowDailyTaskLogMetrics.createCell(5).setCellValue(dailyTaskLog.getTaskId());
                                    rowDailyTaskLogMetrics.createCell(6).setCellValue(dailyTaskLog.getTaskName());
                                });
                    }

                    newRowSeparator(sheet, rowNum);

                });
    }

    private void processValidationInfo(XSSFWorkbook workbook, XSSFSheet sheet, Cell cell, ValidationInformation validationInformation, Map<Styles, CellStyle> styles) {
        switch (validationInformation.getValidationStatus()) {
            case OK:
                cell.setCellStyle(styles.get(Styles.OK));
                break;
            case WARNING:
                cell.setCellStyle(styles.get(Styles.WARNING));
                break;
            case ERROR_NOT_ENOUGH:
                cell.setCellStyle(styles.get(Styles.ERROR_NOT_ENOUGH));
                break;
            case ERROR_TOO_MUCH:
                cell.setCellStyle(styles.get(Styles.ERROR_TOO_MUCH));
                break;
        }

        if (null != validationInformation.getMessage()) {
            addComment(workbook, sheet, cell, "Big Brother", validationInformation.getMessage());
        }
    }

    public void addComment(Workbook workbook, Sheet sheet, Cell cell, String author, String commentText) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:
        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        anchor.setCol2(cell.getColumnIndex() + 3); //...and ends at that given column
        anchor.setRow1(cell.getRowIndex() + 1); //one row below the cell...
        anchor.setRow2(cell.getRowIndex() + 5); //...and 4 rows high

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor(author);

        cell.setCellComment(comment);
    }

    private String safeGetIntAsString(Map<LocalDate, Integer> timeSpentByDays, LocalDate day) {
        Integer res = timeSpentByDays.get(day);

        return res == null || res.equals(0)
                ? ""
                : convertMinutesToHour(res).toString();
    }

    public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {

        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(startDate::plusDays)
                .collect(Collectors.toList());
    }

    private void generateSprintReport(SprintView sprintView, XSSFWorkbook workbook, XSSFSheet sheet) {
        Map<Styles, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Styles.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(sprintView.getProject());
        rowProjectName.setRowStyle(styles.get(Styles.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(sprintView.getProject());
        rowSprintName.setRowStyle(styles.get(Styles.H2));
        rowSprintName.setHeightInPoints(25);

        TaskTimeMetrics totalTasksTimeMetrics = sprintView.getTotalTasksTimeMetrics();

        Row rowTotalTasksTimeMetricsHeader = sheet.createRow(rowNum.getAndIncrement());
        rowTotalTasksTimeMetricsHeader.createCell(0).setCellValue("Spent h");
        rowTotalTasksTimeMetricsHeader.createCell(1).setCellValue("Original h");
        rowTotalTasksTimeMetricsHeader.createCell(2).setCellValue("TC");

        Row rowSprintTaskLog = sheet.createRow(rowNum.getAndIncrement());
        rowSprintTaskLog.createCell(0).setCellValue(convertMinutesToHour(totalTasksTimeMetrics.getTimeSpentMinutes()));
        rowSprintTaskLog.createCell(1).setCellValue(convertMinutesToHour(totalTasksTimeMetrics.getOriginalEstimateMinutes()));
        rowSprintTaskLog.createCell(2).setCellValue(totalTasksTimeMetrics.getTimeCoefficient());
    }

    private void newRowSeparator(XSSFSheet sheet, AtomicInteger rowNum) {
        sheet.createRow(rowNum.getAndIncrement());
    }

    private static Float convertMinutesToHour(Integer minutes) {
        return TimeUtils.convertMinutesToHour(minutes);
    }

    enum Styles {
        H1,
        H2,
        H3,
        ERROR_NOT_ENOUGH,
        ERROR_TOO_MUCH,
        WARNING,
        OK
    }

    private static Map<Styles, CellStyle> createStyles(Workbook wb) {
        Map<Styles, CellStyle> styles = new HashMap<>();

        Font titleFontH1 = wb.createFont();
        titleFontH1.setFontHeightInPoints((short) 20);
        titleFontH1.setFontName("Trebuchet MS");

        CellStyle styleH1 = wb.createCellStyle();
        styleH1.setFont(titleFontH1);
        styleH1.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleH1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.H1, styleH1);

        Font titleFontH2 = wb.createFont();
        titleFontH2.setFontHeightInPoints((short) 18);
        titleFontH2.setFontName("Trebuchet MS");
        CellStyle styleH2 = wb.createCellStyle();
        styleH2.setFont(titleFontH2);
        styleH2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleH2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.H2, styleH2);

        Font titleFontH3 = wb.createFont();
        titleFontH3.setFontHeightInPoints((short) 16);
        titleFontH3.setFontName("Trebuchet MS");
        CellStyle styleH3 = wb.createCellStyle();
        styleH3.setFont(titleFontH3);
        styleH3.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        styleH3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.H3, styleH3);

        CellStyle styleError = wb.createCellStyle();
        styleError.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        styleError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.ERROR_NOT_ENOUGH, styleError);

        CellStyle styleErrorNotEnough = wb.createCellStyle();
        styleErrorNotEnough.setFillForegroundColor(IndexedColors.RED1.getIndex());
        styleErrorNotEnough.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.ERROR_TOO_MUCH, styleErrorNotEnough);

        CellStyle styleWarning = wb.createCellStyle();
        styleWarning.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        styleWarning.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.WARNING, styleWarning);

        CellStyle styleOk = wb.createCellStyle();
        styleOk.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Styles.OK, styleOk);

        //
        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		styles.put("item_right", style);
        //
        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		style.setBorderRight(BorderStyle.DOTTED);
        //		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderLeft(BorderStyle.DOTTED);
        //		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderTop(BorderStyle.DOTTED);
        //		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setDataFormat(wb.createDataFormat().getFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"));
        //		styles.put("input_$", style);

        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		style.setBorderRight(BorderStyle.DOTTED);
        //		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderLeft(BorderStyle.DOTTED);
        //		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderTop(BorderStyle.DOTTED);
        //		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setDataFormat(wb.createDataFormat().getFormat("0.000%"));
        //		styles.put("input_%", style);

        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		style.setBorderRight(BorderStyle.DOTTED);
        //		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderLeft(BorderStyle.DOTTED);
        //		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderTop(BorderStyle.DOTTED);
        //		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setDataFormat(wb.createDataFormat().getFormat("0"));
        //		styles.put("input_i", style);
        //
        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.CENTER);
        //		style.setFont(itemFont);
        //		style.setDataFormat(wb.createDataFormat().getFormat("m/d/yy"));
        //		styles.put("input_d", style);
        //
        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		style.setBorderRight(BorderStyle.DOTTED);
        //		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderLeft(BorderStyle.DOTTED);
        //		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderTop(BorderStyle.DOTTED);
        //		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        //		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //		styles.put("formula_$", style);
        //
        //		style = wb.createCellStyle();
        //		style.setAlignment(HorizontalAlignment.RIGHT);
        //		style.setFont(itemFont);
        //		style.setBorderRight(BorderStyle.DOTTED);
        //		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderLeft(BorderStyle.DOTTED);
        //		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setBorderTop(BorderStyle.DOTTED);
        //		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setDataFormat(wb.createDataFormat().getFormat("0"));
        //		style.setBorderBottom(BorderStyle.DOTTED);
        //		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        //		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        //		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //		styles.put("formula_i", style);

        return styles;
    }
}
