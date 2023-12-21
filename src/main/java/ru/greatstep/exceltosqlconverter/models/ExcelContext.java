package ru.greatstep.exceltosqlconverter.models;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelContext {

    private Map<String, String> formats;
    private List<Map<String, String>> rows;

}

