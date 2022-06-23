package ua.dp.dryzhyryk.big.brother.core.ports.model.view.request;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PeopleSearchTaskExcludeConditions {

	@NonNull
	List<ExcludedFieldNameAndValuePair> byFields;
	@NonNull
	Set<String> byStatus;

	@Value
	public static class ExcludedFieldNameAndValuePair {
		@NonNull
		String name;
		@NonNull
		String value;
	}
}
