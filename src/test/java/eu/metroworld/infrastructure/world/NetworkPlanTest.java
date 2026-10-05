package eu.metroworld.infrastructure.world;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NetworkPlanTest {
 @Test void stationElevationsAndOffsetsVary(){Set<Integer> heights=new HashSet<>();int offGrid=0;for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){var n=NetworkPlan.node(619015,x,z);heights.add(n.y());if(Math.floorMod(n.x(),16)!=0||Math.floorMod(n.z(),16)!=0)offGrid++;}assertTrue(heights.size()>40);assertTrue(offGrid>65);}
 @Test void damageIsMinority(){int intact=0;for(int x=-16;x<16;x++)for(int z=-16;z<16;z++)if(NetworkPlan.condition(21,x,z,80)==NetworkPlan.Condition.INTACT)intact++;assertTrue(intact>650);assertTrue(intact<850);}
 @Test void everyStairTowerHasWholeEightStepFlights(){for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){var n=NetworkPlan.node(21,x,z);var plan=NetworkPlan.forChunk(21,n.x()+48,n.z()+12);assertFalse(plan.stairs().isEmpty());for(var t:plan.stairs()){assertEquals(0,(t.top()-t.bottom())%8);assertTrue(t.bottom()>-248);assertTrue(t.top()<240);}}}
 @Test void originStationAndServiceDoorsExist(){var plan=NetworkPlan.forChunk(619015,0,0);assertTrue(plan.stations().stream().anyMatch(s->s.node().equals(new NetworkPlan.Node(0,0,0))));assertTrue(NetworkPlan.forChunk(619015,-16,32).rooms().size()>0);}
 @Test void chunksShareTheSameRouteAcrossBorders(){var p=NetworkPlan.forChunk(7,48,0);var q=NetworkPlan.forChunk(7,64,0);assertFalse(p.paths().isEmpty());assertFalse(q.paths().isEmpty());assertTrue(p.paths().stream().anyMatch(a->q.paths().stream().anyMatch(b->b.route().points().equals(a.route().points()))));}
 @Test void interchangeGridCellHasTwoStationsAndSharedPedestrianPassage(){
  var a=NetworkPlan.node(619015,1,0);var b=new NetworkPlan.Node(a.x()+320,a.z(),a.y());
  var first=NetworkPlan.forChunk(619015,a.x()-16,a.z()-16);var second=NetworkPlan.forChunk(619015,b.x()-16,b.z()-16);
  int mx=(a.x()+b.x())/2,mz=a.z()+12;var passage=NetworkPlan.forChunk(619015,Math.floorDiv(mx,16)*16,Math.floorDiv(mz,16)*16);
  assertTrue(first.stations().stream().anyMatch(s->s.interchange()&&s.node().equals(a)));
  assertTrue(second.stations().stream().anyMatch(s->s.interchange()&&s.node().equals(b)));
  assertTrue(NetworkPlan.forChunk(619015,864,-32).stations().stream().anyMatch(s->s.interchange()&&s.node().equals(b)));
  assertTrue(passage.paths().stream().anyMatch(p->p.transfer()&&p.service()&&p.route().points().getFirst().x()==a.x()+34&&p.route().points().getLast().x()==b.x()-34&&p.route().points().getFirst().y()==a.y()+2&&p.route().points().getLast().y()==b.y()+2&&p.route().points().getFirst().z()==a.z()+12&&p.route().points().getLast().z()==b.z()+12));
  assertTrue(NetworkPlan.forChunk(619015,704,-16).paths().stream().anyMatch(NetworkPlan.Path::transfer));
 }
}
