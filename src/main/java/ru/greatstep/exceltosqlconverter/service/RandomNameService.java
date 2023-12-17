package ru.greatstep.exceltosqlconverter.service;

import java.util.List;
import ru.greatstep.exceltosqlconverter.models.FakeName;

public interface RandomNameService {

    List<FakeName> getFakeNames(Integer count);

}
