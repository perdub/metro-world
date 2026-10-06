package eu.metroworld.infrastructure.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.LeavesBlock;

/** Botanical annex block sampler; no entities and no required client mod. */
public final class BotanicalStructures {
 private BotanicalStructures() {}
 public static BlockState sample(int x,int y,int z,BotanicalAnnex.Spec spec){
  if(!BotanicalAnnex.contains(spec,x,y,z))return null;
  // The connector pierces all shell layers; decorating never blocks this aisle.
  if(BotanicalAnnex.walkway(spec,x,y,z))return Blocks.AIR.getDefaultState();
  int shell=BotanicalAnnex.shellLayer(spec,x,y,z);
  if(shell>0)return (shell==3?Blocks.BEDROCK:shell==2?Blocks.OBSIDIAN:Blocks.STONE).getDefaultState();
  boolean irrigation=spec.kind()==BotanicalAnnex.Kind.GREENHOUSE&&Math.abs(x)==5&&(z==-4||z==4);
  if(y==0){
   if(irrigation)return Blocks.WATER.getDefaultState();
   if(Math.abs(x)<=2)return Blocks.SMOOTH_QUARTZ.getDefaultState();
   if(spec.kind()==BotanicalAnnex.Kind.GREENHOUSE&&Math.abs(x)>=4&&Math.abs(x)<=6&&z>=-6&&z<=5)
    return Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE,7);
   if(spec.kind()==BotanicalAnnex.Kind.OVERGROWN_NURSERY)return Blocks.GRASS_BLOCK.getDefaultState();
   return (Math.floorMod(x+z,5)==0?Blocks.GREEN_CONCRETE:Blocks.SMOOTH_QUARTZ).getDefaultState();
  }
  if(y==9){
   if(spec.kind()==BotanicalAnnex.Kind.GREENHOUSE)return (x%4==0?Blocks.SEA_LANTERN:Blocks.GLASS).getDefaultState();
   return (Math.floorMod(x+z,7)==0?Blocks.SEA_LANTERN:Blocks.WHITE_CONCRETE).getDefaultState();
  }
  if(Math.abs(x)==7||Math.abs(z)==9)return (y==5&&z%4==0?Blocks.SEA_LANTERN:Blocks.WHITE_CONCRETE).getDefaultState();
  if(BotanicalAnnex.chest(x,y,z))return Blocks.CHEST.getDefaultState();
  // Both blocks over the loot chest stay clear, allowing its lid to open.
  if(x==6&&z==-7&&y>=2&&y<=3)return Blocks.AIR.getDefaultState();
  switch(spec.kind()){
   case GREENHOUSE -> {
    if(irrigation&&y==1)return Blocks.AIR.getDefaultState();
    if(y==1&&Math.abs(x)>=4&&Math.abs(x)<=6&&z>=-6&&z<=5)
     return (x>0?Blocks.WHEAT:Blocks.CARROTS).getDefaultState().with(CropBlock.AGE,7);
    if(y==1&&Math.abs(x)==3&&z==-6)return Blocks.COMPOSTER.getDefaultState();
    if(y==1&&Math.abs(x)==5&&z==7)return Blocks.BARREL.getDefaultState();
   }
   case SEED_BANK -> {
    if(Math.abs(x)>=4&&Math.abs(x)<=6&&z>=-6&&z<=5){
     if(y==1||y==4||y==7)return Blocks.SMOOTH_STONE.getDefaultState();
     if((y==2||y==5)&&Math.floorMod(z,3)==0)return Blocks.BARREL.getDefaultState();
     if((y==3||y==6)&&Math.abs(x)==6&&Math.floorMod(z,3)==0)return Blocks.GREEN_STAINED_GLASS.getDefaultState();
    }
   }
   case LABORATORY -> {
    if(Math.abs(x)>=4&&Math.abs(x)<=6&&z>=-6&&z<=5){
     if(y==1)return Blocks.SMOOTH_QUARTZ.getDefaultState();
     if(y==2&&Math.abs(x)==5&&Math.floorMod(z,3)==0)return Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
     if(y==2&&Math.abs(x)==6&&Math.floorMod(z,3)==1)return Blocks.BREWING_STAND.getDefaultState();
    }
    if(x==-5&&z==7&&y==1)return Blocks.CAULDRON.getDefaultState();
   }
   case OVERGROWN_NURSERY -> {
    if(y==1&&Math.abs(x)>=4&&z>=-6&&z<=5&&Math.floorMod(x*31+z*17,7)==0)return Blocks.FERN.getDefaultState();
    if(Math.abs(x)>=5&&z>=-5&&z<=5&&y>=2&&y<=5&&Math.floorMod(x*13+z*7+y,5)<2)
     return Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT,true);
    if(x==-4&&z==-5&&y>=1&&y<=4)return Blocks.OAK_LOG.getDefaultState();
    if(y==1&&Math.abs(x)==4&&z==5)return Blocks.FLOWERING_AZALEA.getDefaultState();
   }
  }
  return Blocks.AIR.getDefaultState();
 }
}
