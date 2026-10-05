import eu.metroworld.infrastructure.world.NetworkPlan;
import java.util.*;
public class PlanChecks {
 static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[]args){
  for(int x=-1024;x<1024;x++)require(NetworkPlan.sample(99,x,41,6)==NetworkPlan.Material.RAIL_X,"Upper rail "+x);
  for(int z=-1024;z<1024;z++)require(NetworkPlan.sample(99,6,9,z)==NetworkPlan.Material.RAIL_Z,"Lower rail "+z);
  for(int y=9;y<=132;y++)require(NetworkPlan.sample(7,16,y,22)==NetworkPlan.Material.LADDER,"Ladder "+y);
  for(int x=-300;x<300;x++)require(NetworkPlan.sample(42,x,128,50)==NetworkPlan.Material.KEEP,"Surface "+x);
  Set<NetworkPlan.Condition> seen=new HashSet<>();for(int x=-2048;x<2048;x+=64)seen.add(NetworkPlan.condition(123,x,8,0));require(seen.size()==4,"Conditions");
  System.out.println("PASS: 2048 upper rail positions, 2048 lower rail positions, 124 shaft positions, 600 surface positions, four conditions.");
 }
}
