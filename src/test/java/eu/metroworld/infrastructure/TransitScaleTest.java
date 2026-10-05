package eu.metroworld.infrastructure;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransitScaleTest {
 @Test void reverseNetherRatioWorksOnBothSidesOfOrigin(){
  for(double x:new double[]{-1000,-64,-0.5,0,0.5,64,1000})assertEquals(x,TransitScale.toOverworld(TransitScale.toMetro(x)),1e-9);
  assertEquals(800,TransitScale.toMetro(100));assertEquals(-100,TransitScale.toOverworld(-800));
 }
 @Test void stationCellsFollowScaledCoordinatesAndNegativeBoundaries(){
  assertEquals(1,TransitScale.stationCell(TransitScale.toMetro(64)));
  assertEquals(-1,TransitScale.stationCell(TransitScale.toMetro(-64)));
  assertEquals(0,TransitScale.stationCell(TransitScale.toMetro(-32)));
  assertEquals(-1,TransitScale.stationCell(TransitScale.toMetro(-32.01)));
 }
}
