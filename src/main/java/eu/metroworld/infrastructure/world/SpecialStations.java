package eu.metroworld.infrastructure.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;

/** Freight platforms and a garden under an artificial sky; all coordinates are local. */
public final class SpecialStations {
 private SpecialStations() {}
 public static BlockState freight(int x,int y,int z,BlockState original){
  if(original==null)return null;
  int ax=Math.abs(x),az=Math.abs(z),bay=Math.floorMod(x+36,18);
  if(az==17&&y>=3&&y<=9&&ax<35){
   if(y==4)return Blocks.ORANGE_CONCRETE.getDefaultState();
   if(y==7&&Math.floorMod(x,8)<3)return Blocks.SEA_LANTERN.getDefaultState();
   return (Math.floorMod(x,8)==0?Blocks.WAXED_CUT_COPPER:Blocks.POLISHED_DEEPSLATE).getDefaultState();
  }
  if(y==3&&az==10&&Math.floorMod(x,18)==1)return Blocks.RED_GLAZED_TERRACOTTA.getDefaultState();
  if(y==2&&az>=6&&az<=15&&ax<38)return (az==6?Blocks.YELLOW_CONCRETE:Blocks.POLISHED_ANDESITE).getDefaultState();
  // Keep track lanes, the landing aisle and station connections clear.
  if(ax<30&&az>=11&&az<=14&&bay>=3&&bay<=7&&y>=3&&y<=5){
   if(y==3)return Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE,SlabType.TOP);
   if(y==4)return (bay==5?Blocks.CHEST:Blocks.BARREL).getDefaultState();
   if(y==5&&bay==5)return Blocks.AIR.getDefaultState();
   if(y==5&&bay>=4&&bay<=6)return Blocks.WAXED_CUT_COPPER.getDefaultState();
  }
  // Overhead gantry, uprights and suspended hooks above the loading bays.
  if(ax<32&&Math.floorMod(x,18)==0){
   if(az==15&&y>=3&&y<=10)return Blocks.IRON_BLOCK.getDefaultState();
   if(y==10&&az<=15)return (az%3==0?Blocks.YELLOW_CONCRETE:Blocks.BLACK_CONCRETE).getDefaultState();
   if(az==8&&y>=7&&y<=9)return Blocks.CHAIN.getDefaultState();
  }
  if(y==3&&az==9&&Math.floorMod(x,18)==9)return Blocks.LANTERN.getDefaultState();
  return original;
 }
 public static BlockState garden(int x,int y,int z){return garden(x,y,z,DomeDesign.legacy());}
 public static BlockState garden(int x,int y,int z,DomeDesign.Spec spec){
  double horizontal=Math.hypot(x,z),radius=Math.sqrt((double)x*x+(double)z*z+(double)y*y);
  int inner=spec.innerRadius(),outer=spec.outerRadius();
  if(y<0){
   if(horizontal>outer)return null;
   return (y>=-3?Blocks.STONE:y>=-5?Blocks.OBSIDIAN:Blocks.BEDROCK).getDefaultState();
  }
  if(radius>outer)return null;
  if(radius>=inner+5)return (radius<outer-3?Blocks.STONE:radius<outer-1?Blocks.OBSIDIAN:Blocks.BEDROCK).getDefaultState();
  if(radius>=inner+4){
   if(spec.kind()==DomeDesign.Kind.AQUA)return (Math.floorMod(x+z,9)==0?Blocks.SEA_LANTERN:Blocks.BLUE_CONCRETE).getDefaultState();
   if(Math.floorMod(x+2*z,13)<3&&y>inner/2)return Blocks.WHITE_CONCRETE.getDefaultState();
   return (Math.floorMod(x+z,7)==0?Blocks.SEA_LANTERN:Blocks.LIGHT_BLUE_CONCRETE).getDefaultState();
  }
  if(radius>=inner+1.2)return Blocks.AIR.getDefaultState();
  if(radius>=inner+.2)return (Math.floorMod(x,8)==0||Math.floorMod(z,8)==0?Blocks.WHITE_STAINED_GLASS:Blocks.GLASS).getDefaultState();
  boolean path=DomeDesign.dryPath(spec,x,z);
  if(y==0)return (path?Blocks.SMOOTH_QUARTZ:spec.kind()==DomeDesign.Kind.AQUA?(Math.floorMod(x*11+z*23,17)==0?Blocks.SEA_LANTERN:Math.floorMod(x+z,5)==0?Blocks.PRISMARINE:Blocks.MOSS_BLOCK):Blocks.GRASS_BLOCK).getDefaultState();
  if(spec.kind()==DomeDesign.Kind.AQUA){
   // Four sealed tanks leave a dry cross and a dry perimeter promenade.
   if(DomeDesign.tankWall(spec,x,z)&&y<=5)return Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
   if(DomeDesign.water(spec,x,y,z)){
    if(Math.floorMod(x*31+z*17,19)==0&&y<=3)return (y==3?Blocks.KELP:Blocks.KELP_PLANT).getDefaultState();
    return Blocks.WATER.getDefaultState();
   }
   if(!path&&y==1&&Math.floorMod(x*31+z*17,13)==0)return Blocks.PRISMARINE.getDefaultState();
   if(path&&y==1&&Math.abs(x)==2&&Math.floorMod(z,7)==0)return Blocks.SEA_LANTERN.getDefaultState();
   return Blocks.AIR.getDefaultState();
  }
  if(horizontal>=inner-2&&y==1)return Blocks.RED_CONCRETE.getDefaultState();
  if(horizontal>=inner-2&&y==2)return Blocks.WHITE_CONCRETE.getDefaultState();
  int tree=inner/2;
  for(int tx:new int[]{-tree,tree})for(int tz:new int[]{-tree,tree}){
   int dx=x-tx,dz=z-tz;
   if(dx==0&&dz==0&&y>=1&&y<=5)return Blocks.OAK_LOG.getDefaultState();
   if(y>=4&&y<=8&&dx*dx+dz*dz+(y-6)*(y-6)<=12)return Blocks.OAK_LEAVES.getDefaultState().with(net.minecraft.block.LeavesBlock.PERSISTENT,true);
  }
  if(!path&&y==1&&Math.floorMod(x*31+z*17,11)==0)return (Math.floorMod(x+z,3)==0?Blocks.PINK_TULIP:Math.floorMod(x+z,3)==1?Blocks.FERN:Blocks.OXEYE_DAISY).getDefaultState();
  if(y==1&&Math.abs(x)==3&&Math.floorMod(z,8)==0)return Blocks.SEA_LANTERN.getDefaultState();
  if(y==1&&Math.abs(x)==4&&z>=5&&z<=8)return Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE,SlabType.BOTTOM);
  return Blocks.AIR.getDefaultState();
 }
}
