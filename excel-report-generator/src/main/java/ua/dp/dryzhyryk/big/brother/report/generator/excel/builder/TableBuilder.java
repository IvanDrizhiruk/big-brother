package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import java.util.List;

public interface TableBuilder {
	TableBuilder header(List<String> headerData);

	TableBuilder body(List<List<String>> bodyData);

	TableBuilder footer(List<String> footerData);

	TableBuilder footerCells(List<TableCell> footerData);
}
