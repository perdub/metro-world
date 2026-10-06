package eu.metroworld.infrastructure.world;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StationVariantTest {
 @Test void smallStationsNeverReceiveUnreachableMezzanines(){
  for(long seed=0;seed<256;seed++){
   assertEquals(StationVariant.COMPACT_SIDE,StationVariant.select(seed<<20,28,12,10,0));
   assertFalse(StationVariant.select(seed<<20,40,17,10,0).mezzanine());
  }
 }
 @Test void largerStationsHaveDifferentCirculationLayouts(){
  assertEquals(StationVariant.VAULTED_SIDE,StationVariant.select(0,40,17,14,0));
  assertEquals(StationVariant.GALLERY,StationVariant.select(1L<<20,40,17,14,0));
  assertEquals(StationVariant.DISTRIBUTION_HALL,StationVariant.select(2L<<20,40,17,14,0));
  assertEquals(StationVariant.ISLAND_BRIDGE,StationVariant.select(2L<<20,40,17,14,4));
  assertFalse(StationVariant.VAULTED_SIDE.mezzanine());
  assertTrue(StationVariant.GALLERY.mezzanine());
  assertTrue(StationVariant.DISTRIBUTION_HALL.overheadBridge());
  assertFalse(StationVariant.GALLERY.overheadBridge());
 }
 @Test void bothVehicleLanesHaveProtectedThreeBlockEnvelope(){
  for(int expansion:new int[]{0,4})for(int direction:new int[]{-1,1})for(int y=1;y<=3;y++)for(int d:new int[]{0})
   assertTrue(StationVariant.vehicleClearance(y,direction*(3+expansion)+d,expansion));
  assertFalse(StationVariant.vehicleClearance(4,3,0));
  assertFalse(StationVariant.vehicleClearance(2,0,4));
 }
 @Test void roofProfilesRemainSymmetricAndLeaveUsableHeadroom(){
  for(var variant:StationVariant.values())for(int z=-17;z<=17;z++){
   assertEquals(variant.roof(z,17,14),variant.roof(-z,17,14));
   assertTrue(variant.roof(z,17,14)>=12);
  }
  assertNotEquals(StationVariant.COMPACT_SIDE.roof(17,17,14),StationVariant.VAULTED_SIDE.roof(17,17,14));
 }
}
