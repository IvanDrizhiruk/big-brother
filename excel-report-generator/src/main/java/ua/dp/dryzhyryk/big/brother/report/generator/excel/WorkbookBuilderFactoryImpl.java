package ua.dp.dryzhyryk.big.brother.report.generator.excel;

import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.WorkbookBuilder;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.builder.poi.WorkbookPoiBuilder;

public class WorkbookBuilderFactoryImpl implements WorkbookBuilderFactory {
	public WorkbookBuilder prepareBuilder(ReportFileExtension reportFileExtension) {
		switch(reportFileExtension) {
			case XLSX:
				return new WorkbookPoiBuilder();
			default:
				throw new IllegalArgumentException(String.format("Not supported reportFileExtension %s", reportFileExtension));
		}
	}
}
