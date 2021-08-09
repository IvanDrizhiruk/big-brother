package ua.dp.dryzhyryk.big.brother.core.ports.model.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamConfiguration {

    private final int notEnoughTimeLoggedByDayInMinutes = 5 * 60;
    private final int minTimeLoggedByDayInMinutes = 7 * 60;
    private final int maxTimeLoggedByDayInMinutes = 8 * 60;
}
