package eu.metroworld.infrastructure;
import eu.metroworld.infrastructure.world.*;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import java.util.*;
/** Validate actual generated blocks on the central east/west and transverse connections, not just its plan. */
public final class TrackValidation {
 /** Rail placement depends on the support face, not the redstone-conduction predicate. */
 private static boolean supported(net.minecraft.world.WorldView world,BlockPos at,BlockState state){
  if(!(state.getBlock() instanceof AbstractRailBlock)||!state.canPlaceAt(world,at))return false;
  RailShape shape=state.isOf(Blocks.POWERED_RAIL)?state.get(PoweredRailBlock.SHAPE):state.get(RailBlock.SHAPE);
  Direction uphill=switch(shape){case ASCENDING_EAST->Direction.EAST;case ASCENDING_WEST->Direction.WEST;case ASCENDING_NORTH->Direction.NORTH;case ASCENDING_SOUTH->Direction.SOUTH;default->null;};
  return uphill==null||Block.sideCoversSmallSquare(world,at.offset(uphill),Direction.UP);
 }
 public static int run(ServerCommandSource source){
  var world=source.getServer().getWorld(Transit.UNDERGROUND);
  if(world==null){source.sendError(Text.literal("TRACKS_FAILED: metro dimension unavailable"));return 0;}
  int checked=0,errors=0,powered=0;String first="";var visited=new HashSet<BlockPos>();
  record Edge(int x,int z,boolean ns){}
  var edges=new ArrayList<Edge>();edges.add(new Edge(0,0,false));edges.add(new Edge(0,0,true));
  boolean foundSpiral=false,foundSplit=false,foundIsland=false;
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(boolean ns:new boolean[]{false,true}){
   if(ns?Math.floorMod(x,3)!=0:Math.floorMod(z,3)!=0)continue;
   var a=StationGraph.railNode(world.getSeed(),x,z,ns);var b=StationGraph.railNode(world.getSeed(),x+(ns?0:1),z+(ns?1:0),ns);
   boolean spiral=Math.abs(a.y()-b.y())>=48,split=Math.abs(a.y()-b.y())<48&&Math.floorMod(NetworkPlan.hash(world.getSeed(),x,z,309),5)==0;
   boolean island=NetworkPlan.trackOffset(world.getSeed(),x,z,ns)>0||NetworkPlan.trackOffset(world.getSeed(),x+(ns?0:1),z+(ns?1:0),ns)>0;
   if(spiral&&!foundSpiral||split&&!foundSplit||island&&!foundIsland){edges.add(new Edge(x,z,ns));foundSpiral|=spiral;foundSplit|=split;foundIsland|=island;}
  }
  var routes=new ArrayList<NetworkPlan.TrackRoute>();for(var edge:edges)routes.addAll(NetworkPlan.trackRoutes(world.getSeed(),edge.x(),edge.z(),edge.ns()));
  for(var route:routes)for(var lane:(route.singleTrack()?RailPlan.ofSingle(route.route()):RailPlan.of(route.route())).lanes())for(var cell:lane){
   var at=new BlockPos(cell.x(),cell.floor()+1,cell.z());if(!visited.add(at))continue;
   world.getChunk(Math.floorDiv(at.getX(),16),Math.floorDiv(at.getZ(),16));
   var state=world.getBlockState(at);String problem=null;
   if(!(state.getBlock() instanceof AbstractRailBlock))problem="missing rail";
   else if(!supported(world,at,state))problem="unsupported rail; below="+net.minecraft.registry.Registries.BLOCK.getId(world.getBlockState(at.down()).getBlock());
   else if(!world.getBlockState(at.up()).isAir())problem="blocked headroom";
   else {
    int neighbours=0;for(var d:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST}){
     var next=at.offset(d);if(world.getBlockState(next).getBlock() instanceof AbstractRailBlock||world.getBlockState(next.up()).getBlock() instanceof AbstractRailBlock||world.getBlockState(next.down()).getBlock() instanceof AbstractRailBlock)neighbours++;
    }
    // Junctions have selectable active legs; ordinary track cells must match their chain.
    if(neighbours<=2){
     RailShape shape=state.isOf(Blocks.POWERED_RAIL)?state.get(PoweredRailBlock.SHAPE):state.get(RailBlock.SHAPE);
     if(!shape.asString().equals(cell.shape()))problem="wrong rail shape "+shape.asString()+" expected "+cell.shape();
    }
   }
   checked++;if(state.isOf(Blocks.POWERED_RAIL))powered++;if(problem!=null){errors++;if(first.isEmpty())first=at.toShortString()+": "+problem;}
  }
  // Check the two halls at each end, their rail throats and physical exit terminals.
  for(var edge:edges)for(int end=0;end<=1;end++){
   boolean ns=edge.ns();var node=StationGraph.railNode(world.getSeed(),edge.x()+(ns?0:end),edge.z()+(ns?end:0),ns);
   var plan=NetworkPlan.forChunk(world.getSeed(),Math.floorDiv(node.x(),16)*16,Math.floorDiv(node.z(),16)*16);
   var station=plan.stations().stream().filter(s->s.node().equals(node)&&s.northSouth()==ns).findFirst().orElseThrow();
   int length=NetworkPlan.halfLength(station.kind()),width=NetworkPlan.halfWidth(station.kind());
   for(int offset=-length;offset<=length;offset++)for(int lane:new int[]{-(3+station.trackOffset()),3+station.trackOffset()}){
    var at=new BlockPos(node.x()+(ns?-lane:offset),node.y()+1,node.z()+(ns?offset:lane));
    world.getChunk(Math.floorDiv(at.getX(),16),Math.floorDiv(at.getZ(),16));
    if(!(world.getBlockState(at).getBlock() instanceof AbstractRailBlock)||!world.getBlockState(at.up()).isAir()||!supported(world,at,world.getBlockState(at))){
     errors++;if(first.isEmpty())first=at.toShortString()+": station throat blocked";
    }
    checked++;
   }
   var terminal=new BlockPos(node.x()+(ns?-(width+8):-10),node.y()+3,node.z()+(ns?-10:width+8));
   world.getChunk(Math.floorDiv(terminal.getX(),16),Math.floorDiv(terminal.getZ(),16));
   if(!world.getBlockState(terminal).isOf(InfrastructureBlocks.LIFT)){errors++;if(first.isEmpty())first=terminal.toShortString()+": missing exit";}
   for(int side=width-3;side<width+8;side++){
    var feet=new BlockPos(node.x()+(ns?-side:-10),node.y()+3,node.z()+(ns?-10:side));
    world.getChunk(Math.floorDiv(feet.getX(),16),Math.floorDiv(feet.getZ(),16));
    if(!world.getBlockState(feet).isAir()||!world.getBlockState(feet.up()).isAir()||!world.getBlockState(feet.down()).isSideSolidFullSquare(world,feet.down(),Direction.UP)){
     errors++;if(first.isEmpty())first=feet.toShortString()+": blocked exit concourse";
    }
   }
  }
  if(errors>0){source.sendError(Text.literal("TRACKS_FAILED: "+errors+" / "+checked+"; "+first));return 0;}
  int count=checked,powerCount=powered;source.sendFeedback(()->Text.literal("TRACKS_OK: "+count+" generated rails ("+powerCount+" powered); placement support, headroom and shapes verified"),false);return count;
 }
}
