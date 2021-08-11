package ua.dp.dryzhyryk.big.brother.core.ports.model.view.request;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class PeopleSearchConditions {

    @NonNull
    private final String teamName;
    @NonNull
    private final List<String> peopleNames;
    @NonNull
    private final LocalDate startPeriod;
    @NonNull
    private final LocalDate endPeriod;
}
