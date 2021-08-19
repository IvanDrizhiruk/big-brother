package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;

public interface CellWrapper {
	CellWrapper withStyle(Style style);

	CellWrapper withValue(String cellValue);

	CellWrapper withValue(double cellValue);

	CellWrapper withComment(String cellComment);

	RowWrapper buildCell();
}
