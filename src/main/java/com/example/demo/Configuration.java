package com.example.demo;

import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author huangzhuo
 * @date 2023/6/7 10:19
 **/
@org.springframework.context.annotation.Configuration
public class Configuration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
registry.addViewController("/").setViewName("redirect:/main.html");
    }
}
