package eu.metroworld.infrastructure.world;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
/** Surface damage overlays; shell and all transport/walking connections remain owned by the caller. */
public final class StationDecay {
 private StationDecay(){}
 public static BlockState decorate(int x,int y,int z,int length,int width,int height,long salt,NetworkPlan.Condition condition,BlockState original){
  return decorate(x,y,z,length,width,height,salt,condition,0,original);
 }
 public static BlockState decorate(int x,int y,int z,int length,int width,int height,long salt,NetworkPlan.Condition condition,int expansion,BlockState original){
  if(original==null)return null;
  return switch(DecayPlan.mark(x,y,z,length,width,height,salt,condition,expansion)){
   case KEEP -> original;
   case MISSING_PANEL -> Blocks.AIR.getDefaultState();
   case BROKEN_PANEL -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
   case RUBBLE -> original.isAir()?Blocks.COBBLESTONE_SLAB.getDefaultState().with(SlabBlock.TYPE,SlabType.BOTTOM):original;
   case WEB -> original.isAir()?Blocks.COBWEB.getDefaultState():original;
   case HAZARD -> (Math.floorMod(x+y,4)<2?Blocks.YELLOW_CONCRETE:Blocks.BLACK_CONCRETE).getDefaultState();
   case YELLOW_GLASS -> Blocks.YELLOW_STAINED_GLASS.getDefaultState();
   case RED_LIGHT -> Blocks.SHROOMLIGHT.getDefaultState();
   case DARK_LAMP -> original.isOf(Blocks.SEA_LANTERN)||original.isOf(Blocks.GLOWSTONE)?Blocks.GRAY_CONCRETE.getDefaultState():original;
  };
 }
}
