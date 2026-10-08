package eu.metroworld.infrastructure;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorGeometryTest {
 @Test void slopeUsesWalkableHalfBlockStepsAndFlatLandings(){
  var p=new EscalatorGeometry.Plan(10,64,20,1,0,6,false);
  assertEquals(12,p.length());assertEquals(64,p.height(-.1));assertEquals(70,p.height(12));
  for(int step=0;step<12;step++)assertEquals(64+(step+1)*.5,p.height(step+.5));
 }
 @Test void onlyPlayersStandingOnTheActualRunAreCarried(){
  for(int[] axis:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
   var p=new EscalatorGeometry.Plan(-10,20,-20,axis[0],axis[1],4,false);
   double x=p.x()+.5+axis[0]*2,z=p.z()+.5+axis[1]*2;
   assertEquals(2.5,p.along(x,z));assertEquals(0,p.across(x,z));
   assertTrue(p.riding(x,p.height(2.5),z));
   assertFalse(p.riding(x,p.height(2.5)+1,z));
   assertFalse(p.riding(x-axis[1]*2,p.height(2.5),z+axis[0]*2));
   assertFalse(p.riding(p.x()+.5+axis[0]*8,p.y()+4,p.z()+.5+axis[1]*8));
  }
 }
 @Test void movingBeyondEitherLandingEndsActivation(){
  var up=new EscalatorGeometry.Plan(0,0,0,1,0,1,false);double next=up.advance(1.99);
  assertTrue(next>up.length());assertFalse(up.riding(next,up.height(next),.5));
  var down=new EscalatorGeometry.Plan(0,0,0,1,0,1,true);next=down.advance(.01);
  assertTrue(next<0);assertFalse(down.riding(next,down.height(next),.5));
 }
 @Test void physicalFloorAndPlayerLeadingEdgeAreDistinguished(){
  var p=new EscalatorGeometry.Plan(0,64,0,1,0,3,false);
  assertEquals(64.5,p.floorHeight(.5));assertEquals(64.5,p.height(.5));
  assertEquals(64.5,p.floorHeight(.85));assertEquals(65,p.height(.85));
  for(int step=0;step<p.length();step++)assertEquals(64+(step+1)*.5,p.floorHeight(step));
 }
 @Test void invalidOrientationsAndOversizedRunsAreRejected(){
  assertThrows(IllegalArgumentException.class,()->new EscalatorGeometry.Plan(0,0,0,1,1,4,false));
  assertThrows(IllegalArgumentException.class,()->new EscalatorGeometry.Plan(0,0,0,1,0,33,false));
 }
}
