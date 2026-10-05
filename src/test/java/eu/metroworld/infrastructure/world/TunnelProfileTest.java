package eu.metroworld.infrastructure.world;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TunnelProfileTest {
 @Test void routeSeedsCoverEveryMainAndServiceFamily(){
  Set<TunnelProfile.Style> found=new HashSet<>();
  for(int i=0;i<5;i++)found.add(TunnelProfile.section((long)i<<9,false,false,30,100).style());
  assertEquals(5,found.size());
  for(int i=0;i<4;i++)found.add(TunnelProfile.section((long)i<<9,true,false,30,100).style());
  assertEquals(EnumSet.allOf(TunnelProfile.Style.class),found);
 }
 @Test void compactGalleryHasExactlyTwoWalkableColumnsAndTwoBlocksOfHeadroom(){
  var section=TunnelProfile.section(3L<<9,true,false,24,54);
  assertTrue(section.compact());int open=0;
  for(int offset=-4;offset<=4;offset++){
   var sample=new TransitGeometry.Sample(Math.abs(offset),0,0,1,24,offset);
   double distance=section.distance(sample);
   if(distance<=section.halfWidth()-0.85){open++;assertEquals(3,section.roof(distance));}
  }
  assertEquals(2,open);
  assertFalse(TunnelProfile.section(3L<<9,true,false,4,54).compact());
  assertFalse(TunnelProfile.section(3L<<9,true,false,50,54).compact());
  assertFalse(TunnelProfile.section(3L<<9,true,false,7,14).compact());
 }
 @Test void dimServiceLightsAreMuchSparserThanCleanTunnelLights(){
  var bright=TunnelProfile.section(4L<<9,false,false,40,160);
  var dim=TunnelProfile.section(2L<<9,true,false,40,160);int a=0,b=0;
  for(int i=0;i<160;i++){if(bright.lamp(i,false))a++;if(dim.lamp(i,false))b++;}
  assertTrue(a>=b*8);
 }
}
