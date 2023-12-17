package ru.greatstep.exceltosqlconverter.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.greatstep.exceltosqlconverter.config.FakeNameDeserializer;

@JsonDeserialize(using = FakeNameDeserializer.class)
public record FakeName(
        String fullName,
        String firstName,
        String fatherName,
        String lastName
) {

}
