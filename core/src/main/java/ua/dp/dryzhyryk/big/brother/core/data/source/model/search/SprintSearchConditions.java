package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SprintSearchConditions {
    @NonNull
    private final String project;
    @NonNull
    private final String sprint;
}
