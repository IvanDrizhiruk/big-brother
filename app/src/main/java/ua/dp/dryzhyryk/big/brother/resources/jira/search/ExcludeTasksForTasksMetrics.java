package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExcludeTasksForTasksMetrics {

	List<ExcludedFieldNameAndValue> byFields;
	Set<String> byStatus;

	@Value
	public static class ExcludedFieldNameAndValue {
		String name;
		String value;
	}
}
