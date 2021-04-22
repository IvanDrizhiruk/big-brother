package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;

public interface WorkbookBuilderFactory {
	WorkbookBuilder prepareBuilder(ReportFileExtension reportFileExtension);
}
