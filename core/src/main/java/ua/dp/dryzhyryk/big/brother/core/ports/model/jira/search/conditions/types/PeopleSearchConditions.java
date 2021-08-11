package ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditionType;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class PeopleSearchConditions implements JiraSearchConditions {

    @NonNull
    private final String teamName;
    @NonNull
    private final List<String> peopleNames;
    @NonNull
    private final LocalDate startPeriod;
    @NonNull
    private final LocalDate endPeriod;

    @Override
    public JiraSearchConditionType getSearchConditionType() {
        return JiraSearchConditionType.PEOPLE;
    }
}
