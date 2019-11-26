package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class TimeSpentByDay {

    private final LocalDate day;
    private final int timeSpentMinutes;
}
