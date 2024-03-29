package ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log;

import ua.dp.dryzhyryk.big.brother.core.configuration.ConfigurationService;
import ua.dp.dryzhyryk.big.brother.core.ports.model.configuration.TeamConfiguration;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;
import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;

public class TaskWorkingLogsForPeopleValidator {

    private final ConfigurationService configurationService;

    public TaskWorkingLogsForPeopleValidator(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ValidatedValue<TimeSpentByDay> validate(TimeSpentByDay timeSpentByDay, String teamName) {

        TeamConfiguration teamConfiguration = configurationService.getConfigurationForTeam(teamName);

        if (timeSpentByDay.getTimeSpentMinutes() < teamConfiguration.getNotEnoughTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day less then " + TimeUtils.convertMinutesToHour(teamConfiguration.getNotEnoughTimeLoggedByDayInMinutes()) + "h";
            return ValidatedValue.valueWithErrorStatus(timeSpentByDay, note);
        }

        if (timeSpentByDay.getTimeSpentMinutes() < teamConfiguration.getMinTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day less then " + TimeUtils.convertMinutesToHour(teamConfiguration.getMinTimeLoggedByDayInMinutes()) + "h";
            return ValidatedValue.valueWithWarningStatus(timeSpentByDay, note);
        }

        if (timeSpentByDay.getTimeSpentMinutes() > teamConfiguration.getMaxTimeLoggedByDayInMinutes()) {
            String note = "Time logged in day more then " + TimeUtils.convertMinutesToHour(teamConfiguration.getMaxTimeLoggedByDayInMinutes()) + "h";
            return ValidatedValue.valueWithErrorStatus(timeSpentByDay, note);
        }

        return ValidatedValue.valueWithOkStatus(timeSpentByDay);
    }
}
