package ru.greatstep.exceltosqlconverter.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.http.HttpStatusCode;
import ru.greatstep.exceltosqlconverter.models.ErrorResponse;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WebClientException extends RuntimeException {

    private final ErrorResponse body;
    private final HttpStatusCode httpStatus;

}
