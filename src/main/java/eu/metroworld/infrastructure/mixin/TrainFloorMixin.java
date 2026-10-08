package eu.metroworld.infrastructure.mixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Only our tagged floor proxies bypass the shulker's integer snapping and attachment AI. */
@Mixin(ShulkerEntity.class)
public abstract class TrainFloorMixin extends GolemEntity {
 protected TrainFloorMixin(EntityType<? extends GolemEntity> type,World world){super(type,world);}
 @Inject(method="setPosition",at=@At("HEAD"),cancellable=true)
 private void metroPosition(double x,double y,double z,CallbackInfo ci){if(getCommandTags().contains("metro-world:train-floor")){super.setPosition(x,y,z);ci.cancel();}}
 @Inject(method="tick",at=@At("HEAD"),cancellable=true)
 private void metroFloorTick(CallbackInfo ci){if(getCommandTags().contains("metro-world:train-floor"))ci.cancel();}
}
