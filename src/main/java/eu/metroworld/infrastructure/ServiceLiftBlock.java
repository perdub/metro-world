package eu.metroworld.infrastructure;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Recessed hatch terminal returns the player above the surface hatch. */
public final class ServiceLiftBlock extends SimplePolymerBlock {
 public ServiceLiftBlock(){super(AbstractBlock.Settings.copy(Blocks.BEDROCK),Blocks.BLACK_GLAZED_TERRACOTTA);}
 @Override protected ActionResult onUse(BlockState state,World world,BlockPos pos,PlayerEntity player,BlockHitResult hit){
  if(player instanceof ServerPlayerEntity p){if(Transit.isNetworkWorld(world))Transit.leave(p);else Transit.enter(p,pos.up(3));}
  return ActionResult.SUCCESS;
 }
}
