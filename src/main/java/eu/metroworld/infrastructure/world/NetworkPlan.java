package eu.metroworld.infrastructure.world;
import java.util.*;
/** Immutable spatial plan. Stations own explicit ports; routes never start inside platforms. */
public final class NetworkPlan {
 public enum StationKind { PASSENGER, MINI, TERMINAL, INTERCHANGE, FREIGHT, BIOCENTER }
 public static StationKind stationKind(Node n,long salt){
  if(n.x==0&&n.z==0)return StationKind.INTERCHANGE;
  int roll=Math.floorMod((int)(salt>>>12),10);return roll<2?StationKind.MINI:roll==2?StationKind.TERMINAL:roll<5?StationKind.FREIGHT:roll<7?StationKind.BIOCENTER:StationKind.PASSENGER;
 }
 public enum TrackLayout { CENTRAL, ISLAND }
 public enum Condition { INTACT, COLLAPSED, CHEMICAL, QUARANTINE }
 public static final int SPACING=512,SHELL=6;
 public record Node(int x,int z,int y){}
 public static int halfLength(StationKind kind){return kind==StationKind.MINI?28:kind==StationKind.TERMINAL?54:40;}
 public static int halfWidth(StationKind kind){return kind==StationKind.MINI?12:kind==StationKind.TERMINAL?22:17;}
 public static int stationHeight(StationKind kind){return kind==StationKind.MINI?10:kind==StationKind.TERMINAL?18:14;}
 public record Station(Node node,long salt,Condition condition,boolean interchange,boolean northSouth,boolean portal,StationKind kind,TrackLayout trackLayout,int trackOffset){
  public Station(Node node,long salt,Condition condition){this(node,salt,condition,false,false,false,stationKind(node,salt),TrackLayout.CENTRAL,0);}
  public Station(Node node,long salt,Condition condition,boolean interchange){this(node,salt,condition,interchange,false,false,stationKind(node,salt),TrackLayout.CENTRAL,0);}
  public boolean intersects(int x,int z,int margin){
   int length=halfLength(kind),north=kind==StationKind.BIOCENTER?96:halfWidth(kind),south=halfWidth(kind)+16;
   return northSouth?node.x+north+margin>=x&&node.x-south-margin<=x+15&&node.z+length+margin>=z&&node.z-length-margin<=z+15:
    node.x+length+margin>=x&&node.x-length-margin<=x+15&&node.z+south+margin>=z&&node.z-north-margin<=z+15;
  }
 }
 public record Path(TransitGeometry.Route route,Condition condition,long salt,boolean service,boolean transfer,boolean singleTrack){
  public Path(TransitGeometry.Route route,Condition condition,long salt,boolean service){this(route,condition,salt,service,false,false);}
  public Path(TransitGeometry.Route route,Condition condition,long salt,boolean service,boolean transfer){this(route,condition,salt,service,transfer,false);}
 }
 public record TrackRoute(TransitGeometry.Route route,boolean singleTrack,long salt){}
 public record Room(int x1,int x2,int z1,int z2,int base,int height,Condition condition,long salt){
  public boolean intersects(int x,int z,int margin){return x2+margin>=x&&x1-margin<=x+15&&z2+margin>=z&&z1-margin<=z+15;}
 }
 public record StairTower(int x,int z,int bottom,int top,long salt){
  public boolean intersects(int sx,int sz,int margin){return x+11+margin>=sx&&x-3-margin<=sx+15&&z+10+margin>=sz&&z-1-margin<=sz+15;}
 }
 public record ChunkPlan(List<Station> stations,List<Path> paths,List<Room> rooms,List<StairTower> stairs){}
 public static long hash(long seed,int a,int b,int level){long n=seed^(a*341873128712L)^(b*132897987541L)^(level*42317861L);n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
 public static Node node(long seed,int gx,int gz){return StationGraph.node(seed,gx,gz);}
 public static Condition condition(long seed,int gx,int gz,int salt){
  int v=(int)Math.floorMod(hash(seed,gx,gz,salt),100);
  return v<70?Condition.INTACT:v<84?Condition.COLLAPSED:v<92?Condition.CHEMICAL:Condition.QUARANTINE;
 }
 private static TransitGeometry.Point point(Node n){return new TransitGeometry.Point(n.x,n.y,n.z);}
 private static void addPath(List<Path> out,TransitGeometry.Route route,Condition c,long h,boolean service,int sx,int sz){if(route.intersects(sx,sz,SHELL+(service?3:6)))out.add(new Path(route,c,h,service));}
 private record RouteKey(long seed,int x,int z,boolean northSouth){}
 private static final Map<RouteKey,TransitGeometry.Route> ROUTES=new LinkedHashMap<>(128,.75f,true){
  @Override protected boolean removeEldestEntry(Map.Entry<RouteKey,TransitGeometry.Route> entry){return size()>128;}
 };
 public static int trackOffset(long seed,int x,int z,boolean ns){
  if(StationGraph.anchor(x,z))return 0;
  long h=hash(seed,x,z,ns?217:216);return Math.floorMod(h,3)==0?4:0;
 }
 public static TrackLayout trackLayout(long seed,int x,int z,boolean ns){return trackOffset(seed,x,z,ns)==0?TrackLayout.CENTRAL:TrackLayout.ISLAND;}
 private static Node trackNode(long seed,int x,int z,boolean ns){return StationGraph.railNode(seed,x,z,ns);}

 private static StationKind kind(long seed,int x,int z,boolean ns){
  long h=hash(seed,x,z,29);if(StationGraph.anchor(x,z))return ns?StationKind.INTERCHANGE:Math.floorMod(h,3)==0?StationKind.TERMINAL:StationKind.INTERCHANGE;
  StationKind k=stationKind(node(seed,x,z),h);return ns&&k==StationKind.BIOCENTER?StationKind.PASSENGER:k;
 }
 public static TransitGeometry.Route connection(long seed,int x,int z,boolean ns){
  synchronized(ROUTES){return ROUTES.computeIfAbsent(new RouteKey(seed,x,z,ns),key->{
   var a=trackNode(seed,x,z,ns);var b=trackNode(seed,x+(ns?0:1),z+(ns?1:0),ns);
   return TransitGeometry.spiralConnection(point(a),point(b),ns,halfLength(kind(seed,x,z,ns)),halfLength(kind(seed,x+(ns?0:1),z+(ns?1:0),ns)));
  });}
 }
 private static final Map<RouteKey,List<TrackRoute>> TRACK_ROUTES=new LinkedHashMap<>(128,.75f,true){
  @Override protected boolean removeEldestEntry(Map.Entry<RouteKey,List<TrackRoute>> e){return size()>128;}
 };
 public static List<TrackRoute> trackRoutes(long seed,int x,int z,boolean ns){synchronized(TRACK_ROUTES){return TRACK_ROUTES.computeIfAbsent(new RouteKey(seed,x,z,ns),key->buildTrackRoutes(seed,x,z,ns));}}
 private static List<TrackRoute> buildTrackRoutes(long seed,int x,int z,boolean ns){
  var main=connection(seed,x,z,ns);var a=trackNode(seed,x,z,ns);var b=trackNode(seed,x+(ns?0:1),z+(ns?1:0),ns);
  boolean split=Math.abs(a.y()-b.y())<48&&Math.floorMod(hash(seed,x,z,309),5)==0;
  int startGap=3+trackOffset(seed,x,z,ns),endGap=3+trackOffset(seed,x+(ns?0:1),z+(ns?1:0),ns);
  if(split||startGap!=3||endGap!=3){
   var branches=TransitGeometry.splitMerge(main,startGap,endGap,split);return List.of(new TrackRoute(branches.get(0),true,hash(seed,x,z,310)),new TrackRoute(branches.get(1),true,hash(seed,x,z,311)));
  }
  return List.of(new TrackRoute(main,false,hash(seed,x,z,ns?81:80)));
 }
 public record StationMatch(Node node,StationKind kind,boolean northSouth,boolean interchange){}
 public static StationMatch nearestStation(long seed,double x,double z,String requested){
  int gx=(int)Math.floor(x/SPACING),gz=(int)Math.floor(z/SPACING);StationMatch best=null;double distance=Double.POSITIVE_INFINITY;
  for(int radius=0;radius<=96;radius++){
   for(int ix=gx-radius;ix<=gx+radius;ix++)for(int iz=gz-radius;iz<=gz+radius;iz++){
    if(radius>0&&Math.abs(ix-gx)!=radius&&Math.abs(iz-gz)!=radius)continue;
    if(!StationGraph.exists(ix,iz))continue;boolean anchor=StationGraph.anchor(ix,iz);
    for(boolean ns:new boolean[]{false,true}){
     if(ns?Math.floorMod(ix,3)!=0:Math.floorMod(iz,3)!=0)continue;
     StationKind k=kind(seed,ix,iz,ns);boolean transfer=anchor;
     boolean matches=requested.equals("station")||requested.equals("interchange")&&transfer||requested.equals("passenger")&&k==StationKind.PASSENGER||requested.equals("mini")&&k==StationKind.MINI||requested.equals("terminal")&&k==StationKind.TERMINAL||requested.equals("freight")&&k==StationKind.FREIGHT||requested.equals("biocenter")&&k==StationKind.BIOCENTER;
     if(!matches)continue;Node n=StationGraph.railNode(seed,ix,iz,ns);double d=Math.hypot(n.x()-x,n.z()-z);if(d<distance){distance=d;best=new StationMatch(n,k,ns,transfer);}
    }
   }
   if(best!=null&&radius*SPACING-128>distance)return best;
  }
  return best;
 }
 public static Node nearestBiocenter(long seed,double x,double z){
  Node best=null;double distance=Double.POSITIVE_INFINITY;
  int gx=(int)Math.floor(x/SPACING),gz=(int)Math.floor(z/SPACING);
  // Once the unvisited exterior cannot beat the best result, the nearest is proven.
  for(int radius=0;radius<=96;radius++){
   for(int ix=gx-radius;ix<=gx+radius;ix++)for(int iz=gz-radius;iz<=gz+radius;iz++){
    if(radius>0&&Math.abs(ix-gx)!=radius&&Math.abs(iz-gz)!=radius)continue;
    if(Math.floorMod(iz,3)!=0||StationGraph.anchor(ix,iz)||kind(seed,ix,iz,false)!=StationKind.BIOCENTER)continue;
    Node n=node(seed,ix,iz);double d=Math.hypot(n.x()-x,n.z()-z);if(d<distance){distance=d;best=n;}
   }
   if(best!=null&&radius*SPACING-64>distance)return best;
  }
  return best;
 }
 public static ChunkPlan forChunk(long seed,int sx,int sz){
  var stations=new ArrayList<Station>();var paths=new ArrayList<Path>();var rooms=new ArrayList<Room>();var stairs=new ArrayList<StairTower>();
  int gx=Math.floorDiv(sx,SPACING),gz=Math.floorDiv(sz,SPACING);
  for(int ix=gx-2;ix<=gx+2;ix++)for(int iz=gz-2;iz<=gz+2;iz++){
   if(!StationGraph.exists(ix,iz))continue;
   long h=hash(seed,ix,iz,29);Condition damage=ix==0&&iz==0?Condition.INTACT:condition(seed,ix,iz,41);
   boolean portal=StationGraph.anchor(ix,iz),horizontal=Math.floorMod(iz,3)==0,vertical=Math.floorMod(ix,3)==0;
   for(boolean ns:new boolean[]{false,true}){
    if(ns?!vertical:!horizontal)continue;
    Node n=StationGraph.railNode(seed,ix,iz,ns);Station station=new Station(n,h,damage,portal,ns,portal&&!ns,kind(seed,ix,iz,ns),trackLayout(seed,ix,iz,ns),trackOffset(seed,ix,iz,ns));
    if(station.intersects(sx,sz,SHELL))stations.add(station);
    for(var route:trackRoutes(seed,ix,iz,ns))if(route.route().intersects(sx,sz,SHELL+6))paths.add(new Path(route.route(),damage,route.salt(),false,false,route.singleTrack()));
   }
   Node a=node(seed,ix,iz);
   if(portal){
    int bottom=a.y-24+2,top=a.y+2;StairTower tower=new StairTower(a.x+72,a.z+11,bottom,top,h);
    if(tower.intersects(sx,sz,SHELL))stairs.add(tower);
    int upperStart=halfLength(kind(seed,ix,iz,false))-6;
    addPath(paths,new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+upperStart,top,a.z+12),new TransitGeometry.Point(a.x+70,top,a.z+12))),Condition.INTACT,h,true,sx,sz);
    addPath(paths,new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+14,bottom,a.z+12),new TransitGeometry.Point(a.x+70,bottom,a.z+12))),Condition.INTACT,h,true,sx,sz);
   }else if(horizontal){
    int serviceFloor=a.y+34,serviceZ=kind(seed,ix,iz,false)==StationKind.MINI?8:12;StairTower tower=new StairTower(a.x+66,a.z+serviceZ-1,a.y+2,serviceFloor,h);
    if(tower.intersects(sx,sz,SHELL))stairs.add(tower);
    int start=halfLength(kind(seed,ix,iz,false))-6;
    addPath(paths,new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+start,a.y+2,a.z+serviceZ),new TransitGeometry.Point(a.x+64,a.y+2,a.z+serviceZ))),Condition.INTACT,h,true,sx,sz);
    addPath(paths,new TransitGeometry.Route(List.of(new TransitGeometry.Point(a.x+64,serviceFloor,a.z+serviceZ),new TransitGeometry.Point(a.x+64,serviceFloor,a.z+66))),damage,h,true,sx,sz);
    Room cache=new Room(a.x+54,a.x+78,a.z+56,a.z+81,serviceFloor,8,damage,h);if(cache.intersects(sx,sz,SHELL))rooms.add(cache);
   }
  }
  return new ChunkPlan(List.copyOf(stations),List.copyOf(paths),List.copyOf(rooms),List.copyOf(stairs));
 }
}
