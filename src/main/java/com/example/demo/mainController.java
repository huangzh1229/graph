package com.example.demo;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.ClassPathResource;

import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


/**
 * @author huangzhuo
 * @date 2023/5/29 19:56
 **/
@RestController
public class mainController {
    Graph G = new Graph();
    final private String graphFilePath = "/static/graph/";

    @GetMapping("/graph")
    String getGraph(@RequestParam String graphName) {
        StringBuffer result = new StringBuffer();
        if (!StringUtils.hasText(graphName) || !G.readGraphFromFile(graphFilePath + graphName)) {
            return null;
        }
        ArrayList<Graph.Pair> graph = G.getGraphData();
        for (int i = 0; i < graph.size(); i++) {
            result.append(graph.get(i).u + " " + graph.get(i).v + " " + G.getDefaultDis());
            if (i != graph.size() - 1) {
                result.append(',');
            }
        }
        return result.toString();
    }

    @GetMapping("/graphList")
    ArrayList<String> getGraphList() throws Exception {
        ClassPathResource readFile = new ClassPathResource(graphFilePath);
        File folder = readFile.getFile();
        File[] files = folder.listFiles();
        Assert.notNull(files, "没有图文件");
        return new ArrayList<String>(Arrays.stream(files).map(File::getName).toList());
    }

    @PostMapping("/upload")
    public HashMap<String, String> handleFileUpload(@RequestParam("graph") MultipartFile file) throws Exception {
        HashMap<String, String> result = new HashMap<>();
        result.put("code", "500");
        if (!file.isEmpty()) {
            // 处理文件上传逻辑
            // 可以使用MultipartFile的方法获取文件信息，例如文件名、大小、内容等
            String fileName = file.getOriginalFilename();
            System.out.println(fileName);
            if (StringUtils.hasText(fileName) && !fileName.contains("txt")) {
                throw new FileUploadException("必须是txt文件");
            }
            ClassPathResource readFile = new ClassPathResource(graphFilePath);
            File folder = readFile.getFile();
            File[] files = folder.listFiles();
            if (files != null) {
                for (var f : files) {
                    if (f.getName().equals(fileName)) {
                        throw new FileUploadException("文件重复");
                    }
                }
            }
            InputStream inputStream = file.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String firstLine = reader.readLine();
            reader.close();
            try {
                Integer.parseInt(firstLine);
            } catch (Exception e) {
                throw new FileUploadException("文件格式错误");
            }
            String realPath = ResourceUtils.getURL("classpath:").getPath() + graphFilePath;
            file.transferTo(new File(realPath + File.separator + fileName));
            result.put("code", "200");
            return result;
        } else {
            result.put("msg", "文件为空");
            return result;
        }

    }

    @GetMapping("/Dijkstra")
    public Map<String, Object> Dijkstra(@RequestParam("s") int s, @RequestParam("t") int t) {
        if (G.empty()) return null;
        Map<String, Object> result = G.Dijkstra(s, t);
        StringBuffer line = new StringBuffer();
        for (Graph.Pair p : (ArrayList<Graph.Pair>) result.get(G.mapKey_visitedList)) {
            if (p.u == -1 || p.v == -1) continue;
            line.append(p.u + "," + p.v + "|");
        }
        result.put(G.mapKey_visitedList, line.substring(0, line.length() - 1).toString());
        return result;
    }
}
