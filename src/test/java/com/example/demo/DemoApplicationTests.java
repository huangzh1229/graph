package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.*;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() throws Exception {
        ArrayList<Graph.VertexWithDis> adjList = new ArrayList<>();
        adjList.add(new Graph.VertexWithDis(1, 10));
        adjList.add(new Graph.VertexWithDis(2, 10));
        adjList.add(new Graph.VertexWithDis(1, 10));
        adjList.stream().distinct().forEach(vertexWithDis -> System.out.println(vertexWithDis.v + " " + vertexWithDis.dis));
    }
}
