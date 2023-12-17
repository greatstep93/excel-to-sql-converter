package ru.greatstep.exceltosqlconverter.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.http.HttpStatusCode;
import ru.greatstep.exceltosqlconverter.models.FacadeErrorResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FacadeWebClientException extends RuntimeException {

    private final FacadeErrorResponse body;
    private final HttpStatusCode httpStatus;

}
