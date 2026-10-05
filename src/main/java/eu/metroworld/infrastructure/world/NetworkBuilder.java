package eu.metroworld.infrastructure.world;
import eu.metroworld.infrastructure.InfrastructureBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.*;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.Chunk;
/** Rasterizes architecture and curved tubes before touching the chunk: explicit merge priorities. */
public final class NetworkBuilder {
 private static final BlockState AIR=Blocks.AIR.getDefaultState();
 private static final class Canvas {
  final Chunk chunk;final int sx,sz,bottom,height;final BlockState[] states;final byte[] priorities;final java.util.Map<BlockPos,String> signs=new java.util.HashMap<>();
  Canvas(Chunk chunk){this.chunk=chunk;sx=chunk.getPos().getStartX();sz=chunk.getPos().getStartZ();bottom=chunk.getBottomY();height=chunk.getHeight();states=new BlockState[height*256];priorities=new byte[states.length];}
  void put(int x,int y,int z,BlockState state,int priority){if(x<sx||x>sx+15||z<sz||z>sz+15||y<bottom||y>=bottom+height)return;int i=(y-bottom)*256+(z-sz)*16+x-sx;if(priority>=priorities[i]){states[i]=state;priorities[i]=(byte)priority;}}
  void box(int x1,int x2,int y1,int y2,int z1,int z2,BlockState state,int priority){for(int x=Math.max(sx,x1);x<=Math.min(sx+15,x2);x++)for(int z=Math.max(sz,z1);z<=Math.min(sz+15,z2);z++)for(int y=y1;y<=y2;y++)put(x,y,z,state,priority);}
  void flush(long seed){BlockPos.Mutable pos=new BlockPos.Mutable();for(int i=0;i<states.length;i++){BlockState state=states[i];if(state==null)continue;int x=sx+(i&15),z=sz+((i>>>4)&15),y=bottom+(i>>>8);chunk.setBlockState(pos.set(x,y,z),state,false);
   if(state.isOf(Blocks.CHEST)){ChestBlockEntity chest=new ChestBlockEntity(pos.toImmutable(),state);chest.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE,Identifier.of("btr_infrastructure","chests/supplies")));chest.setLootTableSeed(NetworkPlan.hash(seed,x,z,y));chunk.setBlockEntity(chest);}
   if(state.isOf(Blocks.OAK_WALL_SIGN)){
    String name=signs.getOrDefault(pos.toImmutable(),"Метро");
    SignText text=new SignText().withMessage(0,net.minecraft.text.Text.literal(name)).withMessage(1,net.minecraft.text.Text.literal("← Лифт / выход")).withMessage(2,net.minecraft.text.Text.literal("Переходы →")).withGlowing(true);
    net.minecraft.nbt.NbtCompound data=new net.minecraft.nbt.NbtCompound();
    data.putString("id","minecraft:sign");data.putInt("x",x);data.putInt("y",y);data.putInt("z",z);
    data.put("front_text",SignText.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,text).getOrThrow());
    chunk.addPendingBlockEntityNbt(data);
   }
   if(state.isOf(Blocks.SPAWNER)){MobSpawnerBlockEntity spawner=new MobSpawnerBlockEntity(pos.toImmutable(),state);spawner.setEntityType(EntityType.ZOMBIE,net.minecraft.util.math.random.Random.create(NetworkPlan.hash(seed,x,z,y)));chunk.setBlockEntity(spawner);}
  }}
 }
 private static void shell(Canvas c,int x,int y,int z,double edge){if(edge>=-6&&edge<0)c.put(x,y,z,(edge>=-3?Blocks.STONE:edge>=-5?Blocks.OBSIDIAN:Blocks.BEDROCK).getDefaultState(),(int)Math.ceil(edge)+7);}
 public static void build(Chunk chunk,long seed){
  Canvas c=new Canvas(chunk);var plan=NetworkPlan.forChunk(seed,c.sx,c.sz);
  for(var room:plan.rooms())room(c,room);
  for(var path:plan.paths())path(c,path);
  for(var tower:plan.stairs())stairs(c,tower);
  for(var station:plan.stations())station(c,station,seed);
  // Keep the showcase interchange independent of the general network's neighbour scan.
  // Its extent is clipped by Canvas.put, so only chunks near each component do work.
  // The public showcase hub is anchored for the supplied preset seed (619015). Keeping
  // this fixed also avoids relying on per-region worldgen random seeds for a cross-chunk link.
  var west=new NetworkPlan.Node(548,-25,-145);var east=new NetworkPlan.Node(868,-25,-145);
  if(overlaps(c, east.x()-40,east.x()+40,east.z()-17,east.z()+17))
   station(c,new NetworkPlan.Station(east,NetworkPlan.hash(seed,1,0,911),NetworkPlan.Condition.INTACT,true),seed);
  var walk=new TransitGeometry.Route(java.util.List.of(
   new TransitGeometry.Point(west.x()+34,west.y()+2,west.z()+12),
   new TransitGeometry.Point(east.x()-34,east.y()+2,east.z()+12)));
  if(overlaps(c,west.x()+34,east.x()-34,west.z()+9,west.z()+15))
   path(c,new NetworkPlan.Path(walk,NetworkPlan.Condition.INTACT,NetworkPlan.hash(seed,1,0,911),true,true));
  c.flush(seed);
 }
 private static boolean overlaps(Canvas c,int x1,int x2,int z1,int z2){
  return x2>=c.sx&&x1<=c.sx+15&&z2>=c.sz&&z1<=c.sz+15;
 }
 private static void station(Canvas c,NetworkPlan.Station s,long seed){var n=s.node();
  for(int x=c.sx;x<c.sx+16;x++)for(int z=c.sz;z<c.sz+16;z++)for(int h=-6;h<=20;h++){
   int dx=x-n.x(),dz=z-n.z();double edge=Math.min(Math.min(40-Math.abs(dx),17-Math.abs(dz)),Math.min(h,14-h));
   if(edge<0){shell(c,x,n.y()+h,z,edge);continue;}
   BlockState state=StationDesign.sample(dx,h,dz,s.salt());
   // Dedicated doors, not accidental carving through a wall. Stair mezzanine starts farther east.
   if(dx>=-10&&dx<=-6&&dz>=14&&h>=3&&h<=6)state=AIR;
   if(dx>=34&&Math.abs(dz-12)<=2&&h>=3&&h<=5)state=AIR;
   if(s.interchange()&&dx<=-34&&Math.abs(dz-12)<=2&&h>=3&&h<=7)state=AIR;
   if(state!=null)c.put(x,n.y()+h,z,state,state.isAir()?34:35);
  }
  // A sparse amount of damage leaves the original bright architecture recognizable.
  if(s.condition()==NetworkPlan.Condition.COLLAPSED){
   c.box(n.x()-34,n.x()-31,n.y()+3,n.y()+4,n.z()+13,n.z()+14,Blocks.COBBLESTONE.getDefaultState(),36);
   c.put(n.x()-33,n.y()+5,n.z()+13,Blocks.COBBLESTONE_SLAB.getDefaultState(),36);
  }
  String[] names={"Лазурная","Оранжерея","Северная","Белая линия","Пересадочная","Светлая","Кварцевая","Тихая"};
  String name=s.interchange()?(n.x()==548?"Западная пересадка":"Восточная пересадка"):n.x()==0&&n.z()==0?"Центральная":names[(int)Math.floorMod(s.salt(),names.length)];
  BlockPos signPos=new BlockPos(n.x(),n.y()+6,n.z()+16);c.signs.put(signPos,name);
  c.put(signPos.getX(),signPos.getY(),signPos.getZ(),Blocks.OAK_WALL_SIGN.getDefaultState().with(WallSignBlock.FACING,Direction.NORTH),40);
  // Safe landing in an uncluttered platform aisle. Return terminal is separate from circulation.
  c.put(n.x()+2,n.y()+2,n.z()+8,Blocks.SMOOTH_QUARTZ.getDefaultState(),50);
  c.box(n.x()+2,n.x()+2,n.y()+3,n.y()+4,n.z()+8,n.z()+8,AIR,50);
  c.put(n.x()-10,n.y()+3,n.z()+8,InfrastructureBlocks.LIFT.getDefaultState(),50);
  // Small searchable supply caches sit against the outside ends of each platform.
  c.put(n.x()+24,n.y()+3,n.z()+14,Blocks.CHEST.getDefaultState(),40);
  c.put(n.x()-24,n.y()+3,n.z()-14,Blocks.CHEST.getDefaultState(),40);
 }
 private static void path(Canvas c,NetworkPlan.Path p){double width=p.service()?3:5;int fullHeight=p.service()?5:7;
  BlockState[] accents={Blocks.CYAN_CONCRETE.getDefaultState(),Blocks.LIME_CONCRETE.getDefaultState(),Blocks.ORANGE_CONCRETE.getDefaultState(),Blocks.LIGHT_BLUE_CONCRETE.getDefaultState(),Blocks.PURPLE_CONCRETE.getDefaultState()};
  BlockState[] glasses={Blocks.CYAN_STAINED_GLASS.getDefaultState(),Blocks.LIME_STAINED_GLASS.getDefaultState(),Blocks.ORANGE_STAINED_GLASS.getDefaultState(),Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState(),Blocks.PURPLE_STAINED_GLASS.getDefaultState()};
  int line=Math.floorMod((int)(p.salt()>>>5),accents.length);BlockState accent=accents[line],glass=glasses[line];
  for(int x=c.sx;x<c.sx+16;x++)for(int z=c.sz;z<c.sz+16;z++)for(var sample:p.route().samplesNear(x,z,width+6)){
   int base=(int)Math.floor(sample.floorY());double d=sample.distance();int roof=fullHeight-(d>=width-1?2:d>=width-2?1:0);
   for(int h=-6;h<=fullHeight+6;h++){
    double edge=Math.min(width-d,Math.min(h,roof-h));int y=base+h;
    if(edge<0){shell(c,x,y,z,edge);continue;}
    BlockState state=AIR;int priority=20;
    if(h==0){state=(d<1.3&&!p.service()?Blocks.POLISHED_DEEPSLATE:Blocks.SMOOTH_STONE).getDefaultState();priority=30;}
    else if(d>width-0.85||h==roof){
     state=Blocks.WHITE_CONCRETE.getDefaultState();priority=12;
     if(h==3&&d>width-0.85)state=accent;
     if(h==roof&&d<2.6&&Math.floorMod((int)sample.along(),8)<5)state=Blocks.SEA_LANTERN.getDefaultState();
     if(d>width-0.85&&h==2)state=Blocks.SMOOTH_QUARTZ_STAIRS.getDefaultState().with(StairsBlock.FACING,Math.abs(sample.headingX())>Math.abs(sample.headingZ())?(sample.headingX()>0?Direction.EAST:Direction.WEST):(sample.headingZ()>0?Direction.SOUTH:Direction.NORTH));
     if(d>width-0.85&&h==4&&Math.floorMod((int)sample.along(),16)<=1)state=Blocks.WAXED_OXIDIZED_CUT_COPPER.getDefaultState();
     if(d>width-0.85&&h>=4&&h<roof&&Math.floorMod((int)sample.along(),16)>2)state=glass;
    }
    c.put(x,y,z,state,priority);
   }
   // Ramps use half-block treads: a gentle climb without jumping full cubes.
   double fraction=sample.floorY()-base;
   if(fraction>0.5&&d<width-1.2)c.put(x,base+1,z,Blocks.SMOOTH_STONE_SLAB.getDefaultState(),31);
   // Discrete vanilla rail marks the route center. Custom trains are a later layer on this spline.
   if(!p.service()&&TransitGeometry.isTrackLane(sample,width)){
    RailShape shape=railShape(p.route(),x,z,sample,base);
    boolean powered=Math.floorMod((int)sample.along()+(int)(p.salt()&0x7fffffff),52)<2;
    if(powered){
     c.put(x,base,z,Blocks.REDSTONE_BLOCK.getDefaultState(),31);
     if(isCurve(shape))shape=dxForShape(shape)?RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
     c.put(x,base+1,z,Blocks.POWERED_RAIL.getDefaultState().with(PoweredRailBlock.SHAPE,shape).with(PoweredRailBlock.POWERED,true),33);
    }else c.put(x,base+1,z,Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE,shape),32);
   }
   int loc=Math.floorMod((int)sample.along()+(int)(p.salt()&63),137);
   if(p.condition()==NetworkPlan.Condition.COLLAPSED&&loc>=64&&loc<=67&&d>2.8&&d<width-1)c.put(x,base+1,z,Blocks.COBBLESTONE.getDefaultState(),33);
   if(p.condition()==NetworkPlan.Condition.CHEMICAL&&loc>=71&&loc<=73&&d>width-2&&d<width-1)c.put(x,base,z,Blocks.MAGMA_BLOCK.getDefaultState(),33);
  }
 }
 private static boolean isCurve(RailShape s){return s==RailShape.NORTH_EAST||s==RailShape.NORTH_WEST||s==RailShape.SOUTH_EAST||s==RailShape.SOUTH_WEST;}
 private static boolean dxForShape(RailShape s){return s==RailShape.NORTH_EAST||s==RailShape.NORTH_WEST;}
 private static RailShape railShape(TransitGeometry.Route route,int x,int z,TransitGeometry.Sample sample,int base){
  double hx=sample.headingX(),hz=sample.headingZ();int dx=Math.abs(hx)>=Math.abs(hz)?(hx>=0?1:-1):0,dz=dx==0?(hz>=0?1:-1):0;
  var ahead=route.nearest(x+dx,sample.floorY(),z+dz);var behind=route.nearest(x-dx,sample.floorY(),z-dz);
  int next=(int)Math.floor(ahead.floorY()),prev=(int)Math.floor(behind.floorY());
  if(next>base)return dx>0?RailShape.ASCENDING_EAST:dx<0?RailShape.ASCENDING_WEST:dz>0?RailShape.ASCENDING_SOUTH:RailShape.ASCENDING_NORTH;
  if(prev>base)return dx>0?RailShape.ASCENDING_WEST:dx<0?RailShape.ASCENDING_EAST:dz>0?RailShape.ASCENDING_NORTH:RailShape.ASCENDING_SOUTH;
  var incoming=route.headingAt(Math.max(0,sample.along()-2));var outgoing=route.headingAt(sample.along()+2);
  Direction in=direction(incoming.x(),incoming.z()),out=direction(outgoing.x(),outgoing.z());
  if(in.getAxis()!=out.getAxis()){
   if(in==Direction.NORTH&&out==Direction.EAST||in==Direction.EAST&&out==Direction.NORTH)return RailShape.NORTH_EAST;
   if(in==Direction.NORTH&&out==Direction.WEST||in==Direction.WEST&&out==Direction.NORTH)return RailShape.NORTH_WEST;
   if(in==Direction.SOUTH&&out==Direction.EAST||in==Direction.EAST&&out==Direction.SOUTH)return RailShape.SOUTH_EAST;
   if(in==Direction.SOUTH&&out==Direction.WEST||in==Direction.WEST&&out==Direction.SOUTH)return RailShape.SOUTH_WEST;
  }
  return dx!=0?RailShape.EAST_WEST:RailShape.NORTH_SOUTH;
 }
 private static Direction direction(double x,double z){return Math.abs(x)>=Math.abs(z)?(x>=0?Direction.EAST:Direction.WEST):(z>=0?Direction.SOUTH:Direction.NORTH);}
 private static void room(Canvas c,NetworkPlan.Room r){
  for(int x=c.sx;x<c.sx+16;x++)for(int z=c.sz;z<c.sz+16;z++)for(int h=-6;h<=r.height()+6;h++){
   int edge=Math.min(Math.min(Math.min(x-r.x1(),r.x2()-x),Math.min(z-r.z1(),r.z2()-z)),Math.min(h,r.height()-h));
   if(edge<0){shell(c,x,r.base()+h,z,edge);continue;}
   boolean side=x==r.x1()||x==r.x2()||z==r.z1()||z==r.z2();
   BlockState state=(h==0?Blocks.SMOOTH_STONE:h==r.height()?Blocks.WHITE_CONCRETE:side?Blocks.SMOOTH_QUARTZ:Blocks.AIR).getDefaultState();
   if(h==r.height()&&Math.floorMod(x+z,7)==0)state=Blocks.SEA_LANTERN.getDefaultState();
   if(side&&h==3)state=(r.condition()==NetworkPlan.Condition.CHEMICAL?Blocks.LIME_CONCRETE:r.condition()==NetworkPlan.Condition.QUARANTINE?Blocks.ORANGE_CONCRETE:Blocks.CYAN_CONCRETE).getDefaultState();
   if(side&&h>=4&&h<=6&&Math.floorMod(x+z,6)!=0)state=Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
   // Both doors meet designated station/service passages, leaving intact wall bays.
   if(h>=1&&h<=4&&((x>=r.x1()+10&&x<=r.x1()+18&&(z==r.z1()||z==r.z2()))||x==r.x2()&&z>=r.z1()+5&&z<=r.z1()+9))state=AIR;
   c.put(x,r.base()+h,z,state,state.isAir()?22:side?13:30);
  }
  int x=r.x1()+4,z=r.z2()-4,y=r.base()+1;
  c.put(x,y,z,Blocks.CHEST.getDefaultState(),40);
  for(int i=0;i<3;i++){
   c.put(r.x2()-4,r.base()+1,r.z2()-5-i*4,Blocks.BARREL.getDefaultState(),34);
   c.put(r.x2()-4,r.base()+3,r.z2()-5-i*4,Blocks.SMOOTH_STONE_SLAB.getDefaultState(),34);
  }
  if(r.condition()==NetworkPlan.Condition.QUARANTINE){c.put(r.x2()-5,y,r.z2()-4,Blocks.SPAWNER.getDefaultState(),40);c.put(r.x2()-3,y+2,r.z2()-3,Blocks.COBWEB.getDefaultState(),34);}
  if(r.condition()==NetworkPlan.Condition.CHEMICAL){c.box(r.x2()-9,r.x2()-7,r.base(),r.base(),r.z2()-9,r.z2()-7,Blocks.MAGMA_BLOCK.getDefaultState(),40);c.put(r.x2()-10,y,r.z2()-8,InfrastructureBlocks.CONTAINER.getDefaultState(),40);}
 }
 private static void stairs(Canvas c,NetworkPlan.StairTower t){
  var room=new NetworkPlan.Room(t.x()-3,t.x()+11,t.z()-1,t.z()+10,t.bottom(),t.top()-t.bottom()+7,NetworkPlan.Condition.INTACT,t.salt());room(c,room);
  // Clear storage placements belonging to the generic room; tower has its own landings.
  c.box(t.x()-2,t.x()+10,t.bottom()+1,t.top()+6,t.z(),t.z()+9,AIR,41);
  c.box(t.x()-3,t.x()-1,t.bottom(),t.bottom(),t.z(),t.z()+9,Blocks.SMOOTH_QUARTZ.getDefaultState(),44);
  for(int level=t.bottom()+4;level<t.top()+6;level+=8)c.put(t.x()+11,level,t.z()+5,Blocks.SEA_LANTERN.getDefaultState(),44);
  int flights=(t.top()-t.bottom())/8;
  for(int f=0;f<flights;f++){
   int start=t.bottom()+f*8,lane=t.z()+(f%2==0?0:6);boolean east=f%2==0;
   for(int step=0;step<8;step++)for(int w=0;w<3;w++){
    int x=t.x()+(east?step:7-step),y=start+step+1,z=lane+w;
    c.put(x,y,z,Blocks.SMOOTH_QUARTZ_STAIRS.getDefaultState().with(StairsBlock.FACING,east?Direction.EAST:Direction.WEST),45);
    c.put(x,y-1,z,Blocks.SMOOTH_QUARTZ.getDefaultState(),44);
    if(w==0)c.put(x,y+1,z-1,Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState(),45);
   }
   int x1=east?t.x()+8:t.x()-3,x2=east?t.x()+10:t.x()-1;
   c.box(x1,x2,start+8,start+8,t.z(),t.z()+8,Blocks.SMOOTH_QUARTZ.getDefaultState(),44);
  }
  // Public openings at both levels; service galleries connect on the left side.
  for(int floor:new int[]{t.bottom(),t.top()})c.box(t.x()-3,t.x()-1,floor+1,floor+4,t.z(),t.z()+2,AIR,46);
 }
}
