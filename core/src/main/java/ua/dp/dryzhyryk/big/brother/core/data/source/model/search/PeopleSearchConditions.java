package ua.dp.dryzhyryk.big.brother.core.data.source.model.search;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class PeopleSearchConditions implements SearchConditions {

    @NonNull
    private final String teamName;
    @NonNull
    private final List<String> peopleNames;
    @NonNull
    private final LocalDate startPeriod;
    @NonNull
    private final LocalDate endPeriod;

    @Override
    public SearchConditionType getSearchConditionType() {
        return SearchConditionType.PEOPLE;
    }
}
