package ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditionType;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class PersonSearchConditions implements JiraSearchConditions {

    @NonNull
    private final String personName;
    @NonNull
    private final LocalDate startPeriod;
    @NonNull
    private final LocalDate endPeriod;

    @Override
    public JiraSearchConditionType getSearchConditionType() {
        return JiraSearchConditionType.PERSON;
    }
}
