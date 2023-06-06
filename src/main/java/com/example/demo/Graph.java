package com.example.demo;

import ch.qos.logback.core.joran.sanity.Pair;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author huangzhuo
 * @date 2023/5/29 20:02
 **/
public class Graph {
    private int countVertex;
    private ArrayList<ArrayList<VertexWithDis>> adjList;
    private ArrayList<Pair> graphData;
    private int inf = 9999;
    private int defaultDis = 1;
    public String mapKey_dis="dis";
    public String mapKey_visitedList="visitedList";
    public int getInf() {
        return inf;
    }

    public int getDefaultDis() {
        return defaultDis;
    }

    public Graph(String filename) {
        countVertex = 0;
        adjList = new ArrayList<>();
        graphData = new ArrayList<>();
        readGraphFromFile(filename);
    }

    public Graph() {
        countVertex = 0;
        adjList = new ArrayList<>();
        graphData = new ArrayList<>();
    }

    public boolean empty() {
        return adjList.size() == 0;
    }

    public int getCountVertex() {
        return countVertex;
    }

    public ArrayList<Pair> getGraphData() {
        return graphData;
    }

    public void setCountVertex(int countVertex) {
        this.countVertex = countVertex;
    }

    public ArrayList<ArrayList<VertexWithDis>> getAdjList() {
        return adjList;
    }

    public ArrayList<VertexWithDis> neighbors(int vertex) {
        return adjList.get(vertex);
    }


    public boolean readGraphFromFile(String filePath) {
        try {
            adjList.clear();
            graphData.clear();
            ClassPathResource readFile = new ClassPathResource(filePath);
            File file = readFile.getFile();
            Scanner scanner = new Scanner(file);

            // Read the number of vertices
            if (scanner.hasNextLine()) {
                this.countVertex = Integer.parseInt(scanner.nextLine().trim());
            }
            // Initialize the adjacency list
            for (int i = 0; i < countVertex; i++) {
                adjList.add(new ArrayList<>());
            }
            // Read the connections and update the adjacency list
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] pair = line.split(" ");
                int vertex1 = Integer.parseInt(pair[0]);
                int vertex2 = Integer.parseInt(pair[1]);
                graphData.add(new Pair(vertex1, vertex2));
                adjList.get(vertex1).add(new VertexWithDis(vertex2));
                adjList.get(vertex2).add(new VertexWithDis(vertex1));
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, Object> Dijkstra(int s, int t) {
        boolean[] visited = new boolean[countVertex];
        int[] pre = new int[countVertex];
        int[] dis = new int[countVertex];
        ArrayList<Pair> visitedList = new ArrayList<>();
        Arrays.fill(dis, inf);
        Arrays.fill(visited, false);
        dis[s] = 0;
        pre[s] = -1;
        for (int i = 0; i < countVertex; i++) {
            int vertex = -1;
            int minDis = inf;
            for (int j = 0; j < countVertex; j++) {
                if (!visited[j] && dis[j] < minDis) {
                    minDis = dis[j];
                    vertex = j;
                }
            }
            visitedList.add(new Pair(pre[vertex], vertex));
            visited[vertex] = true;
            if (vertex == t) {
                break;
            }
            for (VertexWithDis neighbor : this.neighbors(vertex)) {
                if (!visited[neighbor.v] && dis[vertex] + neighbor.dis < dis[neighbor.v]) {
                    pre[neighbor.v] = vertex;
                    dis[neighbor.v] = dis[vertex] + neighbor.dis;
                }
            }
        }
        HashMap<String, Object> re = new HashMap<>(2);
        re.put(mapKey_dis, dis[t]);
        re.put(mapKey_visitedList, visitedList);
        return re;
    }

    class VertexWithDis {
        int v;
        int dis;

        public VertexWithDis(int v) {
            this.v = v;
            this.dis = defaultDis;
        }

        public VertexWithDis(int v, int dis) {
            this.v = v;
            this.dis = dis;
        }
    }

    class Pair {
        int u, v;

        public Pair(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }
}
