package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.ReportFileExtension;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;

public class WorkbookPoiBuilder implements WorkbookBuilder {
	private final XSSFWorkbook workbook;
	private final Map<Style, CellStyle> styles;

	public WorkbookPoiBuilder() {
		this.workbook = new XSSFWorkbook();
		this.styles = createStyles();
	}

	@Override
	public SheetWrapper sheet(String sheetName) {
		XSSFSheet xssfSheet = workbook.createSheet(sheetName);
		return new SheetPoiWrapper(xssfSheet, styles);
	}

	@Override
	public void saveReportFile(File reportRoot, String reportFileName) {
		String fullReportFileName = reportFileName + String.format(".%s", ReportFileExtension.XLSX.toString().toLowerCase());
		File reportFile = new File(reportRoot, fullReportFileName);
		try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to generate report", e);
		}
	}

	private Map<Style, CellStyle> createStyles() {
		Map<Style, CellStyle> stylesMap = new HashMap<>();

		Font titleFontH1 = workbook.createFont();
		titleFontH1.setFontHeightInPoints((short) 20);
		titleFontH1.setFontName("Trebuchet MS");

		CellStyle styleH1 = workbook.createCellStyle();
		styleH1.setFont(titleFontH1);
		styleH1.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleH1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.H1, styleH1);

		Font titleFontH2 = workbook.createFont();
		titleFontH2.setFontHeightInPoints((short) 18);
		titleFontH2.setFontName("Trebuchet MS");
		CellStyle styleH2 = workbook.createCellStyle();
		styleH2.setFont(titleFontH2);
		styleH2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleH2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.H2, styleH2);

		Font titleFontH3 = workbook.createFont();
		titleFontH3.setFontHeightInPoints((short) 16);
		titleFontH3.setFontName("Trebuchet MS");
		CellStyle styleH3 = workbook.createCellStyle();
		styleH3.setFont(titleFontH3);
		styleH3.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
		styleH3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.H3, styleH3);

		CellStyle styleError = workbook.createCellStyle();
		styleError.setFillForegroundColor(IndexedColors.ROSE.getIndex());
		styleError.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.ERROR_NOT_ENOUGH, styleError);

		CellStyle styleErrorNotEnough = workbook.createCellStyle();
		styleErrorNotEnough.setFillForegroundColor(IndexedColors.RED1.getIndex());
		styleErrorNotEnough.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.ERROR_TOO_MUCH, styleErrorNotEnough);

		CellStyle styleWarning = workbook.createCellStyle();
		styleWarning.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		styleWarning.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.WARNING, styleWarning);

		CellStyle styleOk = workbook.createCellStyle();
		styleOk.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleOk.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		stylesMap.put(Style.OK, styleOk);

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

		return stylesMap;
	}
}
