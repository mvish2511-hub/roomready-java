package com.roomready.error;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The one place that turns errors into a ProblemDetail (application/problem+json). The base class already answers
 * Spring's own errors - an unknown path, a wrong method - as ProblemDetails; this adds the domain refusals and a bad
 * query value: one that does not fit its type, or a number outside its range.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail refusal(DomainException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(e.status()), e.getMessage());
        problem.setType(URI.create(e.type()));
        problem.setTitle(e.title());
        e.properties().forEach(problem::setProperty);
        return problem;
    }

    /** A query value that does not fit its type - ?state=painting, ?floor=x - is 400 /errors/validation.*/
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(                                                        TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        return handleExceptionInternal(ex, validation(field, "'" + ex.getValue() + "' is not an allowed value"),
                headers, HttpStatus.BAD_REQUEST, request);
    }

    /** A query value outside its range - ?floor=9 against @Min(1) @Max(4) - is 400 /errors/validation too.*/
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(                                                                            HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        var invalid = ex.getParameterValidationResults().get(0);
        String field = invalid.getMethodParameter().getParameterName();
        return handleExceptionInternal(ex, validation(field, String.valueOf(invalid.getResolvableErrors().get(0)
                        .getDefaultMessage())), headers, HttpStatus.BAD_REQUEST, request);
    }

    static ProblemDetail validation(String field, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request is not valid");
        problem.setType(URI.create("/errors/validation"));
        problem.setTitle("Invalid request");
        problem.setProperty("errors", List.of(Map.of("field", String.valueOf(field), "message", message)));
        return problem;
    }
}
