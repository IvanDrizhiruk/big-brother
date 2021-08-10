package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import ua.dp.dryzhyryk.big.brother.core.configuration.ConfigurationService;
import ua.dp.dryzhyryk.big.brother.core.ports.model.configuration.TeamConfiguration;
import ua.dp.dryzhyryk.big.brother.core.ports.model.person.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValueWithValidation;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;

public class TaskMetricsForPeopleValidator {

    private final ConfigurationService configurationService;

    public TaskMetricsForPeopleValidator(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ValueWithValidation<TimeSpentByDay> validate(TimeSpentByDay timeSpentByDay, String teamName) {

        TeamConfiguration teamConfiguration = configurationService.getConfigurationForTeam(teamName);

        if (timeSpentByDay.getTimeSpentMinutes() < teamConfiguration.getNotEnoughTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day less then " + TimeUtils.convertMinutesToHour(teamConfiguration.getNotEnoughTimeLoggedByDayInMinutes()) + "h";
            return ValueWithValidation.valueWithErrorStatus(timeSpentByDay, note);
        }

        if (timeSpentByDay.getTimeSpentMinutes() < teamConfiguration.getMinTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day less then " + TimeUtils.convertMinutesToHour(teamConfiguration.getMinTimeLoggedByDayInMinutes()) + "h";
            return ValueWithValidation.valueWithWarningStatus(timeSpentByDay, note);
        }

        if (timeSpentByDay.getTimeSpentMinutes() > teamConfiguration.getMaxTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day more then " + TimeUtils.convertMinutesToHour(teamConfiguration.getMaxTimeLoggedByDayInMinutes()) + "h";
            return ValueWithValidation.valueWithErrorStatus(timeSpentByDay, note);
        }

        return ValueWithValidation.valueWithOkStatus(timeSpentByDay);
    }
}
