import eu.metroworld.infrastructure.world.*;
import java.util.*;
public class RailGeometryChecks {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args){int cells=0,routes=0;
  for(int dy=-360;dy<=360;dy+=9)for(int direction=0;direction<2;direction++){
   var route=TransitGeometry.between(new TransitGeometry.Point(0,0,0),new TransitGeometry.Point(direction==0?512:35,dy,direction==0?71:512),direction,40,42);
   var plan=RailPlan.of(route);Set<String> occupied=new HashSet<>();
   for(var lane:plan.lanes()){
    check(lane.get(0).floor()==0,"start height");check(lane.get(lane.size()-1).floor()==dy,"arrival height dy="+dy+" dir="+direction);
    for(int i=0;i<lane.size();i++){
     var c=lane.get(i);cells++;
     check(occupied.add(c.x()+","+c.floor()+","+c.z()),"overlapping rails "+c+" dy="+dy+" dir="+direction);
     if(i==0)continue;var a=lane.get(i-1);
     check(Math.abs(c.x()-a.x())+Math.abs(c.z()-a.z())==1,"gap "+a+" -> "+c);
     check(Math.abs(c.floor()-a.floor())<=1,"vertical jump");
     if(c.floor()!=a.floor()){
      var lower=c.floor()<a.floor()?c:a;
      check(lower.shape().startsWith("ascending_"),"slope on curve "+lower);
     }
     if(i%64==0){var sample=route.nearest(c.x(),c.floor(),c.z());check(plan.floorAt(c.x(),c.z(),sample)==c.floor(),"floor mismatch "+c);}
    }
   }
   routes++;
  }
  System.out.println("PASS: "+routes+" routes, "+cells+" rail cells; face connections, arrival heights, no lane overlap, legal slopes and matching floor");
 }
}
