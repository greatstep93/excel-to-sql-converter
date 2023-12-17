package ru.greatstep.exceltosqlconverter.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final Map<String, Object> NAMES_PREFIX = Map.of(
            "Mr.", "Mr.",
            "Mrs.", "Mrs.",
            "Ms.", "Ms.",
            "Miss", "Miss",
            "Dr.", "Dr.");

}
