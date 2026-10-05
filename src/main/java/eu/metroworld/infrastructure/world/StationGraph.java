package eu.metroworld.infrastructure.world;
import java.util.*;
/** Portal anchors first; intermediate stops subdivide each macro edge into three parts. */
public final class StationGraph {
 public static final int STOP_SPACING=512,ANCHOR_STEPS=3,ANCHOR_SPACING=STOP_SPACING*ANCHOR_STEPS;
 public record Index(int x,int z){}
 public static boolean anchor(int x,int z){return Math.floorMod(x,3)==0&&Math.floorMod(z,3)==0;}
 public static boolean exists(int x,int z){return Math.floorMod(x,3)==0||Math.floorMod(z,3)==0;}
 public static List<Index> neighbours(int x,int z){
  if(!exists(x,z))return List.of();var list=new ArrayList<Index>();
  if(Math.floorMod(z,3)==0){list.add(new Index(x-1,z));list.add(new Index(x+1,z));}
  if(Math.floorMod(x,3)==0){list.add(new Index(x,z-1));list.add(new Index(x,z+1));}
  return List.copyOf(list);
 }
 private static NetworkPlan.Node portal(long seed,int ax,int az){
  if(ax==0&&az==0)return new NetworkPlan.Node(0,0,0);
  long h=NetworkPlan.hash(seed,ax,az,171);
  return new NetworkPlan.Node(ax*ANCHOR_SPACING+(int)Math.floorMod(h,129)-64,az*ANCHOR_SPACING+(int)Math.floorMod(h>>>16,129)-64,(int)Math.floorMod(h>>>32,241)-144);
 }
 public static NetworkPlan.Node node(long seed,int x,int z){
  if(!exists(x,z))throw new IllegalArgumentException("No station off a macro edge");
  if(anchor(x,z))return portal(seed,Math.floorDiv(x,3),Math.floorDiv(z,3));
  boolean horizontal=Math.floorMod(z,3)==0;int ax=Math.floorDiv(x,3),az=Math.floorDiv(z,3);
  var a=portal(seed,ax,az);var b=portal(seed,ax+(horizontal?1:0),az+(horizontal?0:1));
  double t=Math.floorMod(horizontal?x:z,3)/3.0;
  return new NetworkPlan.Node((int)Math.round(a.x()+(b.x()-a.x())*t),(int)Math.round(a.z()+(b.z()-a.z())*t),(int)Math.round(a.y()+(b.y()-a.y())*t));
 }
 public static NetworkPlan.Node railNode(long seed,int x,int z,boolean northSouth){var n=node(seed,x,z);return northSouth?new NetworkPlan.Node(n.x(),n.z(),n.y()-24):n;}
 public static NetworkPlan.Node nearestPortal(long seed,double x,double z){
  int ax=(int)Math.floor((x+ANCHOR_SPACING/2.0)/ANCHOR_SPACING),az=(int)Math.floor((z+ANCHOR_SPACING/2.0)/ANCHOR_SPACING);
  NetworkPlan.Node best=null;double distance=Double.POSITIVE_INFINITY;
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
   var n=portal(seed,ax+dx,az+dz);double d=Math.pow(x-n.x(),2)+Math.pow(z-n.z(),2);if(d<distance){distance=d;best=n;}
  }
  return best;
 }
}
