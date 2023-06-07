package com.example.demo;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangzhuo
 * @date 2023/6/5 11:31
 **/
@RestControllerAdvice
public class ExceptionHandlerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerConfig.class);

    @ExceptionHandler(SizeLimitExceededException.class)
    public Map<String, String> fileSizeExceed(SizeLimitExceededException e) {
        logger.info("文件超过限制-----" + e.toString());
        HashMap<String, String> re = new HashMap<>();
        re.put("code", "500");
        re.put("msg", "文件超过10MB");
        return re;
    }

    @ExceptionHandler(FileUploadException.class)
    public Map<String, String> fileException(FileUploadException e) {
        logger.info("上传错误：" + e.getMessage());
        HashMap<String, String> re = new HashMap<>();
        re.put("code", "500");
        re.put("msg", e.getMessage());
        return re;
    }
}
