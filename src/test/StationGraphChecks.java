import eu.metroworld.infrastructure.world.*;
import java.util.*;
public class StationGraphChecks {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){
  var seen=new HashSet<StationGraph.Index>();var queue=new ArrayDeque<StationGraph.Index>();queue.add(new StationGraph.Index(0,0));
  while(!queue.isEmpty()){var at=queue.remove();if(!seen.add(at))continue;var ns=StationGraph.neighbours(at.x(),at.z());check(ns.size()==(StationGraph.anchor(at.x(),at.z())?4:2),"isolated station");
   for(var n:ns){check(!n.equals(at)&&StationGraph.neighbours(n.x(),n.z()).contains(at),"non reciprocal edge");if(Math.abs(n.x())<=9&&Math.abs(n.z())<=9&&!seen.contains(n))queue.add(n);}}
  int expected=0;for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)if(StationGraph.exists(x,z))expected++;
  check(seen.size()==expected,"isolated local component");int routes=0,cells=0,chunks=0,spirals=0;
  for(long seed:new long[]{0,7,619015,Long.MIN_VALUE})for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(boolean ns:new boolean[]{false,true}){
   if(ns?Math.floorMod(x,3)!=0:Math.floorMod(z,3)!=0)continue;
   var a=StationGraph.railNode(seed,x,z,ns);var b=StationGraph.railNode(seed,x+(ns?0:1),z+(ns?1:0),ns);var route=NetworkPlan.connection(seed,x,z,ns);var points=route.points();
   check(points.get(0).y()==a.y()&&points.get(points.size()-1).y()==b.y(),"wrong station height");
   if(Math.abs(b.y()-a.y())<48)for(int i=1;i<points.size();i++)check((ns?points.get(i).z()-points.get(i-1).z():points.get(i).x()-points.get(i-1).x())>=-1e-8,"local return loop");
   for(int end=0;end<2;end++){
    var n=end==0?a:b;var hall=NetworkPlan.forChunk(seed,Math.floorDiv(n.x(),16)*16,Math.floorDiv(n.z(),16)*16).stations().stream().filter(s->s.node().equals(n)&&s.northSouth()==ns).findFirst().orElseThrow();
    var port=points.get(end==0?0:points.size()-1);int sign=end==0?1:-1;
    check(port.x()==n.x()+(ns?0:sign*NetworkPlan.halfLength(hall.kind()))&&port.z()==n.z()+(ns?sign*NetworkPlan.halfLength(hall.kind()):0),"route misses station doorway");
   }
   boolean reverses=false;for(int i=1;i<points.size();i++)if((ns?points.get(i).z()-points.get(i-1).z():points.get(i).x()-points.get(i-1).x())<-1e-8)reverses=true;
   if(reverses){spirals++;check(Math.abs(b.y()-a.y())>=48,"unnecessary local coil");}
   var occupied=new HashSet<String>();var checkedChunks=new HashSet<String>();
   for(var track:NetworkPlan.trackRoutes(seed,x,z,ns))for(var lane:(track.singleTrack()?RailPlan.ofSingle(track.route()):RailPlan.of(track.route())).lanes()){
    for(int end=0;end<2;end++){
     var n=end==0?a:b;var cell=lane.get(end==0?0:lane.size()-1);int gap=3+NetworkPlan.trackOffset(seed,x+(end==1&&!ns?1:0),z+(end==1&&ns?1:0),ns);
     check((ns?Math.abs(cell.x()-n.x()):Math.abs(cell.z()-n.z()))==gap,"lane misses station layout");
    }
    check(lane.get(0).floor()==a.y()&&lane.get(lane.size()-1).floor()==b.y(),"rails miss station port height");
    for(int i=0;i<lane.size();i++){
     var c=lane.get(i);cells++;check(occupied.add(c.x()+","+c.floor()+","+c.z()),"overlapping lane");
     if(i>0){var prev=lane.get(i-1);check(Math.abs(c.x()-prev.x())+Math.abs(c.z()-prev.z())==1,"rail gap");check(Math.abs(c.floor()-prev.floor())<=1,"rail height jump");if(c.floor()!=prev.floor())check((c.floor()<prev.floor()?c:prev).shape().startsWith("ascending_"),"slope on curve");}
     int sx=Math.floorDiv(c.x(),16)*16,sz=Math.floorDiv(c.z(),16)*16;
     if(checkedChunks.add(sx+","+sz)){chunks++;check(NetworkPlan.forChunk(seed,sx,sz).paths().stream().anyMatch(p->p.route().points().equals(track.route().points())),"route clipped at chunk border "+sx+","+sz);}
    }
   }
   routes++;
  }
  check(spirals>0,"spirals absent from new planner");
  int located=0;
  for(long seed:new long[]{0,7,619015,Long.MIN_VALUE})for(int px:new int[]{-4097,0,2000})for(int pz:new int[]{-1000,0,3001}){
   var found=NetworkPlan.nearestBiocenter(seed,px,pz);check(found!=null,"biocenter missing");
   double distance=Math.hypot(found.x()-px,found.z()-pz);
   for(int x=-24;x<=24;x++)for(int z=-24;z<=24;z+=3){
    if(StationGraph.anchor(x,z))continue;var n=StationGraph.node(seed,x,z);
    if(NetworkPlan.stationKind(n,NetworkPlan.hash(seed,x,z,29))==NetworkPlan.StationKind.BIOCENTER)check(Math.hypot(n.x()-px,n.z()-pz)>=distance-1e-8,"locator returned farther biocenter");
   }
   var halls=NetworkPlan.forChunk(seed,Math.floorDiv(found.x(),16)*16,Math.floorDiv(found.z(),16)*16).stations();
   check(halls.stream().anyMatch(s->s.node().equals(found)&&s.kind()==NetworkPlan.StationKind.BIOCENTER),"locator disagrees with generated station");located++;
  }
  int searches=0;
  for(long seed:new long[]{7,619015})for(String type:new String[]{"station","passenger","mini","interchange","terminal","freight","biocenter"})for(int px:new int[]{-2000,0,2000}){
   var found=NetworkPlan.nearestStation(seed,px,1700,type);check(found!=null,"missing station type "+type);
   var n=found.node();var halls=NetworkPlan.forChunk(seed,Math.floorDiv(n.x(),16)*16,Math.floorDiv(n.z(),16)*16).stations();
   check(halls.stream().anyMatch(h->h.node().equals(n)&&h.northSouth()==found.northSouth()&&h.kind()==found.kind()),"locator mismatch "+type);
   check(type.equals("station")||type.equals("interchange")&&found.interchange()||found.kind().name().equals(type.toUpperCase(java.util.Locale.ROOT))||type.equals("passenger")&&found.kind()==NetworkPlan.StationKind.PASSENGER,"wrong station type "+type);searches++;
  }
  System.out.println("PASS: "+searches+" station-type searches");
  System.out.println("PASS: "+spirals+" spiral connections; "+located+" nearest biocenter searches checked against regional candidates");
  System.out.println("PASS: connected "+expected+"-station graph; "+routes+" station connections, "+cells+" rails, "+chunks+" chunk ownership checks");
 }
}
