package ua.dp.dryzhyryk.big.brother.core.validator;

import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;
import ua.dp.dryzhyryk.big.brother.core.validator.model.PearsonCoefficientThresholds;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationInformation;
import ua.dp.dryzhyryk.big.brother.core.validator.model.ValidationStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class ReportByPersonValidator {

    public static final int NOT_ENOUGH_TIME_LOGGED_BY_DAY_IN_MINUTES = 5 * 60;
    public static final int MIN_TIME_LOGGED_BY_DAY_IN_MINUTES = 6 * 60;
    public static final int MAX_TIME_LOGGED_BY_DAY_IN_MINUTES = 7 * 60;
    public static final PearsonCoefficientThresholds DEFAULT_PEARSON_COEFFICIENT_THRESHOLDS =
            PearsonCoefficientThresholds.builder()
                    .errorLessCoefficientThreshold(0.5f)
                    .warningLessCoefficientThreshold(0.8f)
                    .warningMoreCoefficientThreshold(1.5f)
                    .errorMoreCoefficientThreshold(2.0f)
                    .build();


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

    public ValidationInformation validateDay(LocalDate day) {
        if (day.getDayOfWeek() == DayOfWeek.SATURDAY
                || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return ValidationInformation.builder()
                    .message("Work logged in weekend")
                    .validationStatus(ValidationStatus.WARNING)
                    .build();
        }

        return ValidationInformation.NONE;
    }

    public ValidationInformation validateTC(float timeCoefficient, String person) {
        PearsonCoefficientThresholds thresholds = DEFAULT_PEARSON_COEFFICIENT_THRESHOLDS;
         if (timeCoefficient < thresholds.getErrorLessCoefficientThreshold()) {
             return ValidationInformation.builder()
                     .message("Task made extremely slow")
                     .validationStatus(ValidationStatus.ERROR_NOT_ENOUGH)
                     .build();
         }

        if (timeCoefficient < thresholds.getWarningLessCoefficientThreshold()) {
            return ValidationInformation.builder()
                    .message("Task made to slow")
                    .validationStatus(ValidationStatus.WARNING)
                    .build();
        }

        if (timeCoefficient > thresholds.getErrorMoreCoefficientThreshold()) {
            return ValidationInformation.builder()
                    .message("Task made extremely fast")
                    .validationStatus(ValidationStatus.ERROR_TOO_MUCH)
                    .build();
        }

        if (timeCoefficient > thresholds.getWarningMoreCoefficientThreshold()) {
            return ValidationInformation.builder()
                    .message("Task made to fast")
                    .validationStatus(ValidationStatus.WARNING)
                    .build();
        }

        return ValidationInformation.OK;
    }
}
