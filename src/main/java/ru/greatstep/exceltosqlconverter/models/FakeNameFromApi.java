package ru.greatstep.exceltosqlconverter.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FakeNameFromApi {

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("FatherName")
    private String fatherName;

}
