import eu.metroworld.infrastructure.world.DecayPlan;
import eu.metroworld.infrastructure.world.NetworkPlan;
import java.util.*;
public final class StationDecayChecks {
 static void require(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  for(var condition:NetworkPlan.Condition.values())for(int expansion:new int[]{0,4}){
   for(int x=-40;x<=40;x++)for(int z=-17;z<=17;z++)for(int y=0;y<=14;y++){
    var mark=DecayPlan.mark(x,y,z,40,17,14,1L<<19,condition,expansion);
    if(DecayPlan.protectedPassage(x,y,z,40,17,expansion))require(mark==DecayPlan.Mark.KEEP);
    if(condition==NetworkPlan.Condition.INTACT)require(mark==DecayPlan.Mark.KEEP);
   }
  }
  var found=new EnumMap<NetworkPlan.Condition,Set<DecayPlan.Mark>>(NetworkPlan.Condition.class);
  for(var condition:NetworkPlan.Condition.values()){
   var marks=EnumSet.noneOf(DecayPlan.Mark.class);
   for(int x=-54;x<=54;x++)for(int z=-22;z<=22;z++)for(int y=0;y<=18;y++)marks.add(DecayPlan.mark(x,y,z,54,22,18,0,condition,0));
   found.put(condition,marks);
  }
  require(found.get(NetworkPlan.Condition.COLLAPSED).containsAll(EnumSet.of(DecayPlan.Mark.MISSING_PANEL,DecayPlan.Mark.BROKEN_PANEL,DecayPlan.Mark.DARK_LAMP,DecayPlan.Mark.RUBBLE,DecayPlan.Mark.WEB)));
  require(found.get(NetworkPlan.Condition.QUARANTINE).containsAll(EnumSet.of(DecayPlan.Mark.HAZARD,DecayPlan.Mark.YELLOW_GLASS,DecayPlan.Mark.RED_LIGHT)));
  require(!found.get(NetworkPlan.Condition.CHEMICAL).contains(DecayPlan.Mark.MISSING_PANEL));
  System.out.println("STATION_DECAY_OK: intact preservation, protected routes for both layouts, collapse/quarantine/chemical feature masks");
 }
}
