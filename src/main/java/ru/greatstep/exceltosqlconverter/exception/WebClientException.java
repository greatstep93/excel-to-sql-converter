package ru.greatstep.exceltosqlconverter.exception;

import lombok.Getter;
import ru.greatstep.exceltosqlconverter.models.ErrorResponse;

@Getter
public class WebClientException extends RuntimeException {

    private final ErrorResponse response;

    public WebClientException(ErrorResponse response) {
        this.response = response;
    }

}
