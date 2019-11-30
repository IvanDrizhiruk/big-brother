package ua.dp.dryzhyryk.big.brother.core.validator;

import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationInformation;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationStatus;

public class ReportByPersonValidator {

    public static final int NOT_ENOUGH_TIME_LOGGED_BY_DAY_IN_MINUTES = 5 * 60;
    public static final int MIN_TIME_LOGGED_BY_DAY_IN_MINUTES = 6 * 60;
    public static final int MAX_TIME_LOGGED_BY_DAY_IN_MINUTES = 7 * 60;

    public ValidationInformation validateTotalTimeSpentByDay(Integer totalTimeSpentByDayInMinutes) {
        if (totalTimeSpentByDayInMinutes == null || totalTimeSpentByDayInMinutes < NOT_ENOUGH_TIME_LOGGED_BY_DAY_IN_MINUTES) {
            return ValidationInformation.builder()
                    .message("Time logged in day less " + TimeUtils.convertMinutesToHour(MIN_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.ERROR_NOT_ENOUGH)
                    .build();
        }

        if (totalTimeSpentByDayInMinutes < MIN_TIME_LOGGED_BY_DAY_IN_MINUTES) {
            return ValidationInformation.builder()
                    .message("Time logged in day less " + TimeUtils.convertMinutesToHour(MIN_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.WARNING)
                    .build();
        }

        if (totalTimeSpentByDayInMinutes > MAX_TIME_LOGGED_BY_DAY_IN_MINUTES) {
            return ValidationInformation.builder()
                    .message("Time logged in day more then " + TimeUtils.convertMinutesToHour(MAX_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.ERROR_TOO_MUCH)
                    .build();
        }


        return ValidationInformation.OK;
    }

    public ValidationInformation validateTotalTimeSpentByPeriod(
            int totalTimeSpentInCurrentPeriodInMinutes, int daysInCurrentPeriod) {

        if (totalTimeSpentInCurrentPeriodInMinutes < daysInCurrentPeriod * NOT_ENOUGH_TIME_LOGGED_BY_DAY_IN_MINUTES) {

            return ValidationInformation.builder()
                    .message("Time logged in day less " + TimeUtils.convertMinutesToHour(daysInCurrentPeriod * MIN_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.ERROR_NOT_ENOUGH)
                    .build();
        }

        if (totalTimeSpentInCurrentPeriodInMinutes < daysInCurrentPeriod * MIN_TIME_LOGGED_BY_DAY_IN_MINUTES) {
            return ValidationInformation.builder()
                    .message("Time logged in day less " + TimeUtils.convertMinutesToHour(daysInCurrentPeriod * MIN_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.WARNING)
                    .build();
        }

        if (totalTimeSpentInCurrentPeriodInMinutes > daysInCurrentPeriod * MAX_TIME_LOGGED_BY_DAY_IN_MINUTES) {
            return ValidationInformation.builder()
                    .message("Time logged in day more then (minutes) " + TimeUtils.convertMinutesToHour(daysInCurrentPeriod * MAX_TIME_LOGGED_BY_DAY_IN_MINUTES))
                    .validationStatus(ValidationStatus.ERROR_TOO_MUCH)
                    .build();
        }


        return ValidationInformation.OK;
    }
}
