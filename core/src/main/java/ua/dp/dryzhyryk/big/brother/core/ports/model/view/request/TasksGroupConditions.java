package ua.dp.dryzhyryk.big.brother.core.ports.model.view.request;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TasksGroupConditions {

	List<FieldNameAndValuePair> byFields;
	Set<String> byStatus;

	@Value
	public static class FieldNameAndValuePair {
		@NonNull
		String name;
		@NonNull
		String value;
	}
}
