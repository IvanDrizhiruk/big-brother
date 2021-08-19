package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.CellWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.SheetWrapper;

public class RowPoiWrapper implements RowWrapper {
	protected final SheetPoiWrapper sheetWrapper;
	private final XSSFRow row;
	private final Map<Style, CellStyle> styles;
	private final AtomicInteger cellNum = new AtomicInteger(0);

	public RowPoiWrapper(SheetPoiWrapper sheetWrapper, XSSFRow row, Map<Style, CellStyle> styles) {
		this.sheetWrapper = sheetWrapper;
		this.row = row;
		this.styles = styles;
	}

	@Override
	public CellWrapper cell() {
		XSSFCell xssfCell = row.createCell(cellNum.getAndIncrement());
		return new CellPoiWrapper(this, xssfCell, styles);
	}

	@Override
	public CellWrapper cell(String cellValue) {
		XSSFCell xssfCell = row.createCell(cellNum.getAndIncrement());
		return new CellPoiWrapper(this, xssfCell, styles)
				.withValue(cellValue);
	}

	@Override
	public CellWrapper cell(double cellValue) {
		XSSFCell xssfCell = row.createCell(cellNum.getAndIncrement());
		return new CellPoiWrapper(this, xssfCell, styles)
				.withValue(cellValue);
	}

	@Override
	public RowWrapper withStyle(Style rowStyle) {
		row.setRowStyle(styles.get(rowStyle));
		return this;
	}

	@Override
	public RowWrapper withHeightInPoints(float heightInPoints) {
		row.setHeightInPoints(heightInPoints);
		return this;
	}

	@Override
	public SheetWrapper buildRow() {
		return sheetWrapper;
	}
}
