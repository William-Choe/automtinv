package com.neu.mtinv.filter;

import com.neu.mtinv.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Response handleAllExceptions(Exception ex) {
        log.error("error occurs: ", ex);
        return Response.isFail().msg(ex);
    }
}
