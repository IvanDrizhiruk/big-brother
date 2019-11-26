package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class PeopleView {

    private final String teamName;
    private final LocalDate startPeriod;
    private final LocalDate endPeriod;

    private final List<PersonMetrics> personMetrics;
}
