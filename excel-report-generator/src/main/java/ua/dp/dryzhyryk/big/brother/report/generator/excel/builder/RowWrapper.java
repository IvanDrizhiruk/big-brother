package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;

public interface RowWrapper {
	CellWrapper cell();

	CellWrapper cell(String cellValue);

	CellWrapper cell(double cellValue);

	RowWrapper withStyle(Style rowStyle);

	RowWrapper withHeightInPoints(float heightInPoints);

	SheetWrapper buildRow();
}
