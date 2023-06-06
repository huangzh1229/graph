package com.example.demo;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author huangzhuo
 * @date 2023/6/5 11:31
 **/
@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(SizeLimitExceededException.class) {
    
    }
}
