package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PeopleSearchRequest {

	String teamName;
	List<String> peopleNames;
	TasksGroupingConditions unFunctionalTasksGroupConditions;
	TasksGroupingConditions inProgressTasksGroupConditions;
}
