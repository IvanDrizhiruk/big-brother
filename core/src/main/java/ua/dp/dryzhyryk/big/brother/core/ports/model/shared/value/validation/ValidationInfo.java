package ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ValidationInfo {

	private final NoteType noteType;
	private final String note;
}
