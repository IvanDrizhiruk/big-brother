package ua.dp.dryzhyryk.big.brother;

import org.jbehave.core.steps.Parameters;

public final class Utilities {

    private Utilities() {
        //do nothing
    }

    public static <T> T valueAs(Parameters parameter, String name, Class<T> type, T defaultValue) {
        String stringValue = parameter.values().get(name);
        if(null == stringValue || stringValue.isEmpty()) {
            return null;
        }

        return parameter.valueAs(name, type, defaultValue);
    }
}
