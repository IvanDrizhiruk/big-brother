package ua.dp.dryzhyryk.big.brother.base;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.steps.Steps;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class BaseSteps extends Steps {

    @AsParameterConverter
    public LocalDateTime toLocalDateTime(String text) {
        return LocalDateTime.parse(text);
    }

    @AsParameterConverter
    public LocalDate toLocalDate(String text) {
        return LocalDate.parse(text);
    }
}
