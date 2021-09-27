package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.RowWrapper;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.TableCell;

public class TablePoiBuilder implements TableBuilder {
	private final SheetPoiWrapper sheetPoiWrapper;
	public TablePoiBuilder(SheetPoiWrapper sheetPoiWrapper) {
		this.sheetPoiWrapper = sheetPoiWrapper;
	}

	@Override
	public TableBuilder header(List<String> headerData) {
		this.row(headerData);
		return this;
	}

	@Override
	public TableBuilder body(List<List<String>> bodyData) {
		bodyData.forEach(this::row);
		return this;
	}

	@Override
	public TableBuilder bodyCells(List<List<TableCell>> bodyData) {
		bodyData.forEach(rowData -> {
			RowWrapper row = sheetPoiWrapper.row();
			rowData.forEach(data ->
					row
							.cell(data.getValue())
							.withStyle(data.getStyle())
							.withComment(data.getCellComment())
							.buildCell());
		});

		return this;
	}

	@Override
	public TableBuilder footer(List<String> footerData) {
		this.row(footerData);
		return this;
	}

	@Override
	public TableBuilder footerCells(List<TableCell> footerData) {
		this.rowCells(footerData);
		return this;
	}

	private void row(List<String> rowData) {
		RowWrapper row = sheetPoiWrapper.row();
		rowData.forEach(row::cell);
	}

	private void rowCells(List<TableCell> cellsData) {
		RowWrapper row = sheetPoiWrapper.row();
		cellsData.forEach(data ->
				row
					.cell(data.getValue())
					.withStyle(data.getStyle())
					.withComment(data.getCellComment())
					.buildCell());
	}
}
