package ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.core.configuration.ConfigurationService;
import ua.dp.dryzhyryk.big.brother.core.ports.model.configuration.TeamConfiguration;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation.ValidatedValue;

import java.time.LocalDate;

class TaskMetricsForPeopleValidatorTest {

    @Test
    public void okShouldBeGeneratedForTimeSpentByDayCase1() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 7 * 60;

        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ConfigurationService configurationService = mockConfigurationService(teamName);


        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithOkStatus(
                newTimeSpentByDay(timeSpentMinutes));

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void okShouldBeGeneratedForTimeSpentByDayCase2() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 8 * 60;

        ConfigurationService configurationService = mockConfigurationService(teamName);
        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithOkStatus(
                newTimeSpentByDay(timeSpentMinutes));

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private ConfigurationService mockConfigurationService(String teamName) {
        TeamConfiguration teamConfiguration = TeamConfiguration.builder()
                .build();

        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getConfigurationForTeam(teamName))
                .thenReturn(teamConfiguration);

        return configurationService;
    }

    @Test
    public void warningShouldBeGeneratedForTimeSpentByDayCase1() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 7 * 60 - 1;

        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ConfigurationService configurationService = mockConfigurationService(teamName);


        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithWarningStatus(
                newTimeSpentByDay(timeSpentMinutes),
                "Time logged in day less then 7.0h");

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void warningShouldBeGeneratedForTimeSpentByDayCase2() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 5 * 60;

        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ConfigurationService configurationService = mockConfigurationService(teamName);


        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithWarningStatus(
                newTimeSpentByDay(timeSpentMinutes),
                "Time logged in day less then 7.0h");

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void errorShouldBeGeneratedForTimeSpentByDayLessThreshold() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 5 * 60 - 1;

        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ConfigurationService configurationService = mockConfigurationService(teamName);


        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithErrorStatus(
                newTimeSpentByDay(timeSpentMinutes),
                "Time logged in day less then 5.0h");

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void errorShouldBeGeneratedForTimeSpentByDayMoteThreshold() {
        //given
        String teamName = "Ducks";
        int timeSpentMinutes = 8 * 60 +1;

        TimeSpentByDay timeSpentByDay = newTimeSpentByDay(timeSpentMinutes);

        ConfigurationService configurationService = mockConfigurationService(teamName);


        ValidatedValue<TimeSpentByDay> expected = ValidatedValue.valueWithErrorStatus(
                newTimeSpentByDay(timeSpentMinutes),
                "Time logged in day more then 8.0h");

        //when
        TaskMetricsForPeopleValidator validator = new TaskMetricsForPeopleValidator(configurationService);
        ValidatedValue<TimeSpentByDay> actual = validator.validate(timeSpentByDay, teamName);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private TimeSpentByDay newTimeSpentByDay(int timeSpentMinutes) {
        return TimeSpentByDay.builder()
                .day(LocalDate.of(2021, 8, 10))
                .timeSpentMinutes(timeSpentMinutes)
                .build();
    }
}