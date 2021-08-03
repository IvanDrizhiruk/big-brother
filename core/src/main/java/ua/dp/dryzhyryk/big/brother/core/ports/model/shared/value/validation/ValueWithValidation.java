package ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ValueWithValidation <T> {

	private final T value;

	private final ValidationStatus validationStatus;
	private final List<ValidationStatus> validationNotes;

	public static <T> ValueWithValidation<T> valueWithNotEvaluatedValidationStatus(T value) {
		return ValueWithValidation.<T>builder()
				.value(value)
				.validationStatus(ValidationStatus.NOT_EVALUATED)
				.build();

	}
}
