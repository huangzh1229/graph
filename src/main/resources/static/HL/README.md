算法启动需要的终端命令：

make

bin/construct_index <数据集文件> <landmark数量> <输出的index文件名>
bin/query_distance <数据集文件> <landmark数量> <输出的index文件名> 起点 终点


eg：
make
bin/construct_index sample/graph.txt 3 graph 
bin/query_distance sample/graph.txt 3 graph 0 1
