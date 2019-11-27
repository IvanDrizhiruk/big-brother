package ua.dp.dryzhyryk.big.brother.resources.jira.search;

import lombok.Value;

@Value
public class SprintSearchRequest {

    private final String project;
    private final String sprint;
}
