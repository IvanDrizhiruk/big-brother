package ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class TaskWorkLog {
    private final String person;
    private final LocalDateTime startDateTime;
    private final int minutesSpent;
}
