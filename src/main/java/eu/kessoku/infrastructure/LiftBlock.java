package eu.kessoku.infrastructure;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class LiftBlock extends SimplePolymerBlock {
 public LiftBlock(){super(AbstractBlock.Settings.copy(Blocks.BEDROCK),Blocks.BLACK_GLAZED_TERRACOTTA);}
 @Override protected ActionResult onUse(BlockState state,World world,BlockPos pos,PlayerEntity player,BlockHitResult hit){
  if(player instanceof ServerPlayerEntity serverPlayer)Transit.toggle(serverPlayer);
  return ActionResult.SUCCESS;
 }
}
