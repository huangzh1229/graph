#include<cstdio>
#include<cstring>
#include<iostream>
#include<cstdlib>
#include <sys/time.h>
#include<vector>
#include<algorithm>
#include <xmmintrin.h>
#include<cmath>
using namespace std;

double GetTime(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec + tv.tv_usec * 1e-6;
}

const int infinity = 999999999;
const int SIZEOFINT = 4;

int *toRMQ, *height, **RMQIndex;
int *belong;
int root, TreeSize;
int **rootToRoot, *rootSite;
int **dis, **dis_next, **pos, **pos2;
int *posSize, *pos2Size;
int *chSize;
int ** ch;
int *LOG2, *LOGD; 
int rootSize;
int *DFSList, *toDFS;
int ***BS;

inline	int LCAQuery(int _p, int _q){
		int p = toRMQ[_p], q = toRMQ[_q];
		
		if (p > q){
			int x = p;
			p = q;
			q = x;
		}
		int len = q - p + 1;
		
		int i = LOGD[len], k = LOG2[len];
		
		q = q - i + 1;
		if (height[RMQIndex[k][p]] < height[RMQIndex[k][q]])
			return RMQIndex[k][p];
		else return RMQIndex[k][q]; 
	}
	
long long queryCnt;	
//long long aCnt;
inline	int distanceQuery(int p, int q){
		if (p == q) return 0;
		int x = belong[p], y = belong[q];	
		int lca = LCAQuery(x, y);
		if (lca == x || lca == y){
			queryCnt++;
	//		aCnt++;
			if (lca == y){
				int v = y;
				y = x;
				x = v;
				v = p;
				p = q;
				q = v;
			}
			return dis[y][pos[x][posSize[x] - 1]];
		}
		else {
			int res = infinity;
			int *dx = dis[x], *dy = dis[y],*p2 = pos2[lca];
			_mm_prefetch(dx, _MM_HINT_T0);
			_mm_prefetch(dy, _MM_HINT_T0);
			_mm_prefetch(p2, _MM_HINT_T0);
			int ps = pos2Size[lca];
			for (int i = 0; i < ps; i++){
				queryCnt ++;
				int tmp = dx[p2[i]] + dy[p2[i]];
				if (res > tmp)
					res = tmp;
			}
			return res;
		}
		
	}
	
	int *Degree;
	int **Neighbor, **Weight;
	void readGraph(char *filename){
		FILE * file = fopen(filename, "r");
		int n, m;
		fscanf(file, "%d %d", &n, &m);
		Degree = (int*)malloc(sizeof(int) * (n + 1));
		vector< vector<pair<int, int> > > nb;
		vector<pair<int, int> > v;
		v.clear();
		for (int i = 0; i <= n; i++){
		//	Degree[i] = 0;
			nb.push_back(v);	
		}
	//	cout << n << " " << m << endl;
		for (int i = 0; i < m; i++){
			int x, y, z;
			fscanf(file, "%d %d %d", &x, &y, &z);
	//		Degree[x]++;
	//		cout << x << " " << y << " " << z << endl;
			nb[x].push_back(make_pair(y, z));
		}
		Neighbor = (int**)malloc(sizeof(int*) * (n + 1));
		Weight = (int**)malloc(sizeof(int*) * (n + 1));
		for (int i = 1; i <= n; i++){
			Degree[i] = nb[i].size();
			Neighbor[i] = (int*)malloc(sizeof(int) * nb[i].size());
			Weight[i] = (int*)malloc(sizeof(int) * nb[i].size());
			for (int j = 0; j < nb[i].size(); j++){
				Neighbor[i][j] = nb[i][j].first;
				Weight[i][j] = nb[i][j].second;
			}
		}
	}
    bool move(int &p, int highest){
    //    printf("move\n");
        if (height[belong[p]] == highest)
            return false;
        p = dis_next[belong[p]][highest];
        return true;
        
        
     //   printf("p, highest: %d %d\n", p, highest);
        int Degree_p = Degree[p];
        int i;
        for (i = 0; i < Degree_p; i++){
            int x = Neighbor[p][i];
        //    printf("i, p, height[belong[p]], highest, x, height[belong[x]]: %d %d %d %d %d %d\n", i, p, height[belong[p]], highest, x, height[belong[x]]);
            if (height[belong[x]] < highest + 1)
                continue;
       //     printf("dis[belong[x]][highest], Weight[p][i], dis[belong[p]][highest]: %d %d %d\n", dis[belong[x]][highest], Weight[p][i], dis[belong[p]][highest]);
            if (dis[belong[x]][highest] + Weight[p][i] == dis[belong[p]][highest]){
                printf("p, x, dis_next[belong[p]][highest]: %d %d %d\n", p, x, dis_next[belong[p]][highest]);
                printf("height[belong[p]], highest: %d %d\n", height[belong[p]], highest);
                if (dis_next[belong[p]][highest] == 0)
                    while(1);
                p = x;
                break;
            }
        }
     //   printf("i, Degree_p, p: %d %d %d\n", i, Degree_p, p);
        if (i >= Degree_p){
            printf(">= Degree_p");
            while (1);
            return false;
        }
        else return true;
            
    }
    inline vector<int> shortestPathQuery(int p, int q){
        int res = 0;
        vector<int> path;
        path.clear();
        while (p != q){
            res++;
            path.push_back(p);
            int pq = distanceQuery(p, q);
            for (int i = 0; i < Degree[p]; i++){
                int x = Neighbor[p][i];
            //    int y = Weight[p][i];
                int xq = distanceQuery(x, q);
                if (xq + Weight[p][i] == pq){
                    p = x;
                    break;
                }
            }
        }
        path.push_back(q);
        return path;
    }
    void print_vector(vector<int> a){
        for (int i = 0; i < a.size(); i++)
            printf("%d, ", a[i]);
        printf("\n");
    }
	inline vector<int> optimal_shortestPathQuery(int p, int q){
        int res, highest = -1;
   //     printf("height[root]: %d\n", height[root]);
   //     printf("p, q: %d %d\n", p, q);
//-----------------------------------------------
            int x = belong[p], y = belong[q];
            int lca = LCAQuery(x, y);
        
        
            res = infinity;
            int *dx = dis[x], *dy = dis[y],*p2 = pos2[lca];
            _mm_prefetch(dx, _MM_HINT_T0);
            _mm_prefetch(dy, _MM_HINT_T0);
            _mm_prefetch(p2, _MM_HINT_T0);
            for (int i = 0; i < height[lca]; i++){
                queryCnt ++;
                int tmp = dx[i] + dy[i];
                if (res > tmp){
                    res = tmp;
                    highest = i;
                }
            }
   //     printf("lca, highest: %d %d\n", lca, highest);
//-----------------------------------------------
        vector<int> path1, path2;
        path1.clear();
        path2.clear();
        int tt = 0;
		while (p != q){
            tt++;
            bool is_updated = false;
        //   printf("p, q, belong[p], belong[q], height[belong[p]], height[belong[q]]: %d %d %d %d %d %d\n", p, q, belong[p], belong[q], height[belong[p]], height[belong[q]]);
            if (height[belong[p]] > highest + 1){
             //   if (path1.size() > 0 && path1[path1.size() - 1] == p){
              //      print_vector(path1);
              //      while (1);
             //   }
                path1.push_back(p);
                bool ok = move(p, highest);
                is_updated = is_updated || ok;
           //     res++;
            }
            if (height[belong[q]] > highest + 1){
            //    if (path2.size() > 0 && path2[path2.size() - 1] == q){
            //        print_vector(path2);
            //        while (1);
            //    }
                path2.push_back(q);
                bool ok = move(q, highest);
                is_updated = is_updated || ok;
            //    res++;
            }
        //    if (!is_updated){
        //        printf("not update\n");
        //        while (1);
         //   }
		}
    //    printf("height[lca], tt: %d %d\n", height[lca], tt);
        path1.push_back(p);
        reverse(path2.begin(), path2.end());
        path1.insert(path1.end(), path2.begin(), path2.end());
     //   printf("path1: ");
      //  print_vector(path1);
      //  printf("path2: ");
      //  print_vector(path2);
		return path1;
	}
    int calculate_path_distance(vector<int> path){
        int res = 0;
        for (int i = 0; i < path.size() - 1; i++)
            res += distanceQuery(path[i], path[i + 1]);
        return res;
    }
	int test(char *file, char *op){
	//	cout << "test: " << file << " BEGIN" << endl;
		FILE *fin = fopen(file, "r");
		int x, y, dis, res = 0;
		vector<int> X, Y, DIS;
		X.clear();
		Y.clear();
		DIS.clear();
		while (fscanf(fin, "%d %d %d", &x, &y, &dis) != EOF){
			if (x <= 0 || y <= 0) break;
			X.push_back(x);
			Y.push_back(y);
			DIS.push_back(dis);
			res++;
		}
		cout << X.size() << endl;

		//
		queryCnt = 0;
	//	aCnt = 0;
		//
		double t= 0;
		int *ANS;
        int type;
        if (strcmp(op, "normal") == 0)
            type = 1;
        else if (strcmp(op, "optimal") == 0)
            type = 2;
        else {
            printf("operate type error!\n");
            while (1);
        }
		ANS = (int*)malloc(sizeof(int) * (X.size() + 1));
		double start_time = GetTime();
		for (int i = 0; i < X.size(); i++){
            if (type == 1)
                shortestPathQuery(X[i], Y[i]);
            else optimal_shortestPathQuery(X[i], Y[i]);
           //     ANS[i] = calculate_path_distance(shortestPathQuery(X[i], Y[i]));
		//	ANS[i] = distanceQuery(X[i], Y[i]);
        //    else ANS[i] = calculate_path_distance(optimal_shortestPathQuery(X[i], Y[i]));
         //   printf("normal length: %d\n", shortestPathQuery(X[i], Y[i]).size());
        //    print_vector(shortestPathQuery(X[i], Y[i]));
        //    printf("optimal length: %d\n", optimal_shortestPathQuery(X[i], Y[i]).size());
         //   print_vector(optimal_shortestPathQuery(X[i], Y[i]));
		//	if (ANS[i] != DIS[i]){
		//		cout << X[i] << " " << Y[i] << " " << DIS[i] << " " << ANS[i] << endl;
		//		while(1);
		//	}
		}
		double end_time = GetTime();
		for (int i = 0; i < X.size(); i++){
			t += ANS[i];
		}
	//	cout << end_time - start_time << endl;
	//	cout << res << endl;
		//cout << "Check Count: " << double(queryCnt) / res  << endl;
	//	cout << "aCnt: " << double(aCnt) / res << endl;
		//printf("Distance Query Time : %lf usec\n", (end_time - start_time) * 1e6 / res);
		//printf("average distance: %.6lf\n",t / res);
		/*
		start_time = GetTime();
		long long step = 0;
		for (int i = 0; i < X.size(); i++){
			step += shortestPathQuery(X[i], Y[i]);
		}		
		end_time = GetTime();
		cout << step;
		printf("Shortest Path Query Time : %lf usec\n", (end_time - start_time) * 1e6 / res);
		printf("average step: %.6lf\n", double(step) / double(res));
		*/
		return res;
	}
	
	
	FILE *fin;
	string TT = "";
	void scanIntArray(int *a, int n){
		fread(a, SIZEOFINT, n, fin);
	}
	int* scanIntVector(int *a){
		int _n;
		fread(&_n, SIZEOFINT, 1, fin);
		a = (int*)malloc(sizeof(int) * _n);
		scanIntArray(a, _n);
		return a;
	}

	int n;
	int *EulerSeq;
	void readIndex(char *file){
		double _time = GetTime();
		int tree_height = 0, tree_width = 0, most_sp = 0;
		fin = fopen(file, "rb");
		fread(&n, SIZEOFINT, 1, fin);
		int ts;
		fread(&ts, SIZEOFINT, 1, fin);
		TreeSize = ts;
		height = (int*)malloc(sizeof(int) * (ts + 1));
		for (int i = 0; i < ts; i++){
			fread(&height[i], SIZEOFINT, 1, fin);
		}
		belong = (int*)malloc(sizeof(int) * (n + 1));
	  	fread(belong, SIZEOFINT, n + 1, fin);
		toRMQ = (int*)malloc(sizeof(int) * (n + 1));
	  	fread(toRMQ, SIZEOFINT, n + 1, fin);
		int ris;
		fread(&ris, SIZEOFINT, 1, fin);
		fread(&ts, SIZEOFINT, 1, fin);
		EulerSeq = (int*)malloc(sizeof(int) * (ts + 1));
		RMQIndex = (int**)malloc(sizeof(int*) * (ris + 1));
		for (int i = 0; i < ris; i++){
			RMQIndex[i] = scanIntVector(RMQIndex[i]);
		}
		fread(&root, SIZEOFINT, 1, fin);
		cout << "root: " << root << endl;
		
		posSize = (int*)malloc(sizeof(int) * (n + 1));
		pos2Size = (int*)malloc(sizeof(int) * (n + 1));
		pos = (int**)malloc(sizeof(int*) * (TreeSize));
		pos2 = (int**)malloc(sizeof(int*) * (TreeSize));
		dis = (int**)malloc(sizeof(int*) * (TreeSize));
        dis_next = (int**)malloc(sizeof(int*) * (TreeSize));
		chSize = (int*)malloc(sizeof(int) * (TreeSize));
		ch = (int**)malloc(sizeof(int*) * (TreeSize));
			
		for (int i = 0; i < TreeSize; i++){
			fread(&chSize[i], SIZEOFINT, 1, fin);
			ch[i] = (int*)malloc(sizeof(int) * chSize[i]);
			for (int j = 0; j < chSize[i]; j++){
				int x;
				fread(&x, SIZEOFINT, 1, fin);
				ch[i][j] = x;
			}
		}
		for (int i = 0; i < TreeSize; i++){
			int x;
			fread(&x, SIZEOFINT, 1, fin);
			fread(&posSize[x], SIZEOFINT, 1, fin);
			pos[x] = (int*)malloc(sizeof(int) * (posSize[x] + 1));
			fread(pos[x], SIZEOFINT, posSize[x], fin);
			if (posSize[x] > tree_width)
				tree_width = posSize[x];
			int _n, _m;
			fread(&_n, SIZEOFINT, 1, fin);
			dis[x] = (int*)malloc(sizeof(int) * _n);
            dis_next[x] = (int*)malloc(sizeof(int) * _n);
			fread(dis[x], SIZEOFINT, _n, fin);
            fread(&_m, SIZEOFINT, 1, fin);
         //   printf("x, posSize[x], disSize[x], disnextSize[x]: %d %d %d %d\n", x, posSize[x], _n, _m);
            if (_n != _m)
                while(1);
            fread(dis_next[x], SIZEOFINT, _m, fin);
			if (_n > tree_height)
				tree_height = _n;
		}
		printf("dis read finished!\n");
		for (int i = 0; i < TreeSize; i++){
			int x;
			fread(&x, SIZEOFINT, 1, fin);
			fread(&pos2Size[x], SIZEOFINT, 1, fin);
			pos2[x] = (int*)malloc(sizeof(int) * (pos2Size[x] + 1));
			fread(pos2[x], SIZEOFINT, pos2Size[x], fin);
			if (pos2Size[x] > most_sp)
				most_sp = pos2Size[x];
		}
		
		fclose(fin);
		//
		printf("Load Index Time : %lf sec\n", (GetTime() - _time));
		printf("tree height: %d\n", tree_height);
		printf("tree width: %d\n", tree_width);
		printf("most search space: %d\n", most_sp);
		//
	}
	int cnt;
	void getDFSListDFS(int p){
		toDFS[p] = cnt;
		DFSList[cnt++] = p;
		for (int i = 0; i < chSize[p]; i++){
			getDFSListDFS(ch[p][i]);
		}
		BS[p] = (int**)malloc(sizeof(int*) * chSize[p]);
		for (int i = 0; i < chSize[p]; i++){
			BS[p][i] = (int*)malloc(sizeof(int) * chSize[p]);
			for (int j = 0; j < chSize[p]; j++){
				if (posSize[ch[p][i]] < posSize[ch[p][j]])
					BS[p][i][j] = ch[p][i];
				else BS[p][i][j] = ch[p][j];
			}
		}
	}
	void getDFSList(){
		DFSList = (int*) malloc(sizeof(int) * (TreeSize + 1));
		toDFS = (int*) malloc(sizeof(int) * (TreeSize + 1));
		BS = (int***)malloc(sizeof(int**) * (TreeSize + 1));
		cnt = 0;
		getDFSListDFS(root);
	}
	
