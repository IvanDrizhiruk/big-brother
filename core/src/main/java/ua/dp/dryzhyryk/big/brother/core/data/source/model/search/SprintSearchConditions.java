package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SprintSearchConditions implements SearchConditions {

	@NonNull
	private final String project;
	@NonNull
	private final String sprint;

	@Override
	public SearchConditionType getSearchConditionType() {
		return SearchConditionType.SPRINT;
	}
}
