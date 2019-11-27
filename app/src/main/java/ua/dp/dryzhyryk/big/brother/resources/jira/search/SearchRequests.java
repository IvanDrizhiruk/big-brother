package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SearchRequests {
    private final List<PeopleSearchRequest> peopleSearchConditions;
    private final List<SprintSearchRequest> sprintSearchConditions;
}
