package ua.dp.dryzhyryk.big.brother.core.utils;

public class TimeUtils {

    public static Float convertMinutesToHour(Integer minutes) {
        return null == minutes
                ? null
                : ((float) minutes) / 60;
    }
}
