package com.aloy.coreapp.exception;


import com.aloy.coreapp.dto.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
@Slf4j
public class CoreGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {CoreServiceException.class})
    protected Response handleCoreException(RuntimeException ex, WebRequest request, HttpServletResponse response) {
        CoreServiceException coreServiceException = (CoreServiceException) ex;
        response.setStatus(400);
        return new Response(false, coreServiceException.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    protected Response handleAnyException(RuntimeException ex, WebRequest request, HttpServletResponse response) {
        log.error(ExceptionUtils.getStackTrace(ex));
        response.setStatus(500);
        return new Response(false, "Some error occurred");
    }
}
