package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class PeopleSearchRequest {

    private final String teamName;
    private final List<String> peopleNames;
    @Deprecated
    private final LocalDate beginOfTheTime;
    @Deprecated
    private final int periodDurationInDays;
}
