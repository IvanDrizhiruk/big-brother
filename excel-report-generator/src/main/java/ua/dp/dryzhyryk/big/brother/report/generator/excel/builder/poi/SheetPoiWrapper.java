package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;

public class SheetPoiWrapper implements SheetWrapper {
	protected final XSSFWorkbook workbook;
	protected final XSSFSheet sheet;
	private final Map<Style, CellStyle> styles;
	private final AtomicInteger rowNum = new AtomicInteger(0);

	public SheetPoiWrapper(XSSFWorkbook workbook, XSSFSheet sheet, Map<Style, CellStyle> styles) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.styles = styles;
	}

	@Override
	public RowWrapper row() {
		XSSFRow xssfRow = sheet.createRow(rowNum.getAndIncrement());
		return new RowPoiWrapper(this, xssfRow, styles);
	}

	@Override
	public void buildSheet() {

	}

	@Override
	public SheetWrapper whiteLine() {
		sheet.createRow(rowNum.getAndIncrement());
		return this;
	}

	@Override
	public SheetWrapper buildTable(Consumer<TableBuilder> consumer) {
		TableBuilder tableBuilder = new TablePoiBuilder(this);
		consumer.accept(tableBuilder);
		return this;
	}
}
