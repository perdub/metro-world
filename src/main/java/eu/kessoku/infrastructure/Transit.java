package eu.kessoku.infrastructure;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.registry.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class Transit {
 public static final RegistryKey<World> UNDERGROUND=RegistryKey.of(RegistryKeys.WORLD,Identifier.of("btr_infrastructure","metro"));
 public static boolean isNetworkWorld(net.minecraft.world.World w){var id=w.getRegistryKey().getValue();return id.getNamespace().equals("btr_infrastructure")&&(id.getPath().equals("metro")||id.getPath().equals("underground"));}
 private static Path file(ServerPlayerEntity p){return p.getServer().getSavePath(WorldSavePath.ROOT).resolve("kessoku-return.properties");}
 private static Properties read(ServerPlayerEntity p)throws IOException{
  Properties props=new Properties();Path legacy=file(p).resolveSibling("btr-return.properties");
  if(Files.exists(legacy))try(var in=Files.newInputStream(legacy)){props.load(in);}
  Path path=file(p);if(Files.exists(path))try(var in=Files.newInputStream(path)){props.load(in);}return props;
 }
 public static int enter(ServerPlayerEntity p){
  if(p.getWorld().getRegistryKey().equals(UNDERGROUND))return 1;
  if(isNetworkWorld(p.getWorld()))leave(p);
  ServerWorld target=p.getServer().getWorld(UNDERGROUND);
  if(target==null){p.sendMessage(Text.literal("Измерение метро не загружено: требуется перезапуск сервера."));return 0;}
  try{
   Properties props=read(p);props.setProperty(p.getUuidAsString(),p.getWorld().getRegistryKey().getValue()+","+p.getX()+","+p.getY()+","+p.getZ()+","+p.getYaw()+","+p.getPitch());
   Path tmp=file(p).resolveSibling("kessoku-return.properties.tmp");
   try(var out=Files.newOutputStream(tmp)){props.store(out,"Kessoku return positions");}
   Files.move(tmp,file(p),StandardCopyOption.REPLACE_EXISTING);
  }catch(IOException e){p.sendMessage(Text.literal("Не удалось сохранить точку возвращения; переход отменён."));return 0;}
  int gx=(int)Math.floor((p.getX()+256)/512),gz=(int)Math.floor((p.getZ()+256)/512);
  var station=eu.kessoku.infrastructure.world.NetworkPlan.node(target.getSeed(),gx,gz);
  int arrivalX=station.x()+2;
  target.getChunk(Math.floorDiv(arrivalX,16),Math.floorDiv(station.z()+8,16));
  p.teleport(target,arrivalX+0.5,station.y()+3,station.z()+8.5,90,0);
  p.sendMessage(Text.literal("Метро: пересадочная станция. Лифт возвращения — слева, чёрно-жёлтый блок. Высоты станций и тоннелей меняются по пути."));
  return 1;
 }
 public static int leave(ServerPlayerEntity p){
  if(!isNetworkWorld(p.getWorld()))return 1;
  try{
   String saved=read(p).getProperty(p.getUuidAsString());
   if(saved!=null){String[] s=saved.split(",");ServerWorld w=p.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD,Identifier.of(s[0])));
    if(w!=null){p.teleport(w,Double.parseDouble(s[1]),Double.parseDouble(s[2]),Double.parseDouble(s[3]),Float.parseFloat(s[4]),Float.parseFloat(s[5]));return 1;}
   }
  }catch(IOException|IllegalArgumentException e){p.sendMessage(Text.literal("Точка возвращения недоступна; возвращение к спавну."));}
  ServerWorld w=p.getServer().getOverworld();BlockPos spawn=w.getSpawnPos();
  p.teleport(w,spawn.getX()+0.5,spawn.getY()+1,spawn.getZ()+0.5,0,0);return 1;
 }
 public static void toggle(ServerPlayerEntity p){if(isNetworkWorld(p.getWorld()))leave(p);else enter(p);}
}
