package eu.kessoku.infrastructure.mixin;
import eu.kessoku.infrastructure.world.NetworkBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FlatChunkGenerator.class)
public abstract class FlatGeneratorMixin {
 @Inject(method="buildSurface", at=@At("TAIL"))
 private void kessoku$build(ChunkRegion region, StructureAccessor structures, NoiseConfig noise, Chunk chunk, CallbackInfo ci) {
  FlatChunkGenerator self = (FlatChunkGenerator)(Object)this;
  if (self.getConfig().getBiome().matchesKey(RegistryKey.of(RegistryKeys.BIOME, Identifier.of("btr_infrastructure", "metro"))))
   NetworkBuilder.build(chunk, region.getSeed());
 }
}
