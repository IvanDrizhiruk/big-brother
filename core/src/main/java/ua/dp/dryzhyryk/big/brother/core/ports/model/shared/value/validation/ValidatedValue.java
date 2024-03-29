package ua.dp.dryzhyryk.big.brother.core.ports.model.shared.value.validation;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ValidatedValue<T> {

    private final T value;

    private final ValidationStatus validationStatus;
    private final List<ValidationNote> validationNotes;

    public static <T> ValidatedValue<T> valueWithNotEvaluatedStatus(T value) {
        return ValidatedValue.<T>builder()
                .value(value)
                .validationStatus(ValidationStatus.NOT_EVALUATED)
                .build();
    }

    public static <T> ValidatedValue<T> valueWithWarningStatus(T value, String note) {
        return valueWithStatus(value, NoteType.WARNING, ValidationStatus.WARNING, note);
    }

    public static <T> ValidatedValue<T> valueWithErrorStatus(T value, String note) {
        return valueWithStatus(value, NoteType.ERROR, ValidationStatus.ERROR, note);
    }

    private static <T> ValidatedValue<T> valueWithStatus(T value, NoteType noteType, ValidationStatus validationStatus, String note) {
        List<ValidationNote> validationNotes = List.of(
                ValidationNote.builder()
                        .noteType(noteType)
                        .note(note)
                        .build()
        );

        return ValidatedValue.<T>builder()
                .value(value)
                .validationStatus(validationStatus)
                .validationNotes(validationNotes)
                .build();
    }

    public static <T> ValidatedValue<T> valueWithOkStatus(T value) {
        return ValidatedValue.<T>builder()
                .value(value)
                .validationStatus(ValidationStatus.OK)
                .build();
    }
}
