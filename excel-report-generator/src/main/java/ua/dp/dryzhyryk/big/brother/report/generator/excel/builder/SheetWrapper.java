package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import java.util.function.Consumer;

public interface SheetWrapper {
	RowWrapper row();

	void buildSheet();

	SheetWrapper whiteLine();

	SheetWrapper buildTable(Consumer<TableBuilder> consumer);
}
