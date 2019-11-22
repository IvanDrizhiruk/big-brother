package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.WorkLogByDay;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.WorkLogByPerson;

@Slf4j
public class ExcelReportGenerator {

	private final File reportRoot;

	public ExcelReportGenerator(String reportRoot) {
		this.reportRoot = new File(reportRoot);

		if (!this.reportRoot.isDirectory()) {
			throw new IllegalArgumentException("Report root directory does not exist. Path: " + reportRoot);
		}
	}

	public void generate(TasksTree tasksTree) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(String.format("%s-%s", tasksTree.getProject(), tasksTree.getSprint()));

		generate(tasksTree, workbook, sheet);

		File report = new File(reportRoot, String.format("[%s]-[%s].xlsx", tasksTree.getProject(), tasksTree.getSprint()));

		try {
			FileOutputStream outputStream = new FileOutputStream(report);
			workbook.write(outputStream);
			workbook.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generate(TasksTree tasksTree, XSSFWorkbook workbook, XSSFSheet sheet) {

		Map<Styles, CellStyle> styles = createStyles(workbook);

		AtomicInteger rowNum = new AtomicInteger(0);

		Row rowProjectName = sheet.createRow(rowNum.getAndIncrement());
		Cell cell = rowProjectName.createCell(0);
		cell.setCellStyle(styles.get(Styles.H1));
		cell.setCellValue("Project: ");
		rowProjectName.createCell(1).setCellValue(tasksTree.getProject());
		rowProjectName.setRowStyle(styles.get(Styles.H1));
		rowProjectName.setHeightInPoints(25);

		Row rowSprintName = sheet.createRow(rowNum.getAndIncrement());
		rowSprintName.createCell(0).setCellValue("Sprint:");
		rowSprintName.createCell(1).setCellValue(tasksTree.getProject());
		rowSprintName.setRowStyle(styles.get(Styles.H2));
		rowSprintName.setHeightInPoints(25);

		tasksTree.getRootTasks()
				.forEach(rootTask -> {
					Row rowTaskName = sheet.createRow(rowNum.getAndIncrement());
					rowTaskName.createCell(0).setCellValue("Task:");
					rowTaskName.createCell(1).setCellValue(rootTask.getId());
					rowTaskName.createCell(3).setCellValue(rootTask.getName());
					rowTaskName.setRowStyle(styles.get(Styles.H3));
					rowTaskName.setHeightInPoints(25);

					TaskMetrics taskMetrics = tasksTree.getTaskMetricsByTaskId().get(rootTask.getId());

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

								TaskMetrics subTaskMetrics = tasksTree.getTaskMetricsByTaskId().get(subTask.getId());

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
										rowSubWorkLogByDayMetrics.createCell(3).setCellValue(convertMinutesToHour(personWorkLog.getMinutesSpent()));
									});
								}

							});

					newRowSeparator(sheet, rowNum);

				});
	}

	private void newRowSeparator(XSSFSheet sheet, AtomicInteger rowNum) {
		sheet.createRow(rowNum.getAndIncrement());
	}

	private static Integer convertMinutesToHour(Integer minutes) {
		if (null == minutes || minutes.equals(0)) {
			return 0;
		}
		return minutes / 60;
	}

	enum Styles {
		H1,
		H2,
		H3
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

		//		Font itemFont = wb.createFont();
		//		itemFont.setFontHeightInPoints((short) 9);
		//		itemFont.setFontName("Trebuchet MS");
		//		style = wb.createCellStyle();
		//		style.setAlignment(HorizontalAlignment.LEFT);
		//		style.setFont(itemFont);
		//		styles.put("item_left", style);
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
