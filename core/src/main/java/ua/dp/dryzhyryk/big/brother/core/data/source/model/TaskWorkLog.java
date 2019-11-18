package ua.dp.dryzhyryk.big.brother.core.data.source.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class TaskWorkLog {
    private final String person;
    private final LocalDateTime startDateTime;
    private final int minutesSpent;
}
