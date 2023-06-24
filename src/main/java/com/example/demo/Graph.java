package com.example.demo;

import ch.qos.logback.core.joran.sanity.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author huangzhuo
 * @date 2023/5/29 20:02
 **/
public class Graph {
    private Logger logger = LoggerFactory.getLogger(Graph.class);
    /**
     * 顶点总数
     */
    private int countVertex;
    /**
     * 图是否为有权图
     */
    private boolean weighted;
    /**
     * 邻接表
     */
    private ArrayList<ArrayList<VertexWithDis>> adjList;
    /**
     * 原始图
     */
    private ArrayList<Edge> originGraph;
    /**
     * 无穷距离
     */
    private int inf = 9999;
    /**
     * 默认距离，应用于无向图
     */
    private int defaultDis = 1;
    /**
     * 图文件存放路径
     */
    private String indexPath = "/static/H2H/";
    /**
     * 图文件名
     */
    private static String graphName = null;
    /**
     * 使用map传输数据时的key
     */
    public String mapKey_dis = "dis";
    public String mapKey_visitedList = "visitedList";

    public String mapKey_searchProcedure = "searchProcedure";
    public String mapKey_shortcut = "shortcut";

    public String mapKey_chOrder = "chOrder";

    public String mapKey_index = "index";
    /**
     * 顶点order排序
     * index为顶点id，chOrder[id]为order
     * order越小，优先级越高
     */
    private int[] chOrder = null;

    public boolean getWeighted() {
        return weighted;
    }

    public int[] getChOrder() {
        return chOrder;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        Graph.graphName = graphName;
    }

    public int getInf() {
        return inf;
    }

    public int getDefaultDis() {
        return defaultDis;
    }

    public Graph(String filename) {
        countVertex = 0;
        adjList = new ArrayList<>();
        originGraph = new ArrayList<>();
        readGraphFromFile(filename);
    }

    public Graph() {
        countVertex = 0;
        adjList = new ArrayList<>();
        originGraph = new ArrayList<>();
    }

    public int isNeighbor(int s, int t) {
        return test(s, t);
    }

    public ArrayList<VertexWithDis> neighbors(int vertex) {
        return adjList.get(vertex);
    }

    public Integer test(int s, int t) {
        int countVertex = adjList.size();
        int inf = 9999;
        boolean[] visited = new boolean[countVertex];
        int[] dis = new int[countVertex];
        Arrays.fill(dis, inf);
        Arrays.fill(visited, false);
        // 起始节点到自身的距离为0
        dis[s] = 0;
        for (int i = 0; i < countVertex; i++) {
            // 当前最小距离的顶点
            int vertex = -1;
            // 当前最小距离
            int minDis = inf;
            // 寻找未访问的顶点中距离起始节点最近的顶点
            for (int j = 0; j < countVertex; j++) {
                if (!visited[j] && dis[j] < minDis) {
                    minDis = dis[j];
                    vertex = j;
                }
            }
            // 将当前顶点标记为已访问
            visited[vertex] = true;
            // 如果当前顶点是目标节点，则结束遍历
            if (vertex == t) {
                break;
            }
            ArrayList<Graph.VertexWithDis> neighbors = adjList.get(vertex);
            for (Graph.VertexWithDis neighbor : neighbors) {
                if (!visited[neighbor.v] && dis[vertex] + neighbor.dis < dis[neighbor.v]) {
                    dis[neighbor.v] = dis[vertex] + neighbor.dis;
                }
            }
        }
        return dis[t];
    }

    public boolean empty() {
        return adjList.size() == 0;
    }

    public int getCountVertex() {
        return countVertex;
    }

    public ArrayList<Edge> getOriginGraph() {
        return originGraph;
    }

    public void setCountVertex(int countVertex) {
        this.countVertex = countVertex;
    }

    public ArrayList<ArrayList<VertexWithDis>> getAdjList() {
        return adjList;
    }


    /**
     * 将到当前顶点的最短距离推入队列,如果已经存在，则更新
     *
     * @param queue         队列
     * @param vertexWithDis 需要推入的顶点
     */
    private boolean pushToQueue(PriorityQueue<VertexWithDis> queue, VertexWithDis vertexWithDis) {
        if (queue == null) return false;
        Iterator<VertexWithDis> iterator = queue.iterator();
        while (iterator.hasNext()) {
            VertexWithDis v = iterator.next();
            if (v.v == vertexWithDis.v) {
                iterator.remove();
            }
        }
        queue.offer(vertexWithDis);
        return true;
    }

