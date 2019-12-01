package ua.dp.dryzhyryk.big.brother.core.validator.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PearsonCoefficientThresholds {

    private final String person;
    private final float errorLessCoefficientThreshold;
    private final float warningLessCoefficientThreshold;
    private final float warningMoreCoefficientThreshold;
    private final float errorMoreCoefficientThreshold;
}
