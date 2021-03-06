package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class PersonSearchConditions implements SearchConditions {

    @NonNull
    private final String personName;
    @NonNull
    private final LocalDate startPeriod;
    @NonNull
    private final LocalDate endPeriod;

    @Override
    public SearchConditionType getSearchConditionType() {
        return SearchConditionType.PERSON;
    }
}
