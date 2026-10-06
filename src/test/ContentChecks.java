import eu.metroworld.infrastructure.world.*;
import java.util.*;
public final class ContentChecks {
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public static void main(String[] args){
  var botanical=new HashSet<BotanicalAnnex.Kind>();int cells=0;
  for(long salt=0;salt<1000;salt++){
   var a=BotanicalAnnex.spec(salt);botanical.add(a.kind());check(a.equals(BotanicalAnnex.spec(salt)),"unstable annex");
   check(a.offsetX()+a.halfWidth()<180&&Math.abs(a.offsetZ())+a.halfLength()<180,"annex outside station reservation");
   for(int x=-2;x<=2;x++)for(int z=-7;z<=a.halfLength();z++)for(int y=1;y<=3;y++){check(BotanicalAnnex.walkway(a,x,y,z),"blocked botanical access");cells++;}
  }
  check(botanical.size()==4,"missing botanical variants");
  var kinds=new HashSet<SideBranchDesign.Kind>();int branches=0;
  record XY(int x,int z){}
  for(int length:new int[]{28,40,54})for(int i=0;i<16;i++){
   var s=SideBranchDesign.select((long)i<<23,length);kinds.add(s.kind());check(SideBranchDesign.minX(s)>-180&&SideBranchDesign.maxZ(s)<180,"branch outside reservation");
   var queue=new ArrayDeque<XY>();var seen=new HashSet<XY>();queue.add(new XY(-length+6,12));
   while(!queue.isEmpty()){
    var v=queue.remove();if(!seen.add(v))continue;
    for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){var n=new XY(v.x+d[0],v.z+d[1]);if(!seen.contains(n)&&SideBranchDesign.interior(n.x,3,n.z,s))queue.add(n);}
   }
   check(seen.contains(new XY(-100,70)),"dead-end room unreachable");
   check(SideBranchDesign.interior(-114,3,80,s)&&SideBranchDesign.container(-114,3,80,s),"branch loot unreachable");
   if(s.kind()==SideBranchDesign.Kind.QUARANTINE)for(int x:new int[]{-111,-100,-89})check(SideBranchDesign.spawner(x,3,78,s)&&SideBranchDesign.interior(x,3,78,s),"invalid infected spawner");
   branches++;
  }
  check(kinds.size()==4,"missing branch types");
  System.out.println("CONTENT_OK: four botanical annexes, "+cells+" protected aisle cells; four dead-end types, "+branches+" reachable room layouts and quarantine spawner positions");
 }
}
