package ua.dp.dryzhyryk.big.brother.core.model.search;

import java.util.List;

import lombok.Value;

@Value
public class SearchConditions {
	private final List<SprintSearchConditions> projects;
}
