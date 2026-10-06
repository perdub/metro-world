package eu.metroworld.infrastructure.world;
import java.util.*;
/** Bounded three-dimensional A*, with legal grades, rounded bends and flat station throats. */
public final class NoiseRouter {
 private record Key(long seed,StationGraph.Id a,StationGraph.Id b){}
 private static final Map<Key,Optional<TransitGeometry.Route>> CACHE=new LinkedHashMap<>(256,.75f,true){protected boolean removeEldestEntry(Map.Entry<Key,Optional<TransitGeometry.Route>> e){return size()>256;}};
 private record Cell(int x,int y,int z){}
 private record CellKey(long seed,Cell cell){}
 private static final Map<CellKey,Boolean> CLEARANCE=new LinkedHashMap<>(8192,.75f,true){protected boolean removeEldestEntry(Map.Entry<CellKey,Boolean> e){return size()>65536;}};
 private static boolean clear(long seed,Cell c){synchronized(CLEARANCE){return CLEARANCE.computeIfAbsent(new CellKey(seed,c),k->ExclusionNoise.boxClear(seed,c.x*64,c.y*8+5,c.z*64,76,30,76));}}
 private record Visit(Cell cell,double cost,double score){}
 public static TransitGeometry.Route connect(long seed,StationGraph.Vertex first,StationGraph.Vertex second){
  boolean ns=Math.abs(first.node().z()-second.node().z())>Math.abs(first.node().x()-second.node().x());
  if(ns?first.node().z()>second.node().z():first.node().x()>second.node().x()){var t=first;first=second;second=t;}
  final var a=first;final var b=second;
  synchronized(CACHE){return CACHE.computeIfAbsent(new Key(seed,a.id(),b.id()),k->Optional.ofNullable(build(seed,a,b,ns))).orElse(null);}
 }
 private static TransitGeometry.Point point(NetworkPlan.Node n){return new TransitGeometry.Point(n.x(),n.y(),n.z());}
 private static double heuristic(Cell a,Cell b){return 64*Math.hypot(a.x-b.x,a.z-b.z)+2*Math.abs(a.y-b.y);}
 private static TransitGeometry.Route build(long seed,StationGraph.Vertex av,StationGraph.Vertex bv,boolean ns){
  var a=StationGraph.railNode(av,ns);var b=StationGraph.railNode(bv,ns);TransitGeometry.Route direct;
  try{direct=TransitGeometry.spiralConnection(point(a),point(b),ns,54,54);}catch(IllegalArgumentException e){direct=null;}
  if(direct!=null&&ExclusionNoise.routeClear(seed,direct,40))return direct;
  if((ns?b.z()-a.z():b.x()-a.x())<512)return null;
  double ax=a.x()+(ns?0:134),az=a.z()+(ns?134:0),bx=b.x()-(ns?0:134),bz=b.z()-(ns?134:0);
  int ay=(int)Math.round(a.y()/8.0),by=(int)Math.round(b.y()/8.0);
  Cell start=ns?new Cell((int)Math.round(ax/64),ay,(int)Math.ceil(az/64)+1):new Cell((int)Math.ceil(ax/64)+1,ay,(int)Math.round(az/64));
  Cell goal=ns?new Cell((int)Math.round(bx/64),by,(int)Math.floor(bz/64)-1):new Cell((int)Math.floor(bx/64)-1,by,(int)Math.round(bz/64));
  int minX=Math.min(start.x,goal.x)-12,maxX=Math.max(start.x,goal.x)+12,minZ=Math.min(start.z,goal.z)-12,maxZ=Math.max(start.z,goal.z)+12;
  var open=new PriorityQueue<Visit>(Comparator.comparingDouble(Visit::score).thenComparingInt(v->v.cell.x).thenComparingInt(v->v.cell.y).thenComparingInt(v->v.cell.z));
  var costs=new HashMap<Cell,Double>();var previous=new HashMap<Cell,Cell>();var clearance=new HashMap<Cell,Boolean>();
  if(!clearance.computeIfAbsent(start,c->clear(seed,c)))return null;
  costs.put(start,0.0);open.add(new Visit(start,0,heuristic(start,goal)));boolean found=false;int iterations=0;
  int[][] directions={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
  while(!open.isEmpty()&&iterations++<16000){var visit=open.poll();Cell c=visit.cell;if(visit.cost>costs.getOrDefault(c,Double.POSITIVE_INFINITY))continue;if(c.equals(goal)){found=true;break;}
   for(int[] d:directions)for(int dy=-1;dy<=1;dy++){
    Cell n=new Cell(c.x+d[0],c.y+dy,c.z+d[1]);
    Cell previousCell=previous.get(c);if(previousCell!=null&&(c.x-previousCell.x)*d[0]+(c.z-previousCell.z)*d[1]<0)continue;
    if((c.equals(start)||n.equals(goal))&&(ns?d[1]:d[0])<=0)continue;
    if(n.x<minX||n.x>maxX||n.z<minZ||n.z>maxZ||n.y< -26||n.y>23)continue;
    if(!clearance.computeIfAbsent(n,v->clear(seed,v)))continue;
    double cost=visit.cost+64*Math.hypot(d[0],d[1])+2*Math.abs(dy);
    if(cost>=costs.getOrDefault(n,Double.POSITIVE_INFINITY))continue;
    previous.put(n,c);costs.put(n,cost);open.add(new Visit(n,cost,cost+heuristic(n,goal)));
   }
  }
  if(!found)return null;
  var cells=new ArrayList<Cell>();for(Cell c=goal;c!=null;c=previous.get(c))cells.add(c);Collections.reverse(cells);
  var controls=new ArrayList<TransitGeometry.Point>();controls.add(new TransitGeometry.Point(a.x()+(ns?0:54),a.y(),a.z()+(ns?54:0)));controls.add(new TransitGeometry.Point(ax,a.y(),az));
  for(int i=0;i<cells.size();i++){
   Cell c=cells.get(i);if(i>0&&i<cells.size()-1){Cell p=cells.get(i-1),n=cells.get(i+1);if(c.x-p.x==n.x-c.x&&c.y-p.y==n.y-c.y&&c.z-p.z==n.z-c.z)continue;}
   controls.add(new TransitGeometry.Point(c.x*64,c.y*8,c.z*64));
  }
  controls.add(new TransitGeometry.Point(bx,b.y(),bz));controls.add(new TransitGeometry.Point(b.x()-(ns?0:54),b.y(),b.z()-(ns?54:0)));
  var route=TransitGeometry.rounded(controls,24);
  return ExclusionNoise.routeClear(seed,route,40)?route:null;
 }
}
