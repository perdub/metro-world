package eu.kessoku.infrastructure;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
public final class InfrastructureMod implements ModInitializer {
 public void onInitialize(){
  InfrastructureBlocks.initialize();
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
   if(server.getTicks()%20!=0)return;
   for(var player:server.getPlayerManager().getPlayerList()){
    if(!Transit.isNetworkWorld(player.getWorld()))continue;
    if(player.getWorld().getBlockState(player.getBlockPos().down()).isOf(Blocks.MAGMA_BLOCK)&&!player.isSpectator()&&!player.isCreative()){
     player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.POISON,60,0));
     player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.NAUSEA,160,0));
    }
    if(server.getTicks()%1200==0)player.playSoundToPlayer(net.minecraft.sound.SoundEvents.AMBIENT_CAVE.value(),net.minecraft.sound.SoundCategory.AMBIENT,0.35f,0.85f);
   }
  });
  CommandRegistrationCallback.EVENT.register((dispatcher,registries,environment)->{
   dispatcher.register(CommandManager.literal("kessoku").requires(s->s.hasPermissionLevel(2))
    .then(CommandManager.literal("enter").executes(c->Transit.enter(c.getSource().getPlayerOrThrow())))
    .then(CommandManager.literal("exit").executes(c->Transit.leave(c.getSource().getPlayerOrThrow())))
    .then(CommandManager.literal("entrance").executes(c->{
     ServerPlayerEntity player=c.getSource().getPlayerOrThrow();
     if(Transit.isNetworkWorld(player.getWorld())){c.getSource().sendError(Text.literal("Создайте вход в обычном мире."));return 0;}
     BlockPos at=player.getBlockPos().add(0,0,3);
     // Small entrance terminal; explicitly placed by admin, no automatic surface edits.
     for(int x=-2;x<=2;x++)for(int z=-1;z<=1;z++)player.getServerWorld().setBlockState(at.add(x,-1,z),Blocks.SMOOTH_STONE.getDefaultState());
     player.getServerWorld().setBlockState(at,InfrastructureBlocks.LIFT.getDefaultState());
     player.getServerWorld().setBlockState(at.up(),Blocks.YELLOW_CONCRETE.getDefaultState());
     player.getServerWorld().setBlockState(at.up(2),Blocks.SEA_LANTERN.getDefaultState());
     c.getSource().sendFeedback(()->Text.literal("Вход установлен. ПКМ по чёрному блоку лифта переносит в метро; обратный лифт сохраняет точку входа."),false);return 1;
    })));
   dispatcher.register(CommandManager.literal("kessoku-network").executes(c->{c.getSource().sendFeedback(()->Text.literal("Метро 0.4.2 в отдельном измерении: /kessoku enter, /kessoku exit, /kessoku entrance. Высота −256…255; тоннели и переходы на разных высотах."),false);return 1;}));
  });
 }
}
