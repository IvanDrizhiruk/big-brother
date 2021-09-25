package ua.dp.dryzhyryk.big.brother.report.generator.utils;

import java.util.Optional;

import ua.dp.dryzhyryk.big.brother.core.utils.TimeUtils;

//TODO add tests
public class Formatter {

	public static Float convertMinutesToHour(Integer minutes) {
		return TimeUtils.convertMinutesToHour(minutes);
	}

	public static String stringValueOrEmpty(Object data) {
		return Optional.ofNullable(data)
				.map(Object::toString)
				.orElse("");
	}
}
