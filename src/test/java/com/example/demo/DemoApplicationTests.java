package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() throws Exception {
        String realPath = ResourceUtils.getURL("classpath:").getPath() + ("/static/index/graph1.txt");
        File file = new File(realPath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(1);
        writer.newLine();
        writer.write(2);
        writer.close();
    }

}
