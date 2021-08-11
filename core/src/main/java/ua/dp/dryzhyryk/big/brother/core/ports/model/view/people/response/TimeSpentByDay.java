package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TimeSpentByDay {

    private final LocalDate day;
    private final int timeSpentMinutes;
}
