package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchRequests {
	List<PeopleSearchRequest> peopleSearchConditions;
	List<SprintSearchRequest> sprintSearchConditions;
}
