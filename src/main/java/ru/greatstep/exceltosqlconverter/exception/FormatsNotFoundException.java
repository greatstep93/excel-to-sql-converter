package ru.greatstep.exceltosqlconverter.exception;

import lombok.Getter;

@Getter
public class FormatsNotFoundException extends RuntimeException {

    public FormatsNotFoundException(String message) {
        super(message);
    }

}
