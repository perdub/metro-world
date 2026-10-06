import eu.metroworld.infrastructure.world.DomeDesign;
import java.util.*;
public final class DomeChecks {
 static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  var variants=new HashSet<String>();int water=0;
  for(long salt=0;salt<1000;salt++){
   var spec=DomeDesign.spec(salt);check(spec.equals(DomeDesign.spec(salt)));variants.add(spec.kind()+"/"+spec.size());
   if(salt>100||spec.kind()!=DomeDesign.Kind.AQUA)continue;
   for(int x=-spec.innerRadius();x<=spec.innerRadius();x++)for(int z=-spec.innerRadius();z<=spec.innerRadius();z++){
    if(Math.abs(x)<=2||Math.abs(z)<=2)check(!DomeDesign.water(spec,x,2,z)&&!DomeDesign.tankWall(spec,x,z));
    if(!DomeDesign.water(spec,x,2,z))continue;water++;
    for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})check(DomeDesign.water(spec,x+d[0],2,z+d[1])||DomeDesign.tankWall(spec,x+d[0],z+d[1]));
   }
  }
  check(variants.size()==6&&water>10000);System.out.println("DOMES_OK: six seeded types/sizes, "+water+" sealed aquarium columns, dry crossing");
 }
}
