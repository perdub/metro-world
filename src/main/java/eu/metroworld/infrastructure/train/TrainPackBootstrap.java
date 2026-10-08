package eu.metroworld.infrastructure.train;
import java.nio.file.*;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
/** Set the first-run default before AutoHost reads its config; preserve existing operator choices. */
public final class TrainPackBootstrap implements PreLaunchEntrypoint {
 public void onPreLaunch(){if(FabricLoader.getInstance().getEnvironmentType()!=net.fabricmc.api.EnvType.SERVER)return;
  Path file=FabricLoader.getInstance().getConfigDir().resolve("polymer/auto-host.json");if(Files.exists(file))return;
  try{Files.createDirectories(file.getParent());Files.writeString(file,"{\"enabled\":true}\n",StandardOpenOption.CREATE_NEW);}catch(FileAlreadyExistsException ignored){}catch(java.io.IOException e){throw new IllegalStateException("Cannot initialize train resource-pack hosting",e);}
 }
}