//./query NY.index NY.query NY.gr normal
int main(int argc, char *argv[])
{
	if (argc != 5) {
        fprintf(stderr, "--------\n");
        return 0;
    }
	readGraph(argv[1]);
	readIndex(argv[2]);
	
	int s=atoi(argv[3]);
	int t=atoi(argv[4]);
	cout<<distanceQuery(s,t);
	return 0;
	//cout << "Load Graph Finished!" << endl;
//	getDFSList();

	//
//	cout << n << endl;
// 	LOG2 = (int*)malloc(sizeof(int) * (n * 2 + 10));
// 	LOGD = (int*)malloc(sizeof(int) * (n * 2 + 10));
// 	int k = 0, j = 1;
//     cout << n << endl;
// 	for (int i = 0; i < n * 2 + 10; i++){
// 		if (i > j * 2){
// 			j *= 2;
// 			k++;
// 		}
// 		LOG2[i] = k;
// 		LOGD[i] = j;
// 	}
//     cout << "hehe" << endl;
//  //   test(argv[2], argv[4]);
// 	//
	
// 	//printf("---- %d\n", distanceQuery(102865, 72638));
	
// 	//
// 	int m = 20;
// //	FILE *Q[11];
// 	char QF[m + 1][100];
//     cout << "hehe" << endl;
// 	for (int i = 1; i <= m; i++){
// 		int j;
// 		for (j = 0; argv[2][j] != '\0'; j++){
// 			QF[i][j] = argv[2][j];
// 		}
// 		QF[i][j++] = '.';
// 		QF[i][j++] = 'Q';
// 		if (i >= 10){
// 			QF[i][j++] = '0' + i / 10;
// 			QF[i][j++] = '0' + i % 10;
// 		}
// 		else QF[i][j++] = '0' + i;
// 		QF[i][j] = '\0';
// 	//	Q[i] = fopen(QF[i], "w");
// 	}
//     cout << "hehe" << endl;
// 	for (int i = 1; i <= 10; i++){
// 		cout << QF[i] << endl;
// 		test(QF[i], argv[4]);
// 	}

    /*
	while (true){
		int i;
		cin >> i;
		if (i <= 0 || i > m) break;
		test(QF[i]);
	}
	
	*/
	//test(argv[2]);
}
