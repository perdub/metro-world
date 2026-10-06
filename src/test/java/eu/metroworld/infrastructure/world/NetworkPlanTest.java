package eu.metroworld.infrastructure.world;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NetworkPlanTest {
 @Test void placementIsRepeatableAndRespectsNoise(){var vs=StationGraph.vertices(7,0,0);assertEquals(vs,StationGraph.vertices(7,0,0));for(var v:vs){assertTrue(ExclusionNoise.stationClear(7,v.node().x(),v.node().y(),v.node().z()));for(var w:vs)if(!v.equals(w))assertTrue(Math.hypot(v.node().x()-w.node().x(),v.node().z()-w.node().z())>=600);}}
 @Test void changingSeedChangesLayout(){assertNotEquals(StationGraph.vertices(7,0,0),StationGraph.vertices(8,0,0));}
 @Test void noiseIsContinuousAcrossNegativeCoordinates(){assertEquals(ExclusionNoise.sample(7,-.001,0,0),ExclusionNoise.sample(7,.001,0,0),.001);}
 @Test void routesAndTrafficRespectReservations(){for(var e:StationGraph.edges(7,0,0)){assertNotNull(NoiseRouter.connect(7,e.a(),e.b()));assertTrue(e.a().traffic()==StationGraph.Traffic.MIXED||e.b().traffic()==StationGraph.Traffic.MIXED||e.a().traffic()==e.b().traffic());}}
 @Test void locatorFindsGeneratedStation(){var s=NetworkPlan.nearestStation(7,0,0,"station");assertNotNull(s);var n=s.node();assertTrue(NetworkPlan.forChunk(7,Math.floorDiv(n.x(),16)*16,Math.floorDiv(n.z(),16)*16).stations().stream().anyMatch(h->h.node().equals(n)&&h.northSouth()==s.northSouth()));}
}
