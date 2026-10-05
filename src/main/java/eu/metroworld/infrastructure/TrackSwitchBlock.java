package eu.metroworld.infrastructure;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.*;
/** Vanilla rail junctions have two active legs. This terminal selects the active pair. */
public final class TrackSwitchBlock extends SimplePolymerBlock {
 public TrackSwitchBlock(){super(AbstractBlock.Settings.copy(Blocks.BEDROCK),Blocks.YELLOW_GLAZED_TERRACOTTA);}
 @Override protected ActionResult onUse(BlockState state,World world,BlockPos pos,PlayerEntity player,BlockHitResult hit){
  if(!(player instanceof ServerPlayerEntity p))return ActionResult.SUCCESS;
  BlockPos best=null;List<RailShape> choices=List.of();double distance=Double.POSITIVE_INFINITY;
  for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-1;dy<=1;dy++){
   BlockPos rail=pos.add(dx,dy,dz);if(!world.getBlockState(rail).isOf(Blocks.RAIL))continue;
   var dirs=EnumSet.noneOf(Direction.class);
   for(Direction d:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST})
    if(world.getBlockState(rail.offset(d)).getBlock() instanceof AbstractRailBlock)dirs.add(d);
   if(dirs.size()<3)continue;
   var options=new ArrayList<RailShape>();
   if(dirs.contains(Direction.NORTH)&&dirs.contains(Direction.SOUTH))options.add(RailShape.NORTH_SOUTH);
   if(dirs.contains(Direction.EAST)&&dirs.contains(Direction.WEST))options.add(RailShape.EAST_WEST);
   if(dirs.contains(Direction.NORTH)&&dirs.contains(Direction.EAST))options.add(RailShape.NORTH_EAST);
   if(dirs.contains(Direction.NORTH)&&dirs.contains(Direction.WEST))options.add(RailShape.NORTH_WEST);
   if(dirs.contains(Direction.SOUTH)&&dirs.contains(Direction.EAST))options.add(RailShape.SOUTH_EAST);
   if(dirs.contains(Direction.SOUTH)&&dirs.contains(Direction.WEST))options.add(RailShape.SOUTH_WEST);
   double score=dx*dx+dz*dz+dy*dy;if(score<distance){distance=score;best=rail;choices=options;}
  }
  if(best==null){p.sendMessage(Text.literal("Рядом нет плоской стрелки с тремя направлениями."));return ActionResult.SUCCESS;}
  BlockState rail=world.getBlockState(best);RailShape next=choices.get(Math.floorMod(choices.indexOf(rail.get(RailBlock.SHAPE))+1,choices.size()));
  world.setBlockState(best,rail.with(RailBlock.SHAPE,next),Block.NOTIFY_LISTENERS);
  p.sendMessage(Text.literal("Стрелка переключена: "+next.asString()));return ActionResult.SUCCESS;
 }
}
