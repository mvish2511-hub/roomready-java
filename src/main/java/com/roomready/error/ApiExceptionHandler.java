package com.roomready.error;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns domain refusals and malformed requests into ProblemDetails: a stable type and safe detail for the caller,
 * with extra properties where useful. Validation errors and a body Jackson cannot read -
 * and anything else that goes wrong.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ProblemDetail refusal(DomainException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        org.springframework.http.HttpStatusCode.valueOf(e.status()), e.getMessage());
        problem.setType(java.net.URI.create(e.type()));
        problem.setTitle(e.title());
        e.properties().forEach(problem::setProperty);
        return problem;
    }

    /**
     * Anything else is a 500 ProblemDetail too (every error is one), with a generic detail - never the
     * exception's message - and the exception in the log.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e) {
        log.error("unexpected error", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            TypeMismatchException.class})
    public ProblemDetail badRequest(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request is invalid");
        problem.setType(java.net.URI.create("/errors/invalid-request"));
        problem.setTitle("Invalid request");
        return problem;
    }
}
