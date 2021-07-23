package com.sample.springmvc.common.aop;

import com.google.common.base.Throwables;
import com.sample.springmvc.common.exception.GenericError;
import com.sample.springmvc.common.exception.GenericResponse;
import com.sample.springmvc.common.exception.GenericException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionHandlingAspect extends ResponseEntityExceptionHandler {

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                               HttpHeaders headers, HttpStatus status,
                                                               WebRequest request) {
        GenericException genericException = new GenericException(GenericError.INVALID_PARAMETER_ERROR, exception);
        return buildErrorResponse(genericException, request);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
                                                          HttpHeaders headers,
                                                          HttpStatus status,
                                                          WebRequest request) {
        GenericException genericException;
        if (exception instanceof NoHandlerFoundException) {
            genericException = new GenericException(GenericError.RESOURCE_NOT_FOUND_ERROR, exception);
        } else {
            ResponseEntity<Object> responseEntity = super.handleExceptionInternal(exception, body,
                                                                                  headers, status, request);
            genericException = new GenericException(responseEntity.getStatusCode().value(),
                                                    responseEntity.getStatusCode().getReasonPhrase(),
                                                    responseEntity.getStatusCode(), exception);
        }
        return buildErrorResponse(genericException, request);
    }

    private ResponseEntity<Object> buildErrorResponse(GenericException genericException, WebRequest request) {
        GenericResponse errorResponse = new GenericResponse(genericException.getErrorCode(),
                                                            genericException.getErrorMessage());
        return ResponseEntity.status(genericException.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<Object> handleApiException(GenericException genericException, WebRequest request) {
        return buildErrorResponse(genericException, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception exception, WebRequest request) {
        GenericException genericException = new GenericException(GenericError.INTERNAL_ERROR, exception);
        return buildErrorResponse(genericException, request);
    }

}
