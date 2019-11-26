package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import java.util.List;

import lombok.Builder;
import lombok.Value;

public interface SearchConditions {
	SearchConditionType getSearchConditionType();
}
