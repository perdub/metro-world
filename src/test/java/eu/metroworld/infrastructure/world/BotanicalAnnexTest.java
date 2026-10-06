package eu.metroworld.infrastructure.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.EnumSet;

class BotanicalAnnexTest {
 @Test void allKindsRepeatAndFitStationReserve(){
  var kinds=EnumSet.noneOf(BotanicalAnnex.Kind.class);
  for(long salt=0;salt<1000;salt++){
   var spec=BotanicalAnnex.spec(salt);assertEquals(spec,BotanicalAnnex.spec(salt));kinds.add(spec.kind());
   assertTrue(Math.abs(spec.offsetX())+spec.halfWidth()<180);
   assertTrue(Math.abs(spec.offsetZ())+spec.halfLength()<180);
   assertTrue(BotanicalAnnex.contains(spec,6,1,-7));
   assertFalse(BotanicalAnnex.walkway(spec,6,1,-7));
   assertEquals(0,BotanicalAnnex.shellLayer(spec,6,1,-7));
  }
  assertEquals(4,kinds.size());
 }
 @Test void southDoorConnectsThroughAllShellLayers(){
  var spec=BotanicalAnnex.spec(19);
  for(int x=-2;x<=2;x++)for(int z=-7;z<=12;z++)for(int y=1;y<=3;y++)
   assertTrue(BotanicalAnnex.walkway(spec,x,y,z));
  assertFalse(BotanicalAnnex.contains(spec,11,1,0));
  assertFalse(BotanicalAnnex.contains(spec,0,13,0));
  assertFalse(BotanicalAnnex.contains(spec,0,-4,0));
  assertEquals(3,BotanicalAnnex.shellLayer(spec,10,4,0));
  assertEquals(2,BotanicalAnnex.shellLayer(spec,9,4,0));
  assertEquals(1,BotanicalAnnex.shellLayer(spec,8,4,0));
 }
}
