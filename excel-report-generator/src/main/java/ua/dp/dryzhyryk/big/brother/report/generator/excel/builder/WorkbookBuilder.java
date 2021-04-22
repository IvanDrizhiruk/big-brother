package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import java.io.File;

public interface WorkbookBuilder {
	SheetWrapper sheet(String sheetName);

	void saveReportFile(File reportRoot, String reportFileName);
}
