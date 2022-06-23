package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PeopleSearchRequest {

    String teamName;
    List<String> peopleNames;
    ExcludeTasksForTasksMetrics excludeTasksForTasksMetrics;

}
