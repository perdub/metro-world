package eu.metroworld.infrastructure.train;
import java.util.*;
import eu.metroworld.infrastructure.Transit;
import eu.metroworld.infrastructure.world.StationGraph;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
/** Integration checks create a temporary real train and always dispose it. */
public final class TrainValidation {
 private TrainValidation(){}
 public static int run(ServerCommandSource source){var world=source.getServer().getWorld(Transit.UNDERGROUND);if(world==null)return 0;Trains.Runtime test=null;
  try{var edge=StationGraph.edges(world.getSeed(),0,0).stream().findFirst().orElseThrow();test=Trains.create(world,edge,edge.freight()?"freight":"blue");if(test==null)throw new IllegalStateException("validation track already occupied");test.show(world);int parts=test.render.stream().mapToInt(r->r.parts.size()).sum();if(parts<40)throw new IllegalStateException("missing body/floor/seat proxies");for(var render:test.render)for(var part:render.parts)if(world.getEntity(part.entity().getUuid())==null)throw new IllegalStateException("render entity not spawned");test.cargo.get(0).setStack(0,new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND,7));test.encodeCargo(world);var gson=new com.google.gson.Gson();var saved=gson.fromJson(gson.toJson(test.data),TrainData.class);var restored=new Trains.Runtime(saved,world);if(restored.cargo.get(0).getStack(0).getCount()!=7||!restored.cargo.get(0).getStack(0).isOf(net.minecraft.item.Items.DIAMOND))throw new IllegalStateException("cargo serialization lost items");
   var c=test.data.control;c.leftDoors=false;c.rightDoors=false;c.power=true;c.compressor=true;c.brake=0;c.traction=3;double start=c.position;for(int i=0;i<100;i++)c.tick(.05,test.data.length()+2,test.path.length-2,0);if(c.position<=start||c.speed<=0)throw new IllegalStateException("controller did not move train");double speed=c.speed;c.emergency=true;for(int i=0;i<100;i++)c.tick(.05,test.data.length()+2,test.path.length-2,0);if(c.speed!=0)throw new IllegalStateException("emergency failed");var pose=test.path.at(start);for(var render:test.render)render.move(pose);source.sendFeedback(()->Text.literal("TRAINS_OK: "+parts+" actual render/floor/interaction entities, traction, emergency stop, real item inventory round-trip; live walking and GUI require a player"),false);return parts;
  }catch(Exception e){source.sendError(Text.literal("TRAINS_FAILED: "+e.getClass().getSimpleName()+": "+e.getMessage()));return 0;}
  finally{if(test!=null){test.hide();Trains.TRAINS.remove(test.data.id);}}
 }
}
