package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksRootView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.WorkLogByDay;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.WorkLogByPerson;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.core.validator.ReportByPersonValidator;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationInformation;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;

@Slf4j
public class ExcelReportGenerator implements ReportGenerator {

    private final File reportRoot;
    private final ReportByPersonValidator reportValidator;
    private final WorkbookBuilderFactory workbookBuilderFactory;

    public ExcelReportGenerator(String reportRoot, ReportByPersonValidator reportByPersonValidator) {
        this.reportRoot = new File(reportRoot);
        this.reportValidator = reportByPersonValidator;
        this.workbookBuilderFactory = new WorkbookBuilderFactoryImpl();

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
        WorkbookBuilder workbookBuilder = workbookBuilderFactory.prepareBuilder(ReportFileExtension.XLSX);
        generatePeopleReport(peopleView, workbookBuilder);

        String reportFileName =
                String.format("%s:%s-%s", peopleView.getTeamName(), peopleView.getStartPeriod(), peopleView.getEndPeriod());
        workbookBuilder.saveReportFile(reportRoot, reportFileName);
    }

    private void generateTaskTreeReport(TasksTreeView tasksTreeView, XSSFWorkbook workbook, XSSFSheet sheet) {

        Map<Style, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Style.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowProjectName.setRowStyle(styles.get(Style.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowSprintName.setRowStyle(styles.get(Style.H2));
        rowSprintName.setHeightInPoints(25);

        tasksTreeView.getRootTasks()
                .forEach(rootTask -> {
                    Row rowTaskName = sheet.createRow(rowNum.getAndIncrement());
                    rowTaskName.createCell(0).setCellValue("Task:");
                    rowTaskName.createCell(1).setCellValue(rootTask.getId());
                    rowTaskName.createCell(3).setCellValue(rootTask.getName());
                    rowTaskName.setRowStyle(styles.get(Style.H3));
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
                                rowSubTaskName.setRowStyle(styles.get(Style.H3));
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

        Map<Style, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Style.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowProjectName.setRowStyle(styles.get(Style.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(tasksTreeView.getProject());
        rowSprintName.setRowStyle(styles.get(Style.H2));
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


    private void processValidationInfo(XSSFWorkbook workbook, XSSFSheet sheet, Cell cell, ValidationInformation validationInformation, Map<Style, CellStyle> styles) {
        switch (validationInformation.getValidationStatus()) {
            case OK:
                cell.setCellStyle(styles.get(Style.OK));
                break;
            case WARNING:
                cell.setCellStyle(styles.get(Style.WARNING));
                break;
            case ERROR_NOT_ENOUGH:
                cell.setCellStyle(styles.get(Style.ERROR_NOT_ENOUGH));
                break;
            case ERROR_TOO_MUCH:
                cell.setCellStyle(styles.get(Style.ERROR_TOO_MUCH));
                break;
        }

        if (null != validationInformation.getMessage()) {
            addComment(workbook, sheet, cell, "Big Brother", validationInformation.getMessage());
        }
    }

    private void addComment(Workbook workbook, Sheet sheet, Cell cell, String author, String commentText) {
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

    private static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {

        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(startDate::plusDays)
                .collect(Collectors.toList());
    }

    private void generateSprintReport(SprintView sprintView, XSSFWorkbook workbook, XSSFSheet sheet) {
        Map<Style, CellStyle> styles = createStyles(workbook);

        AtomicInteger rowNum = new AtomicInteger(0);

        Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
        Cell cell = rowProjectName.createCell(0);
        cell.setCellStyle(styles.get(Style.H1));
        cell.setCellValue("Project: ");
        rowProjectName.createCell(1).setCellValue(sprintView.getProject());
        rowProjectName.setRowStyle(styles.get(Style.H1));
        rowProjectName.setHeightInPoints(25);

        Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
        rowSprintName.createCell(0).setCellValue("Sprint:");
        rowSprintName.createCell(1).setCellValue(sprintView.getProject());
        rowSprintName.setRowStyle(styles.get(Style.H2));
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

    private static Map<Style, CellStyle> createStyles(Workbook wb) {
        Map<Style, CellStyle> styles = new HashMap<>();

        Font titleFontH1 = wb.createFont();
        titleFontH1.setFontHeightInPoints((short) 20);
        titleFontH1.setFontName("Trebuchet MS");

        CellStyle styleH1 = wb.createCellStyle();
        styleH1.setFont(titleFontH1);
        styleH1.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleH1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.H1, styleH1);

        Font titleFontH2 = wb.createFont();
        titleFontH2.setFontHeightInPoints((short) 18);
        titleFontH2.setFontName("Trebuchet MS");
        CellStyle styleH2 = wb.createCellStyle();
        styleH2.setFont(titleFontH2);
        styleH2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleH2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.H2, styleH2);

        Font titleFontH3 = wb.createFont();
        titleFontH3.setFontHeightInPoints((short) 16);
        titleFontH3.setFontName("Trebuchet MS");
        CellStyle styleH3 = wb.createCellStyle();
        styleH3.setFont(titleFontH3);
        styleH3.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        styleH3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.H3, styleH3);

        CellStyle styleError = wb.createCellStyle();
        styleError.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        styleError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.ERROR_NOT_ENOUGH, styleError);

        CellStyle styleErrorNotEnough = wb.createCellStyle();
        styleErrorNotEnough.setFillForegroundColor(IndexedColors.RED1.getIndex());
        styleErrorNotEnough.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.ERROR_TOO_MUCH, styleErrorNotEnough);

        CellStyle styleWarning = wb.createCellStyle();
        styleWarning.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        styleWarning.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.WARNING, styleWarning);

        CellStyle styleOk = wb.createCellStyle();
        styleOk.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styleOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(Style.OK, styleOk);

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
