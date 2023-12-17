package ru.greatstep.exceltosqlconverter.models;

import java.util.List;
import lombok.Data;

@Data
public class DataToolsResponse {

    private List<FakeNameFromApi> fakeNames;

}
