package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.CellWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;

public class CellPoiWrapper implements CellWrapper {
	private final RowPoiWrapper row;
	private final XSSFCell cell;
	private final Map<Style, CellStyle> styles;
	private final CellPoiCommentCreator cellCommentCreator;

	public CellPoiWrapper(RowPoiWrapper row, XSSFCell cell, Map<Style, CellStyle> styles, CellPoiCommentCreator cellCommentCreator) {
		this.row = row;
		this.cell = cell;
		this.styles = styles;
		this.cellCommentCreator = cellCommentCreator;
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
	public CellWrapper withComment(String cellComment) {

		CellPoiCommentWrapper comment = cellCommentCreator.newComment(cellComment);

		cell.setCellComment(comment.comment);

		return this;
	}

	@Override
	public RowWrapper buildCell() {
		return row;
	}
}
