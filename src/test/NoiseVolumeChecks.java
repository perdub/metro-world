import eu.metroworld.infrastructure.world.*;
public final class NoiseVolumeChecks {
 private static void require(boolean v,String message){if(!v)throw new AssertionError(message);}
 public static void main(String[] args){
  int different=0,accepted=0;
  for(long seed:new long[]{7,619015})for(int x=-2400;x<=2400;x+=160)for(int z=-2400;z<=2400;z+=160){
   if(ExclusionNoise.blocked(seed,x,-160,z)!=ExclusionNoise.blocked(seed,x,120,z))different++;
   double y=NetworkPlan.hash(seed,x,z,6)%160;
   if(!ExclusionNoise.boxClear(seed,x,y,z,12,9,12))continue;accepted++;
   for(int dx=-12;dx<=12;dx+=3)for(int dy=-9;dy<=9;dy+=3)for(int dz=-12;dz<=12;dz+=3)require(!ExclusionNoise.blocked(seed,x+dx,y+dy,z+dz),"accepted volume intersects forbidden noise");
  }
  require(different>100,"obstacles are still vertical columns");require(accepted>100,"no free volumes");
  require(Math.abs(ExclusionNoise.sample(7,-.001,0,0)-ExclusionNoise.sample(7,.001,0,0))<.001,"X boundary discontinuity");
  require(Math.abs(ExclusionNoise.sample(7,0,-.001,0)-ExclusionNoise.sample(7,0,.001,0))<.001,"Y boundary discontinuity");
  System.out.println("NOISE_3D_OK: "+different+" height-dependent occupied columns, "+accepted+" fully clear boxes checked throughout their volumes");
 }
}
