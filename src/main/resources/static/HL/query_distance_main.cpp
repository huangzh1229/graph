#include <iostream>
#include "highway_cover_labelling.h"

using namespace std;

int main(int argc, char **argv)
{

  int k = atoi(argv[2]);
  HighwayLabelling *hl = new HighwayLabelling(argv[1], k);
  hl->LoadIndex(argv[3]);

  int topk[k];
  hl->SelectLandmarks_HD(topk);
  hl->RemoveLandmarks(topk);

  // query distance
  int s=atoi(argv[4]), t=atoi(argv[5]);
  std::cout << (int)hl->QueryDistance(s, t)<<endl;
  return 0;

}