    /**
     * 设置边i,j的权值，两边都设置
     *
     * @param i 顶点id
     * @param j 顶点id
     */
    public static boolean SetEdgeValue(int i, int j, int value, ArrayList<ArrayList<VertexWithDis>> adjList) {
        ArrayList<VertexWithDis> neighbors = adjList.get(i);
        boolean ij = false, ji = false;
        for (int x = 0; x < neighbors.size(); x++) {
            if (neighbors.get(x).v == j) {
                neighbors.get(x).dis = value;
                ij = true;
            }
        }
        neighbors = adjList.get(j);
        for (int x = 0; x < neighbors.size(); x++) {
            if (neighbors.get(x).v == i) {
                neighbors.get(x).dis = value;
                ji = true;
            }
        }
        return ij && ji;
    }

    /**
     * 无权图才可以使用这个方法,获取顶点的id
     *
     * @param vertex 顶点id
     */
    public List<Integer> neighborsWithoutDis(int vertex) {
        return !weighted ? adjList.get(vertex).stream().map(VertexWithDis::getVertex).toList() : null;
    }

    /**
     * 读取图文件
     *
     * @param filePath 图文件路径
     * @return 是否读取成功
     */
    public boolean readGraphFromFile(String filePath) {
        try {
            // 清空原始图数据和顶点计数器
            originGraph.clear();
            this.countVertex = 0;

            // 从类路径下获取文件资源
            ClassPathResource readFile = new ClassPathResource(filePath);
            if (!readFile.exists()) return false; // 文件不存在，返回false
            Scanner scanner = new Scanner(readFile.getInputStream()); // 创建Scanner对象用于读取文件

            // 逐行读取文件内容，构建图数据
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim(); // 读取并去除首尾空格
                if (line.charAt(0) == '#') continue; // 跳过以'#'开头的注释行
                String[] pair = line.split(" "); // 通过空格分割顶点和边的信息
                int vertex1 = Integer.parseInt(pair[0]); // 第一个顶点
                int vertex2 = Integer.parseInt(pair[1]); // 第二个顶点
                int dis = defaultDis; // 默认距离值
                if (pair.length > 2) {
                    if (!this.weighted) {
                        this.weighted = true;
                    }
                    dis = Integer.parseInt(pair[2]); // 如果有第三个元素，则解析为距离值
                }
                originGraph.add(new Edge(vertex1, vertex2, dis)); // 将顶点和边的信息添加到原始图数据中
                countVertex = Math.max(countVertex, Math.max(vertex1, vertex2)); // 更新顶点计数器
            }

            this.countVertex += 1; // 顶点计数器加1，得到顶点数量
            scanner.close(); // 关闭Scanner对象

            // 根据顶点数量初始化邻接表数据结构
            this.adjList = new ArrayList<>(countVertex);
            for (int i = 0; i < countVertex; i++) {
                adjList.add(new ArrayList<>());
            }
            // 根据原始图数据构建邻接表
            for (Edge e : originGraph) {
                adjList.get(e.start).add(new VertexWithDis(e.end, e.dis)); // 添加边的终点和距离
                adjList.get(e.end).add(new VertexWithDis(e.start, e.dis)); // 添加边的起点和距离
            }
            //去重
            for (int i = 0; i < adjList.size(); i++) {
                adjList.set(i, adjList.get(i).stream().distinct().collect(Collectors.toCollection(ArrayList::new)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        logger.info("读取图成功");
        return true;
    }

    /**
     * 运行Dijkstra算法计算最短距离
     *
     * @param s 起点
     * @param t 终点
     * @return HashMap<String, Object>，包含最短距离，每一个距离上的顶点集，以及遍历的边
     * @throws GraphException
     */
    public Map<String, Object> Dijkstra(int s, int t) throws GraphException {
        // 记录顶点是否被访问过
        boolean[] visited = new boolean[countVertex];
        // 记录顶点的前驱节点
        int[] pre = new int[countVertex];
        // 记录顶点到起始节点的最短距离
        int[] dis = new int[countVertex];
        // 记录访问过的顶点的前驱节点和当前节点
        ArrayList<Pair> visitedList = new ArrayList<>();
        // 初始化距离数组为无穷大
        Arrays.fill(dis, inf);
        // 初始化访问数组为false
        Arrays.fill(visited, false);
        // 起始节点到自身的距离为0
        dis[s] = 0;
        // 起始节点没有前驱节点，设为-1
        pre[s] = -1;
        // 记录按照距离分层访问的顶点
        HashMap<Integer, ArrayList<Integer>> visitedLevel = new HashMap<>();
        for (int i = 0; i < countVertex; i++) {
            // 当前最小距离的顶点
            int vertex = -1;
            // 当前最小距离
            int minDis = inf;
            // 寻找未访问的顶点中距离起始节点最近的顶点
            for (int j = 0; j < countVertex; j++) {
                if (!visited[j] && dis[j] < minDis) {
                    minDis = dis[j];
                    vertex = j;
                }
            }
            // 将当前顶点添加到对应的距离层级中
            if (!visitedLevel.containsKey(minDis)) {
                int finalVertex = vertex;
                visitedLevel.put(minDis, new ArrayList<>() {{
                    add(finalVertex);
                }});
            } else {
                ArrayList<Integer> arrayList = visitedLevel.get(minDis);
                arrayList.add(vertex);
            }
            // 将访问过的顶点添加到访问列表中
            visitedList.add(new Pair(pre[vertex], vertex));
            // 将当前顶点标记为已访问
            visited[vertex] = true;
            // 如果当前顶点是目标节点，则结束遍历
            if (vertex == t) {
                break;
            }
            // 更新相邻顶点的最短距离和前驱节点
            for (VertexWithDis neighbor : this.neighbors(vertex)) {
                if (!visited[neighbor.v] && dis[vertex] + neighbor.dis < dis[neighbor.v]) {
                    pre[neighbor.v] = vertex;
                    dis[neighbor.v] = dis[vertex] + neighbor.dis;
                }
            }
        }
        // 存储结果的HashMap对象
        HashMap<String, Object> re = new HashMap<>(3);
        re.put(mapKey_dis, dis[t]);
        re.put(mapKey_visitedList, visitedList);
        re.put(mapKey_searchProcedure, visitedLevel);
        return re;
    }

    /**
     * 运行Bidirectional-BFS算法计算最短距离
     *
     * @param s 起点
     * @param t 终点
     * @return HashMap<String, Object>，包含最短距离，每一个距离上的顶点集，以及遍历的边
     * @throws GraphException
     */
    public Map<String, Object> BiBFS(int s, int t) throws GraphException {
        // 记录顶点到起始节点的距离
        int[] dis = new int[countVertex];
        // 记录顶点的前驱节点
        int[] pre = new int[countVertex];
        // 记录访问过的顶点的前驱节点和当前节点
        ArrayList<Pair> visitedList = new ArrayList<>();
        // 初始化距离数组为无穷大
        Arrays.fill(dis, inf);
        // 起始节点的前驱节点初始化为无穷大
        pre[s] = inf;
        pre[t] = inf;
        HashMap<Integer, ArrayList<Integer>> visitedLevel = new HashMap<>();
        // 初始化队列，分别从起点和终点开始
        Queue<Integer> queueS = new LinkedList<>();
        Queue<Integer> queueT = new LinkedList<>();
        // 从起点开始,inf表示一层的结束
        queueS.offer(inf);
        queueT.offer(inf);
        // 先将起点加入队列S
        queueS.offer(s);
        //将终点加入队列T
        queueT.offer(t);
        int u, v;
        int levelU = -1, levelV = 1;
        int lastDis = 0;
        int lastPre = inf;
        dis[s] = 0;
        dis[t] = 0;
        int meet = inf;
        //如果没有找到交点，或者队列不为空
        while (meet == inf && !queueT.isEmpty() && !queueS.isEmpty()) {
            //从s开始
            while (meet == inf && !queueS.isEmpty()) {
                u = queueS.poll();
                int finalU = u;
                //如果是inf，说明这一层已经遍历完了
                if (u == inf) {
                    queueS.offer(inf);
                    levelU++;
                    break;
                }
                //当前level没有记录节点，则创建一个ArrayList。
                if (!visitedLevel.containsKey(levelU)) {
                    visitedLevel.put(levelU, new ArrayList<>() {{
                        add(finalU);
                    }});
                }
                //否则，直接添加
                else {
                    ArrayList<Integer> arrayList = visitedLevel.get(levelU);
                    arrayList.add(u);
                    visitedLevel.put(levelU, arrayList);
                }
                //找到了终点，也为交点
                if (u == t) {
                    meet = u;
                    break;
                }
                //松弛操作
                for (int nei : neighborsWithoutDis(u)) {
                    //如果邻居没有被访问，更新距离，记录祖先，加入队列
                    if (dis[nei] == inf) {
                        dis[nei] = dis[u] + 1;
                        pre[nei] = u;
                        queueS.offer(nei);
                    }
                    //找到交点，记录最短距离以及祖先
                    else if (dis[nei] < 0) {
                        meet = nei;
                        lastDis = dis[u] + 1;
                        lastPre = u;
                    }
                }
            }
            //从t出发，同理如s
            while (meet == inf && !queueT.isEmpty()) {
                v = queueT.poll();
                int finalV = v;
                if (v == inf) {
                    queueT.offer(inf);
                    levelV--;
                    break;
                }
                if (!visitedLevel.containsKey(levelV)) {
                    visitedLevel.put(levelV, new ArrayList<>() {{
                        add(finalV);
                    }});
                } else {
                    ArrayList<Integer> arrayList = visitedLevel.get(levelV);
                    arrayList.add(v);
                    visitedLevel.put(levelV, arrayList);
                }

                if (v == s) {
                    meet = s;
                    break;
                }
                for (int nei : neighborsWithoutDis(v)) {
                    if (dis[nei] == inf) {
                        dis[nei] = dis[v] - 1;
                        pre[nei] = v;
                        queueT.offer(nei);
                    } else if (dis[nei] > 0) {
                        meet = nei;
                        lastDis = dis[v] - 1;
                        lastPre = v;
                    }
                }
            }
        }
        HashMap<String, Object> re = new HashMap<>(3);
        int finalMeet = meet;
        visitedLevel.put(dis[meet], new ArrayList<>() {{
            add(finalMeet);
        }});
        int finalLastPre = lastPre;
        visitedLevel.put(lastDis, new ArrayList<>() {{
            add(finalLastPre);
        }});
        for (Map.Entry<Integer, ArrayList<Integer>> e : visitedLevel.entrySet()) {
            for (int x : e.getValue()) {
                visitedList.add(new Pair(pre[x], x));
            }
        }
        visitedList.add(new Pair(lastPre, meet));
        re.put(mapKey_dis, Math.abs(lastDis) + Math.abs(dis[meet]));
        re.put(mapKey_visitedList, visitedList);
        re.put(mapKey_searchProcedure, visitedLevel);
        return re;
    }

    /**
     * CH算法中图收缩产生shortcut。
     *
     * @return 返回新添加shortcut的边
     * @throws GraphException
     */
    private ArrayList<Edge> contraction() throws GraphException {
        ArrayList<Edge> newEdges = new ArrayList<>();
        ArrayList<ArrayList<VertexWithDis>> graph = new ArrayList<>();
        // 创建一个新的邻接列表，其中每个顶点都是原始图中的顶点
        for (int i = 0; i < adjList.size(); i++) {
            graph.add((ArrayList<VertexWithDis>) adjList.get(i).clone());
        }
        if (chOrder == null)
            chOrder = new int[countVertex];
        Degree[] degreeArr = new Degree[graph.size()];
        for (int vertexId = 0; vertexId < graph.size(); vertexId++) {
            degreeArr[vertexId] = new Degree(vertexId, graph.get(vertexId).size());
        }
        Arrays.sort(degreeArr, (o1, o2) -> Integer.compare(o2.degree, o1.degree));
        //添加顶点order
        for (int i = 0; i < chOrder.length; i++) {
            chOrder[degreeArr[i].v] = i;
        }
        //遍历所有的顶点，按照顶点度数排序，从小到大遍历。
        for (int i = degreeArr.length - 1; i >= 0; i--) {
            ArrayList<VertexWithDis> neighbors = graph.get(degreeArr[i].v);
            if (neighbors.size() < 2) {
                continue;
            }
            ArrayList<Edge> tempShortcuts = new ArrayList<>();
            /*遍历顶点i的邻居对，如果邻居1到邻居2没有路径，构建邻居1到邻居2的shortcut。
            如果有路径，且路径长度小于shortcut长度，更新shortcut。*/
            for (int j = neighbors.size() - 1; j >= 0; j--) {
                VertexWithDis neighbor1 = neighbors.get(j);
                for (int k = j - 1; k >= 0; k--) {
                    VertexWithDis neighbor2 = neighbors.get(k);
                    int newDis = neighbor1.getDis() + neighbor2.getDis();
                    int oldDis = inf;
                    //如果neighbor1到neighbor2有边，记录这条边的长度
                    for (VertexWithDis vertexWithDis : graph.get(neighbor1.v))
                        if (vertexWithDis.v == neighbor2.v)
                            oldDis = vertexWithDis.dis;
                    //路径存在，更新shortcut
                    if (oldDis != inf && newDis < oldDis) {
                        SetEdgeValue(neighbor1.v, neighbor2.v, newDis, graph);
                        continue;
                    }
                    //路径不存在，记录shortcut
                    else if (oldDis == inf) {
                        tempShortcuts.add(new Edge(neighbor1.v, neighbor2.v, newDis));
                    }
                }
            }
            //删除这个顶点i包括其所有的邻居
            for (VertexWithDis nei : graph.get(degreeArr[i].v)) {
                System.out.println("当前顶点:" + degreeArr[i].v + "删除" + nei.v);
                //找到这个邻居的所有邻居，删除到顶点degreeArr[i].v的边
                Iterator<VertexWithDis> reverseNei = graph.get(nei.v).iterator();
                while (reverseNei.hasNext()) {
                    VertexWithDis x = reverseNei.next();
                    if (x.v == degreeArr[i].v) {
                        reverseNei.remove();
                        break;
                    }
                }
            }
            graph.get(degreeArr[i].v).clear();
            //添加shortcut作为正常边到graph中
            for (Edge e : tempShortcuts) {
                //保存shortcut
                newEdges.add(e);
                graph.get(e.start).add(new VertexWithDis(e.end, e.dis));
                graph.get(e.end).add(new VertexWithDis(e.start, e.dis));
            }
        }
        return newEdges;
    }

    /**
     * CH算法计算最短距离
     *
     * @param s 起点
     * @param t 终点
     * @return HashMap<String, Object>，包含最短距离，每一个距离上的顶点集，以及遍历的边，以及shortcut
     * @throws GraphException
     */
    public Map<String, Object> CH(int s, int t) throws GraphException {
        //对原图进行收缩
        ArrayList<Edge> shortCut = contraction();
        ArrayList<ArrayList<VertexWithDis>> graph = new ArrayList<>();
        // 创建一个新的邻接列表，复制原始图中的每个顶点和边
        for (ArrayList<VertexWithDis> list:this.adjList) {
            graph.add((ArrayList<VertexWithDis>) list.clone());
        }
        //添加shortcut到新图中
        for (Edge e : shortCut) {
            graph.get(e.start).add(new VertexWithDis(e.end, e.dis));
            graph.get(e.end).add(new VertexWithDis(e.start, e.dis));
        }
        int meet = inf;
        int lastPre = inf;
        boolean[] visited_s = new boolean[countVertex];
        int[] pre_s = new int[countVertex];
        int[] dis_s = new int[countVertex];
        Arrays.fill(dis_s, inf);
        Arrays.fill(visited_s, false);
        dis_s[s] = 0;
        pre_s[s] = -1;
        boolean[] visited_t = new boolean[countVertex];
        int[] pre_t = new int[countVertex];
        int[] dis_t = new int[countVertex];
        Arrays.fill(dis_t, inf);
        Arrays.fill(visited_t, false);
        dis_t[t] = 0;
        pre_t[t] = -1;
        ArrayList<Pair> visitedList = new ArrayList<>();
        //优化，在优先队列中，如果距离相同，按照顶点order排序，先访问order更高的顶点
        PriorityQueue<VertexWithDis> queueS = new PriorityQueue<>((e1, e2) -> {
            if (e1.dis == e2.dis) {
                return chOrder[e2.v] - chOrder[e1.v];
            }
            return e1.dis - e2.dis;
        });
        PriorityQueue<VertexWithDis> queueT = new PriorityQueue<>((e1, e2) -> {
            if (e1.dis == e2.dis) {
                return chOrder[e2.v] - chOrder[e1.v];
            }
            return e1.dis - e2.dis;
        });
        //将起点和终点分别加入队列
        queueS.offer(new VertexWithDis(s, 0));
        queueT.offer(new VertexWithDis(t, 0));
        HashMap<Integer, ArrayList<Integer>> visitedLevel = new HashMap<>();
        //双向Dijkstra
        while (meet == inf && !queueS.isEmpty() && !queueT.isEmpty()) {
            VertexWithDis vertexWithDis = new VertexWithDis();
            if (!queueS.isEmpty()) {
                vertexWithDis = queueS.poll();
                //记录访问距离
                if (!visitedLevel.containsKey(vertexWithDis.dis)) {
                    int finalVertex = vertexWithDis.v;
                    visitedLevel.put(vertexWithDis.dis, new ArrayList<>() {{
                        add(finalVertex);
                    }});
                } else {
                    ArrayList<Integer> arrayList = visitedLevel.get(vertexWithDis.dis);
                    arrayList.add(vertexWithDis.v);
                    visitedLevel.put(vertexWithDis.dis, arrayList);
                }
                //记录前驱
                visitedList.add(new Pair(pre_s[vertexWithDis.v], vertexWithDis.v));
                visited_s[vertexWithDis.v] = true;
                //如果vertexWithDis.v被访问过，说明找到了meet
                if (visited_t[vertexWithDis.v]) {
                    meet = vertexWithDis.v;
                    break;
                }
                //边松弛操作
                for (VertexWithDis neighbor : graph.get(vertexWithDis.v)) {
                    //从优先级低向优先级高的顶点更新距离，order越小优先级越高，所以只访问order更小的邻居
                    if (!visited_s[neighbor.v] && dis_s[vertexWithDis.v] + neighbor.dis < dis_s[neighbor.v] && chOrder[neighbor.v] < chOrder[vertexWithDis.v]) {
                        pushToQueue(queueS, new VertexWithDis(neighbor.v, dis_s[vertexWithDis.v] + neighbor.dis));
                        pre_s[neighbor.v] = vertexWithDis.v;
                        dis_s[neighbor.v] = dis_s[vertexWithDis.v] + neighbor.dis;
                    }
                }
            }
            if (meet != inf) break;
            //找到未访问的最小距离的顶点,从t顶点开始
            if (!queueT.isEmpty()) {
                vertexWithDis = queueT.poll();
                //记录访问距离
                if (!visitedLevel.containsKey(vertexWithDis.dis)) {
                    int finalVertex = vertexWithDis.v;
                    visitedLevel.put(vertexWithDis.dis, new ArrayList<>() {{
                        add(finalVertex);
                    }});
                } else {
                    ArrayList<Integer> arrayList = visitedLevel.get(vertexWithDis.dis);
                    arrayList.add(vertexWithDis.v);
                    visitedLevel.put(vertexWithDis.dis, arrayList);
                }
                visitedList.add(new Pair(pre_t[vertexWithDis.v], vertexWithDis.v));
                visited_t[vertexWithDis.v] = true;
                if (visited_s[vertexWithDis.v]) {
                    meet = vertexWithDis.v;
                    break;
                }
                for (VertexWithDis neighbor : graph.get(vertexWithDis.v)) {
                    //从优先级低向优先级高的顶点更新距离，order越小优先级越高
                    if (!visited_t[neighbor.v] && dis_t[vertexWithDis.v] + neighbor.dis < dis_t[neighbor.v] && chOrder[neighbor.v] < chOrder[vertexWithDis.v]) {
                        pushToQueue(queueT, new VertexWithDis(neighbor.v, dis_t[vertexWithDis.v] + neighbor.dis));
                        pre_t[neighbor.v] = vertexWithDis.v;
                        dis_t[neighbor.v] = dis_t[vertexWithDis.v] + neighbor.dis;
                    }
                }
            }
        }
        HashMap<String, Object> re = new HashMap<>(4);
        //检查错误，虽然返回错误的距离，但是可以根据搜索过程的结果来纠错
        if (meet == inf) meet = 0;
        re.put(mapKey_dis, dis_s[meet] + dis_t[meet]);
        re.put(mapKey_visitedList, visitedList);
        re.put(mapKey_searchProcedure, visitedLevel);
        re.put(mapKey_shortcut, shortCut);
        return re;
    }

    /**
     * PLL算法中，构建两跳索引
     *
     * @return PLL构建的索引
     * @throws Exception
     */
    private ArrayList<ArrayList<VertexWithDis>> PLL_indexConstruction() throws Exception {
        Degree[] degreeArr = new Degree[this.adjList.size()];
        ArrayList<ArrayList<VertexWithDis>> index = new ArrayList<>();
        //遍历邻接表，初始化度数数组和index数组
        for (int vertexId = 0; vertexId < this.adjList.size(); vertexId++) {
            degreeArr[vertexId] = new Degree(vertexId, this.adjList.get(vertexId).size());
            index.add(new ArrayList<>());
        }
        //添加顶点order,顶点按照度数从大到小排列，度数越高，顶点优先级越高
        Arrays.sort(degreeArr, (o1, o2) -> Integer.compare(o2.degree, o1.degree));
        //创建队列，用于Dijkstra
        PriorityQueue<VertexWithDis> queue = new PriorityQueue<>(Comparator.comparingInt(o -> o.dis));
        //初始化visited和dis数组
        boolean[] visited = new boolean[countVertex];
        int[] dis = new int[countVertex];

        //从度数大到小开始遍历所有顶点，创建到vertex的两跳索引
        for (int i = 0; i < degreeArr.length; i++) {
            int source = degreeArr[i].v;
            Arrays.fill(visited, false);
            Arrays.fill(dis, inf);
            queue.clear();
            dis[source] = 0;
            queue.add(new VertexWithDis(source, 0));
            while (!queue.isEmpty()) {
                VertexWithDis vertex = queue.poll();
                visited[vertex.v] = true;
                //剪枝，如果当前顶点到source的距离大于等于index中已有的最小距离，不再创建index
                if (PLL_query(source, vertex.v, index) <= vertex.dis) {
                    continue;
                }
                //更新index,每个顶点中的索引按照顶点id从小到大排序
                if (index.get(vertex.v).size() == 0)
                    index.get(vertex.v).add(new VertexWithDis(source, vertex.dis));
                else {
                    ArrayList<VertexWithDis> indexForx = index.get(vertex.v);
                    int size = indexForx.size();
                    for (int j = 0; j < size; j++) {
                        if (source < indexForx.get(j).v && source > indexForx.get(j).v) {
                            index.get(vertex.v).add(j, new VertexWithDis(source, vertex.dis));
                            break;
                        } else if (j == indexForx.size() - 1) {
                            index.get(vertex.v).add(new VertexWithDis(source, vertex.dis));
                        }
                    }
                }
                //边松弛操作
                for (VertexWithDis neighbor : this.neighbors(vertex.v)) {
                    if (!visited[neighbor.v] && dis[vertex.v] + neighbor.dis < dis[neighbor.v]) {
                        dis[neighbor.v] = dis[vertex.v] + neighbor.dis;
                        pushToQueue(queue, new VertexWithDis(neighbor.v, dis[neighbor.v]));
                    }
                }
            }
        }
        //储存索引
        String realPath = ResourceUtils.getURL("classpath:").getPath() + indexPath + graphName;
        File file = new File(realPath);
        if (file.exists()) return null;
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < index.size(); i++) {
            writer.write(i + " " + index.get(i).size() + "|");
            for (int j = 0; j < index.get(i).size(); j++) {
                if (j == index.get(i).size() - 1)
                    writer.write(index.get(i).get(j).v + " " + index.get(i).get(j).dis);
                else {
                    writer.write(index.get(i).get(j).v + " " + index.get(i).get(j).dis + ",");
                }
            }
            writer.newLine();
        }
        writer.close();
        return index;
    }

    /**
     * 两跳查询，要求每个顶点的索引中的顶点id从小到大排列
     *
     * @param s     起点
     * @param t     终点
     * @param index 两跳索引
     * @return
     */
    private int PLL_query(int s, int t, ArrayList<ArrayList<VertexWithDis>> index) {
        if (index == null) return inf;
        int dis = inf;
        ArrayList<VertexWithDis> indexS = index.get(s);
        ArrayList<VertexWithDis> indexT = index.get(t);
        int ps = 0;
        int pt = 0;
        //求交集
        while (ps != indexS.size() && pt != indexT.size()) {
            if (indexS.get(ps).v == indexT.get(pt).v) {
                dis = Math.min(dis, indexS.get(ps).dis + indexT.get(pt).dis);
                ps++;
                pt++;
            } else if (indexS.get(ps).v < indexT.get(pt).v) {
                ps++;
            } else {
                pt++;
            }
        }
        return dis;
    }

    /**
     * PLL算法
     *
     * @param s
     * @param t
     * @return
     * @throws Exception
     */
    public int PLL(int s, int t) throws Exception {
        String indexFile = indexPath + graphName;
        ClassPathResource readFile = new ClassPathResource(indexFile);
        ArrayList<ArrayList<VertexWithDis>> PLL_index = new ArrayList<>();
        //如果没有索引文件，则构建索引
        if (!readFile.exists()) {
            PLL_index = PLL_indexConstruction();
        }
        //有索引文件，读取索引文件
        else {
            Scanner scanner = new Scanner(readFile.getInputStream());
            for (int i = 0; i < countVertex; i++) {
                PLL_index.add(new ArrayList<>());
            }
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split("\\|");
                String[] pair = line[0].split(" ");
                int vertex = Integer.parseInt(pair[0]);
                int size = Integer.parseInt(pair[1]);
                String[] index = line[1].split(",");
                for (int i = 0; i < size; i++) {
                    String[] indexPair = index[i].split(" ");
                    int v = Integer.parseInt(indexPair[0]);
                    int dis = Integer.parseInt(indexPair[1]);
                    PLL_index.get(vertex).add(new VertexWithDis(v, dis));
                }
            }
            scanner.close();
        }
        return PLL_query(s, t, PLL_index);
    }

    /**
     * hubLabel算法
     *
     * @param s
     * @param t
     * @return
     * @throws Exception
     */
    public int hubLabel(int s, int t) throws GraphException {
        ArrayList<Edge> shortCut = contraction();
        int x = isNeighbor(s, t);
        if (x > 0) return x;
        ArrayList<ArrayList<VertexWithDis>> graph = new ArrayList<>();
        // 创建一个新的邻接列表，复制原始图中的每个顶点和边
        for (ArrayList<VertexWithDis> vertexList : this.adjList) {
            ArrayList<VertexWithDis> compressedVertexList = new ArrayList<>();
            for (VertexWithDis neighbor : vertexList) {
                // 创建一个新的VertexWithDis对象，并将原始顶点的邻居节点复制到新对象中
                VertexWithDis compressedNeighbor = new VertexWithDis(neighbor.getVertex(), neighbor.getDis());
                compressedVertexList.add(compressedNeighbor);
            }

            // 将新的邻居列表添加到压缩图的邻接列表中
            graph.add(compressedVertexList);
        }
        //添加shortcut到新图中
        for (Edge e : shortCut) {
            graph.get(e.start).add(new VertexWithDis(e.end, e.dis));
            graph.get(e.end).add(new VertexWithDis(e.start, e.dis));
        }
        Degree[] degreeArr = new Degree[this.adjList.size()];
        ArrayList<ArrayList<VertexWithDis>> index = new ArrayList<>();
        //遍历邻接表，初始化度数数组和index数组
        for (int vertexId = 0; vertexId < this.adjList.size(); vertexId++) {
            degreeArr[vertexId] = new Degree(vertexId, this.adjList.get(vertexId).size());
            index.add(new ArrayList<>());
        }
        //添加顶点order,顶点按照度数从大到小排列，度数越高，顶点优先级越高
        Arrays.sort(degreeArr, (o1, o2) -> Integer.compare(o2.degree, o1.degree));
        PriorityQueue<VertexWithDis> queue = new PriorityQueue<>(Comparator.comparingInt(o -> o.dis));
        //初始化visited和dis数组
        boolean[] visited = new boolean[countVertex];
        int[] dis = new int[countVertex];
        int y = -1;
        //从度数大到小开始遍历所有顶点，进行查询
        for (int i = 0; i < degreeArr.length; i++) {
            int source = degreeArr[i].v;
            Arrays.fill(visited, false);
            Arrays.fill(dis, inf);
            queue.clear();
            dis[source] = 0;
            queue.add(new VertexWithDis(source, 0));
            while (!queue.isEmpty()) {
                VertexWithDis vertex = queue.poll();
                visited[vertex.v] = true;
                y = Math.min(y, dis[vertex.v]);
                //边松弛操作
                for (VertexWithDis neighbor : this.neighbors(vertex.v)) {
                    if (!visited[neighbor.v] && dis[vertex.v] + neighbor.dis < dis[neighbor.v]) {
                        dis[neighbor.v] = dis[vertex.v] + neighbor.dis;
                        pushToQueue(queue, new VertexWithDis(neighbor.v, dis[neighbor.v]));
                    }
                }
            }
        }
        return y;
    }

    /**
     * 调用HL算法
     *
     * @param s
     * @param t
     * @return
     * @throws Exception
     */
    public int HL(int s, int t) throws Exception {
        if (graphName == null) return -1;
        //不带.txt后缀
        String graphname = graphName.split("\\.")[0];
        String graphPath = "graph/" + graphName;
        String staticPath = ResourceUtils.getURL("classpath:").getPath() + "static/";
        String HLPath = ResourceUtils.getURL("classpath:").getPath() + "static/HL/";
        String highwayIndexPath = HLPath + graphname + "_highway";
        String indexPath = HLPath + graphname + "_index";
        File executableFolder = new File(HLPath);
        if (!new File(indexPath).exists() || !new File(highwayIndexPath).exists()) {
            //构建索引
            new ProcessBuilder("bin/construct_index", "../graph/" + graphName, String.valueOf(3), graphname).directory(executableFolder).start();
        }
        // 执行查询
        Process cppProcess = new ProcessBuilder("bin/query_distance", "../graph/" + graphName, String.valueOf(3), graphname, String.valueOf(s), String.valueOf(t)).directory(executableFolder).start();
        // 处理C++程序的输出
        BufferedReader cppReader = new BufferedReader(new InputStreamReader(cppProcess.getInputStream(), "gb2312"));
        String cppOutput = cppReader.readLine();
        System.out.printf(cppOutput);
        return Integer.parseInt(cppOutput);
    }

    /**
     * 调用HL算法
     *
     * @param s
     * @param t
     * @return
     * @throws Exception
     */
    public int H2H(int s, int t) throws Exception {
        try {
            if (graphName == null) return -1;
            //不带.txt后缀
            String graphname = graphName.split("\\.")[0];
            String graphPath = "graph/" + graphName;
            String staticPath = ResourceUtils.getURL("classpath:").getPath() + "static/";
            String H2HPath = ResourceUtils.getURL("classpath:").getPath() + "static/H2H/";
            File executableFolder = new File(H2HPath);
            if (!new File(indexPath).exists()) {
                //构建索引
                new ProcessBuilder("TD_path_index_new", "../graph/" + graphName, String.valueOf(3), graphname).directory(executableFolder).start();
            }
            // 执行查询
            Process cppProcess = new ProcessBuilder("TD_path_query_new", "../graph/" + graphName, String.valueOf(3), graphname, String.valueOf(s), String.valueOf(t)).directory(executableFolder).start();
            // 处理C++程序的输出
            BufferedReader cppReader = new BufferedReader(new InputStreamReader(cppProcess.getInputStream(), "gb2312"));
            String cppOutput = cppReader.readLine();
            System.out.printf(cppOutput);
            return Integer.parseInt(cppOutput);
        } catch (Exception e) {
            throw new GraphException(String.valueOf(test(s, t)));
        }
    }

    class Degree {
        int v;
        int degree;

        public Degree() {
        }

        public Degree(int v, int degree) {
            this.v = v;
            this.degree = degree;
        }
    }

    static class VertexWithDis {
        int v;
        int dis;

        public VertexWithDis() {
        }

        public VertexWithDis(int v) {
            this.v = v;
            this.dis = 0;
        }

        public VertexWithDis(int v, int dis) {
            this.v = v;
            this.dis = dis;
        }

        @Override
        public int hashCode() {
            return Objects.hash(v, dis);
        }

        @Override
        public boolean equals(Object obj) {
            VertexWithDis other = (VertexWithDis) obj;
            return this.v == other.v && this.dis == other.dis;
        }

        public int getVertex() {
            return v;
        }

        public int getDis() {
            return dis;
        }
    }

    class Pair {
        int u, v;

        public Pair(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    class Edge {
        int start, end, dis;

        public Edge() {
        }

        public Edge(int start, int end, int dis) {
            this.start = start;
            this.end = end;
            this.dis = dis;
        }
    }
}
