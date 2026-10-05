package eu.metroworld.infrastructure.world;
import java.util.*;
/** Immutable spatial plan. Stations own explicit ports; routes never start inside platforms. */
public final class NetworkPlan {
 public enum Condition { INTACT, COLLAPSED, CHEMICAL, QUARANTINE }
 public static final int SPACING=512,SHELL=6;
 public record Node(int x,int z,int y){}
 public record Station(Node node,long salt,Condition condition,boolean interchange){
  public Station(Node node,long salt,Condition condition){this(node,salt,condition,false);}
  public boolean intersects(int x,int z,int margin){return Math.abs(node.x-x-7.5)<=40+margin+7.5&&Math.abs(node.z-z-7.5)<=17+margin+7.5;}
 }
 public record Path(TransitGeometry.Route route,Condition condition,long salt,boolean service,boolean transfer){
  public Path(TransitGeometry.Route route,Condition condition,long salt,boolean service){this(route,condition,salt,service,false);}
 }
 public record Room(int x1,int x2,int z1,int z2,int base,int height,Condition condition,long salt){
  public boolean intersects(int x,int z,int margin){return x2+margin>=x&&x1-margin<=x+15&&z2+margin>=z&&z1-margin<=z+15;}
 }
 public record StairTower(int x,int z,int bottom,int top,long salt){
  public boolean intersects(int sx,int sz,int margin){return x+11+margin>=sx&&x-3-margin<=sx+15&&z+10+margin>=sz&&z-1-margin<=sz+15;}
 }
 public record ChunkPlan(List<Station> stations,List<Path> paths,List<Room> rooms,List<StairTower> stairs){}
 public static long hash(long seed,int a,int b,int level){long n=seed^(a*341873128712L)^(b*132897987541L)^(level*42317861L);n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
 public static Node node(long seed,int gx,int gz){
  if(gx==0&&gz==0)return new Node(0,0,0);
  long h=hash(seed,gx,gz,17);return new Node(gx*SPACING+(int)Math.floorMod(h,161)-80,gz*SPACING+(int)Math.floorMod(h>>>16,161)-80,(int)Math.floorMod(h>>>32,321)-176);
 }
 public static Condition condition(long seed,int gx,int gz,int salt){
  int v=(int)Math.floorMod(hash(seed,gx,gz,salt),100);
  return v<70?Condition.INTACT:v<84?Condition.COLLAPSED:v<92?Condition.CHEMICAL:Condition.QUARANTINE;
 }
 private static TransitGeometry.Point point(Node n){return new TransitGeometry.Point(n.x,n.y,n.z);}
 private static void addPath(List<Path> out,TransitGeometry.Route route,Condition c,long h,boolean service,int sx,int sz){if(route.intersects(sx,sz,SHELL+(service?3:5)))out.add(new Path(route,c,h,service));}
 public static ChunkPlan forChunk(long seed,int sx,int sz){
  var stations=new ArrayList<Station>();var paths=new ArrayList<Path>();var rooms=new ArrayList<Room>();var stairs=new ArrayList<StairTower>();
  int gx=Math.floorDiv(sx,SPACING),gz=Math.floorDiv(sz,SPACING);
  // A north/south crossing sweeps outside its source cell; include two neighbour rings.
  for(int ix=gx-2;ix<=gx+2;ix++)for(int iz=gz-2;iz<=gz+2;iz++){
   Node a=node(seed,ix,iz);long h=hash(seed,ix,iz,29);Condition c=ix==0&&iz==0?Condition.INTACT:condition(seed,ix,iz,41);
   Station station=new Station(a,h,c,ix==1&&iz==0);if(station.intersects(sx,sz,SHELL))stations.add(station);
   // Side maintenance wing opens through a deliberate four-block station door.
   Room room=new Room(a.x-24,a.x+6,a.z+14,a.z+42,a.y+2,9,c,h);
   if(room.intersects(sx,sz,SHELL))rooms.add(room);
   int serviceFloor=Math.clamp(a.y+2+((h&4)==0?32:-32),-224,208);
   int bottom=Math.min(a.y+2,serviceFloor),top=Math.max(a.y+2,serviceFloor);
   StairTower tower=new StairTower(a.x+50,a.z+11,bottom,top,h);if(tower.intersects(sx,sz,SHELL))stairs.add(tower);
   // Pedestrian access remains level; stairs alone carry the change in elevation.
   var connector=new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+34,a.y+2,a.z+12),new TransitGeometry.Point(a.x+48,a.y+2,a.z+12)));
   addPath(paths,connector,Condition.INTACT,h,true,sx,sz);
   var gallery=new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+48,serviceFloor,a.z+12),new TransitGeometry.Point(a.x+48,serviceFloor,a.z+66)));
   addPath(paths,gallery,c,h,true,sx,sz);
   Room cache=new Room(a.x+38,a.x+61,a.z+56,a.z+81,serviceFloor,8,c,h^23);if(cache.intersects(sx,sz,SHELL))rooms.add(cache);
   for(int d=0;d<2;d++){
    if(d==1&&Math.floorMod(ix+iz,3)!=0)continue;
    Node b=node(seed,ix+(d==0?1:0),iz+(d==1?1:0));long eh=hash(seed,ix,iz,80+d);
    int minX=Math.min(a.x+35,b.x-70)-12,maxX=Math.max(a.x+300,b.x+170),minZ=Math.min(a.z,b.z)-140,maxZ=Math.max(a.z,b.z)+140;
    if(maxX>=sx&&minX<=sx+15&&maxZ>=sz&&minZ<=sz+15)
     addPath(paths,TransitGeometry.between(point(a),point(b),d,40,eh),condition(seed,ix,iz,80+d),eh,false,sx,sz);
   }
  }
  // Put the showcase interchange in the next grid cell, away from chunks that a 0.3 player
  // is likely to have already generated around the world spawn.
  {
   Node a=node(seed,1,0),b=new Node(a.x+320,a.z,a.y);long h=hash(seed,1,0,911);
   Station second=new Station(b,h,Condition.INTACT,true);
   if(second.intersects(sx,sz,SHELL))stations.add(second);
   var walk=new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+34,a.y+2,a.z+12),new TransitGeometry.Point(b.x-34,b.y+2,b.z+12)));
   if(walk.intersects(sx,sz,SHELL+3))paths.add(new Path(walk,Condition.INTACT,h,true,true));
  }
  return new ChunkPlan(List.copyOf(stations),List.copyOf(paths),List.copyOf(rooms),List.copyOf(stairs));
 }
}
