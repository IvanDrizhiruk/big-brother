package ua.dp.dryzhyryk.big.brother.report.generator.excel.builder;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.Style;

@Value
@Builder
public class TableCell {

	private final String value;
	private final Style style;
	private final String cellComment;
}
