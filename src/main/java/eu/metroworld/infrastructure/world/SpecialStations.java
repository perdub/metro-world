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
 public static BlockState garden(int x,int y,int z){
  double horizontal=Math.sqrt(x*x+z*z);
  if(y<0){
   if(horizontal>34)return null;
   return (y>=-3?Blocks.STONE:y>=-5?Blocks.OBSIDIAN:Blocks.BEDROCK).getDefaultState();
  }
  double radius=Math.sqrt(x*x+z*z+y*y);
  if(radius>34)return null;
  if(radius>=28)return (radius<31?Blocks.STONE:radius<33?Blocks.OBSIDIAN:Blocks.BEDROCK).getDefaultState();
  // Blue backdrop and white cloud bands are separated from the clear glass by a lit cavity.
  if(radius>=27){
   if(Math.floorMod(x+2*z,13)<3&&y>10)return Blocks.WHITE_CONCRETE.getDefaultState();
   if(Math.floorMod(x+z,7)==0)return Blocks.SEA_LANTERN.getDefaultState();
   return Blocks.LIGHT_BLUE_CONCRETE.getDefaultState();
  }
  if(radius>=24.2)return Blocks.AIR.getDefaultState();
  if(radius>=23.2)return (Math.floorMod(x,8)==0||Math.floorMod(z,8)==0?Blocks.WHITE_STAINED_GLASS:Blocks.GLASS).getDefaultState();
  boolean path=Math.abs(x)<=2||Math.abs(z)<=2||horizontal>=19;
  if(y==0)return (path?Blocks.SMOOTH_QUARTZ:Blocks.GRASS_BLOCK).getDefaultState();
  // Retro laboratory consoles and a red-and-white perimeter ribbon.
  if(horizontal>=21&&y==1)return Blocks.RED_CONCRETE.getDefaultState();
  if(horizontal>=21&&y==2)return Blocks.WHITE_CONCRETE.getDefaultState();
  if(x>=-18&&x<=-16&&z>=-6&&z<=6&&y==1)return Blocks.SMOOTH_QUARTZ.getDefaultState();
  if(x==-17&&Math.floorMod(z,3)==0&&Math.abs(z)<=6&&y==2)return Blocks.CYAN_GLAZED_TERRACOTTA.getDefaultState();
  // Four compact planted trees: trunks, shaped leafy crowns and flowering understory.
  for(int tx:new int[]{-11,11})for(int tz:new int[]{-11,11}){
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
