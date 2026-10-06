package eu.metroworld.infrastructure.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;

class DomeDesignTest {
 @Test void seededVariantsCoverKindsAndSizes(){
  var variants=new HashSet<String>();
  for(long salt=0;salt<1000;salt++){
   var spec=DomeDesign.spec(salt);
   assertEquals(spec,DomeDesign.spec(salt));
   assertTrue(spec.outerRadius()<=42);
   assertEquals(11,spec.outerRadius()-spec.innerRadius());
   assertEquals(-62,spec.offsetZ());
   variants.add(spec.kind()+"/"+spec.size());
  }
  assertEquals(6,variants.size());
 }
 @Test void AquariumWaterIsContainedAndCrossWalkStaysDry(){
  for(long salt=0;salt<100;salt++){
   var spec=DomeDesign.spec(salt);if(spec.kind()!=DomeDesign.Kind.AQUA)continue;
   int water=0;
   for(int x=-spec.innerRadius();x<=spec.innerRadius();x++)for(int z=-spec.innerRadius();z<=spec.innerRadius();z++){
    if(Math.abs(x)<=2||Math.abs(z)<=2){assertTrue(DomeDesign.dryPath(spec,x,z));assertFalse(DomeDesign.tankWall(spec,x,z));assertFalse(DomeDesign.water(spec,x,2,z));}
    if(!DomeDesign.water(spec,x,2,z))continue;water++;
    for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})
     assertTrue(DomeDesign.water(spec,x+d[0],2,z+d[1])||DomeDesign.tankWall(spec,x+d[0],z+d[1]));
    assertFalse(DomeDesign.water(spec,x,0,z));assertFalse(DomeDesign.water(spec,x,5,z));
   }
   assertTrue(water>0);
  }
 }
}
