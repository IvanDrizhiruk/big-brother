package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.CellWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;

public class CellPoiWrapper implements CellWrapper {
	private final RowWrapper row;
	private final XSSFCell cell;
	private final Map<Style, CellStyle> styles;

	public CellPoiWrapper(RowWrapper row, XSSFCell cell, Map<Style, CellStyle> styles) {
		this.row = row;
		this.cell = cell;
		this.styles = styles;
	}

	@Override
	public CellWrapper withStyle(Style cellStyle) {
		cell.setCellStyle(styles.get(cellStyle));
		return this;
	}

	@Override
	public CellWrapper withValue(String cellValue) {
		cell.setCellValue(cellValue);
		return this;
	}

	@Override
	public CellWrapper withValue(double cellValue) {
		cell.setCellValue(cellValue);
		return this;
	}

	@Override
	public RowWrapper buildCell() {
		return row;
	}
}
