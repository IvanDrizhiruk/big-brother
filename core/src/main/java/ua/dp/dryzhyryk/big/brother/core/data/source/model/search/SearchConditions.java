package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchConditions {
	private final List<SprintSearchConditions> projects;
}
