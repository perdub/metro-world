package eu.kessoku.infrastructure.mixin;
import eu.kessoku.infrastructure.InfrastructureBlocks;
import eu.kessoku.infrastructure.world.NetworkPlan;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChunkGenerator.class)
public abstract class EntranceGeneratorMixin {
 @Inject(method="generateFeatures",at=@At("TAIL"))
 private void kessoku$entrance(StructureWorldAccess world,Chunk chunk,StructureAccessor structures,CallbackInfo ci){
  if(!((Object)this instanceof NoiseChunkGenerator)||!world.toServerWorld().getRegistryKey().equals(World.OVERWORLD))return;
  var registry=world.getRegistryManager().get(RegistryKeys.STRUCTURE);
  java.util.Set<net.minecraft.structure.StructureStart> starts=new java.util.HashSet<>();
  for(int ox=-1;ox<=1;ox++)for(int oz=-1;oz<=1;oz++)starts.addAll(structures.getStructureStarts(new ChunkPos(chunk.getPos().x+ox,chunk.getPos().z+oz),s->{var id=registry.getId(s);if(id==null)return false;String name=id.getPath();return name.startsWith("village_")||name.equals("pillager_outpost")||name.equals("desert_pyramid")||name.equals("jungle_pyramid")||name.equals("igloo")||name.equals("mansion");}));
  for(var start:starts){
   var box=start.getBoundingBox();long h=NetworkPlan.hash(world.getSeed(),start.getPos().x,start.getPos().z,102);int side=(int)(h&3);
   int x=side==0?box.getMaxX()+14:side==1?box.getMinX()-14:(box.getMinX()+box.getMaxX())/2;
   int z=side==2?box.getMaxZ()+14:side==3?box.getMinZ()-14:(box.getMinZ()+box.getMaxZ())/2;
   x=Math.floorDiv(x,16)*16+8;z=Math.floorDiv(z,16)*16+8;
   // Each structure chooses one entrance; only its owning generation chunk writes it.
   if(Math.floorDiv(x,16)!=chunk.getPos().x||Math.floorDiv(z,16)!=chunk.getPos().z)continue;
   int y=world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x,z);
   if(y<world.getSeaLevel()||y>world.getTopY()-8)continue;
   BlockPos.Mutable pos=new BlockPos.Mutable();boolean clear=true;
   for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
    int top=world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,x+dx,z+dz);
    if(Math.abs(top-y)>1||!world.getFluidState(pos.set(x+dx,top-1,z+dz)).isEmpty())clear=false;
    for(int dy=0;dy<=4;dy++)if(!world.getBlockState(pos.set(x+dx,y+dy,z+dz)).isReplaceable())clear=false;
   }
   if(!clear)continue;
   for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
    world.setBlockState(pos.set(x+dx,y-1,z+dz),Blocks.SMOOTH_QUARTZ.getDefaultState(),2);
    world.setBlockState(pos.set(x+dx,y+4,z+dz),Blocks.SMOOTH_QUARTZ_SLAB.getDefaultState().with(SlabBlock.TYPE,SlabType.BOTTOM),2);
    if(Math.abs(dx)==3&&Math.abs(dz)==3)for(int dy=0;dy<4;dy++)world.setBlockState(pos.set(x+dx,y+dy,z+dz),InfrastructureBlocks.RIBBED_PANEL.getDefaultState(),2);
    else if((Math.abs(dx)==3||dz==3))for(int dy=1;dy<4;dy++)world.setBlockState(pos.set(x+dx,y+dy,z+dz),Blocks.CYAN_STAINED_GLASS.getDefaultState(),2);
   }
   world.setBlockState(pos.set(x,y,z+1),InfrastructureBlocks.LIFT.getDefaultState(),2);
   world.setBlockState(pos.set(x,y+3,z),Blocks.SEA_LANTERN.getDefaultState(),2);
   world.setBlockState(pos.set(x,y+3,z-3),InfrastructureBlocks.WAYFINDING_SIGN.getDefaultState(),2);
   world.setBlockState(pos.set(x-2,y,z+2),Blocks.GRASS_BLOCK.getDefaultState(),2);
   world.setBlockState(pos.set(x-2,y+1,z+2),Blocks.PINK_TULIP.getDefaultState(),2);
   org.slf4j.LoggerFactory.getLogger("btr_infrastructure").info("Вход метро рядом с {}: {}, {}, {}",registry.getId(start.getStructure()),x,y,z);
  }
 }
}
