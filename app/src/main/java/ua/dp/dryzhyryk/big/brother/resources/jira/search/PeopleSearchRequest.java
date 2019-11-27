package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class PeopleSearchRequest {

    private final String teamName;
    private final List<String> peopleNames;
    private final LocalDate beginOfTheTime;
    private final int periodDurationInDays;
}
