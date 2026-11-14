package com.roomready.error;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.roomready.config.RequestIdFilter;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> refusal(
            DomainException e,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatusCode.valueOf(e.status()),
                e.getMessage());

        problem.setType(URI.create(e.type()));
        problem.setTitle(e.title());
        problem.setInstance(URI.create(request.getRequestURI()));

        e.properties().forEach(problem::setProperty);

        return problemResponse(problem);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> validation(
            BindException e,
            HttpServletRequest request) {

        ProblemDetail problem = newProblem(
                HttpStatus.BAD_REQUEST,
                "/errors/validation",
                "Validation failed",
                "The request is invalid",
                request);

        List<Map<String, String>> errors = new ArrayList<>();

        for (ObjectError error : e.getAllErrors()) {

            String field = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : error.getObjectName();

            errors.add(Map.of(
                    "field", field,
                    "message", error.getDefaultMessage() == null
                            ? "Invalid value"
                            : error.getDefaultMessage()));
        }

        problem.setProperty("errors", errors);

        return problemResponse(problem);
    }

    // Invalid query parameter or type conversion errors
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ProblemDetail> typeMismatch(
            TypeMismatchException e,
            HttpServletRequest request) {

        ProblemDetail problem = newProblem(
                HttpStatus.BAD_REQUEST,
                "/errors/validation",
                "Validation failed",
                "The request is invalid",
                request);

        return problemResponse(problem);
    }

    // Malformed request body or missing required parameter
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ProblemDetail> invalidRequest(
            Exception e,
            HttpServletRequest request) {

        ProblemDetail problem = newProblem(
                HttpStatus.BAD_REQUEST,
                "/errors/invalid-request",
                "Invalid request",
                "The request is invalid",
                request);

        return problemResponse(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> methodNotAllowed(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        ProblemDetail problem = newProblem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "/errors/method-not-allowed",
                "Method not allowed",
                "The requested HTTP method is not supported for this resource",
                request);

        return problemResponse(problem);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> unsupportedMediaType(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request) {

        ProblemDetail problem = newProblem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "/errors/unsupported-media-type",
                "Unsupported media type",
                "The request content type is not supported",
                request);

        return problemResponse(problem);
    }

    // Keep this as the final handler for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(
            Exception e,
            HttpServletRequest request) {

        String requestId = MDC.get(RequestIdFilter.KEY);

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            MDC.put(RequestIdFilter.KEY, requestId);
        }

        log.error("Unexpected error requestId={}", requestId, e);

        ProblemDetail problem = newProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "/errors/internal",
                "Internal error",
                "Something went wrong on our side; quote the request id if you report it",
                request);

        problem.setProperty("requestId", requestId);

        return problemResponse(problem);
    }

    private ProblemDetail newProblem(
            HttpStatus status,
            String type,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }

    private ResponseEntity<ProblemDetail> problemResponse(
            ProblemDetail problem) {

        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
