package ua.dp.dryzhyryk.big.brother.core.validator.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationInformation {

    public final static ValidationInformation OK = ValidationInformation.builder()
            .validationStatus(ValidationStatus.OK)
            .build();

    private final String message;
    private final ValidationStatus validationStatus;
}
