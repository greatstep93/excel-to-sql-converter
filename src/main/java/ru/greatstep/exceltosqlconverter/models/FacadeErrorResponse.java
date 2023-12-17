package ru.greatstep.exceltosqlconverter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacadeErrorResponse {
    private String error;
    private String message;
    private String stackTrace;
}
