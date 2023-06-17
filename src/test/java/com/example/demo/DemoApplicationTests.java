package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
       ArrayList<ArrayList<Graph.VertexWithDis>> adjList = new ArrayList<>(10);
        System.out.println(adjList.size());
    }

}
