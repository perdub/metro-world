package eu.metroworld.infrastructure.world;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NetworkPlanTest {
 @Test void stationElevationsAndOffsetsVary(){Set<Integer> heights=new HashSet<>();int offGrid=0;for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){if(!StationGraph.exists(x,z))continue;var n=NetworkPlan.node(619015,x,z);heights.add(n.y());if(Math.floorMod(n.x(),16)!=0||Math.floorMod(n.z(),16)!=0)offGrid++;}assertTrue(heights.size()>15);assertTrue(offGrid>30);}
 @Test void damageIsMinority(){int intact=0;for(int x=-16;x<16;x++)for(int z=-16;z<16;z++)if(NetworkPlan.condition(21,x,z,80)==NetworkPlan.Condition.INTACT)intact++;assertTrue(intact>650);assertTrue(intact<850);}
 @Test void everyStairTowerHasWholeEightStepFlights(){for(int x=-4;x<=4;x++)for(int z=-3;z<=3;z+=3){var n=NetworkPlan.node(21,x,z);int offset=StationGraph.anchor(x,z)?72:66;var plan=NetworkPlan.forChunk(21,n.x()+offset,n.z()+12);assertFalse(plan.stairs().isEmpty());for(var t:plan.stairs()){assertEquals(0,(t.top()-t.bottom())%8);assertTrue(t.bottom()>-248);assertTrue(t.top()<240);}}}
 @Test void originHasTwoHallsAndSharedPublicStairs(){var plan=NetworkPlan.forChunk(619015,0,0);assertEquals(2,plan.stations().size());assertTrue(plan.stations().stream().anyMatch(s->s.portal()&&!s.northSouth()));assertFalse(NetworkPlan.forChunk(619015,64,0).stairs().isEmpty());}
 @Test void intermediateStopsEvenlyDividePortalEdges(){for(int x=-3;x<=3;x+=3){var a=StationGraph.node(7,x,0);var b=StationGraph.node(7,x+3,0);for(int step=1;step<=2;step++){var n=StationGraph.node(7,x+step,0);assertEquals(a.x()+(b.x()-a.x())*step/3.0,n.x(),.51);assertEquals(a.y()+(b.y()-a.y())*step/3.0,n.y(),.51);}}}
 @Test void chunksShareTheSameRouteAcrossBorders(){var p=NetworkPlan.forChunk(7,48,0);var q=NetworkPlan.forChunk(7,64,0);assertFalse(p.paths().isEmpty());assertFalse(q.paths().isEmpty());assertTrue(p.paths().stream().anyMatch(a->q.paths().stream().anyMatch(b->b.route().points().equals(a.route().points()))));}
}
