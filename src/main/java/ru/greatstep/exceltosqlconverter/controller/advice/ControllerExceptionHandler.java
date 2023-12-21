//package ru.greatstep.exceltosqlconverter.controller.advice;
//
//import static ru.greatstep.exceltosqlconverter.utils.Constants.ERROR_DELIMITER;
//import static ru.greatstep.exceltosqlconverter.utils.Error.EX_ILLEGAL_ARGUMENT;
//import static ru.greatstep.exceltosqlconverter.utils.Error.EX_SYSTEM_ERROR;
//import static ru.greatstep.exceltosqlconverter.utils.Error.EX_VALIDATION_ERROR;
//
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.JsonMappingException.Reference;
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.util.ReflectionUtils;
//import org.springframework.validation.ObjectError;
//import org.springframework.web.HttpRequestMethodNotSupportedException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.MissingRequestHeaderException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.context.request.WebRequest;
//import ru.greatstep.exceltosqlconverter.models.ErrorResponse;
//
//@RestControllerAdvice
//@RequiredArgsConstructor
//public class ControllerExceptionHandler {
//
//    private static final String EXCEPTION_LOG = "Exception: %s";
//
//    private final Logger logger;
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleOtherException(Exception e, WebRequest request) {
//        return logErrorAndReturnResponse(e, request, EX_SYSTEM_ERROR.name());
//    }
//
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
//            HttpMessageNotReadableException e, WebRequest request) {
//        if (e.getCause() instanceof JsonMappingException error) {
//            logException(e, request);
//            return prepareResponse(getJsonMappingExceptionMessage(error),
//                    EX_VALIDATION_ERROR.name(),
//                    ExceptionUtils.getStackTrace(e));
//        }
//        return logErrorAndReturnResponse(e, request, EX_ILLEGAL_ARGUMENT.name());
//    }
//
//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
//            HttpRequestMethodNotSupportedException e, WebRequest request) {
//        return logErrorAndReturnResponse(e, request, EX_ILLEGAL_ARGUMENT.name());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e,
//                                                                    WebRequest request) {
//        logException(e, request);
//        return prepareResponse(getLocalizedMessage(e),
//                EX_VALIDATION_ERROR.name(),
//                ExceptionUtils.getStackTrace(e));
//    }
//
//    @ExceptionHandler(MissingRequestHeaderException.class)
//    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e,
//                                                                             WebRequest request) {
//        return logErrorAndReturnResponse(e, request, EX_VALIDATION_ERROR.name());
//    }
//
//    private ResponseEntity<ErrorResponse> prepareResponse(String msg,
//                                                          String error,
//                                                          String stackTrace) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ErrorResponse
//                        .builder()
//                        .message(msg)
//                        .error(error)
//                        .stackTrace(stackTrace)
//                        .build());
//    }
//
//    private ResponseEntity<ErrorResponse> logErrorAndReturnResponse(Exception e, WebRequest request,
//                                                                    String error) {
//        logException(e, request);
//        return prepareResponse(Optional.ofNullable(e.getCause()).orElse(e).getMessage(), error,
//                ExceptionUtils.getStackTrace(e));
//    }
//
//    private String getLocalizedMessage(MethodArgumentNotValidException e) {
//        return Arrays.stream(e.getBindingResult().getAllErrors().toArray())
//                .map(x -> ((ObjectError) x).getDefaultMessage())
//                .collect(Collectors.joining(ERROR_DELIMITER));
//    }
//
//    private void logException(Exception e, WebRequest request) {
//        logger.log(Level.ALL, String.format(EXCEPTION_LOG,
//                Optional.ofNullable(e.getCause()).orElse(e).getMessage()), e);
//    }
//
//    private String getJsonMappingExceptionMessage(JsonMappingException exception) {
//        String fields = exception.getPath().stream()
//                .filter(Objects::nonNull)
//                .map(Reference::getFieldName)
//                .filter(StringUtils::isNotBlank)
//                .collect(Collectors.joining("."));
//        Field field;
//        String value;
//        try {
//            field = exception.getClass().getDeclaredField("_value");
//            field.setAccessible(true);
//            value = String.valueOf(ReflectionUtils.getField(field, exception));
//        } catch (Exception e) {
//            value = null;
//        }
//
//        return String.format("Error format from field %s from value %s", fields, value);
//    }
//
//}
