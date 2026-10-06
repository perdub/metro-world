package eu.metroworld.infrastructure.world;

/** Damage is confined to peripheral architectural surfaces, never to route topology. */
public final class DecayPlan {
 public enum Mark { KEEP, MISSING_PANEL, BROKEN_PANEL, RUBBLE, WEB, HAZARD, YELLOW_GLASS, RED_LIGHT, DARK_LAMP }
 private DecayPlan(){}
 public static boolean protectedPassage(int x,int y,int z,int length,int width,int expansion){
  int az=Math.abs(z),gap=3+expansion;
  if(y<=3&&Math.abs(az-gap)<=2)return true;
  if(Math.abs(x)>=length-12)return true;
  // Arrival area, exit vestibule, lower interchange doorway and all public stairs.
  if(Math.abs(x-2)<=5&&Math.abs(z-8)<=5)return true;
  if(x>=-18&&x<=-4&&az>=width-5)return true;
  if(x>=6&&x<=18)return true;
  if(x>=length-26)return true;
  if(expansion>0&&x<=-length+15)return true;
  return false;
 }
 public static Mark mark(int x,int y,int z,int length,int width,int height,long salt,NetworkPlan.Condition condition,int expansion){
  if(condition==NetworkPlan.Condition.INTACT||protectedPassage(x,y,z,length,width,expansion))return Mark.KEEP;
  int az=Math.abs(z),bay=Math.floorMod(Math.floorDiv(x,4)+(int)(salt>>>19),7);
  boolean wall=az==width&&y>=5&&y<=10;
  boolean ceiling=y>=height-2&&y<=height&&az>=width-7&&az<width;
  boolean edge=x< -18&&Math.abs(x)<length-12&&az==width-2;
  return switch(condition){
   case COLLAPSED -> {
    if(wall&&bay==0&&y>=6&&y<=8)yield Mark.MISSING_PANEL;
    if(wall&&bay==1&&y<=7)yield Mark.BROKEN_PANEL;
    if(ceiling&&bay<=1)yield Mark.DARK_LAMP;
    if(edge&&y==3&&Math.floorMod(x,7)<2)yield Mark.RUBBLE;
    if(edge&&y==5&&Math.floorMod(x,19)==0)yield Mark.WEB;
    yield Mark.KEEP;
   }
   case QUARANTINE -> {
    if(az==width&&y==4)yield Mark.HAZARD;
    if(wall&&y>=5&&y<=8)yield Mark.YELLOW_GLASS;
    if(edge&&y==5&&Math.floorMod(x,17)==0)yield Mark.WEB;
    if(wall&&y==9&&Math.floorMod(x,12)==0)yield Mark.RED_LIGHT;
    yield Mark.KEEP;
   }
   case CHEMICAL -> {
    if(az==width&&(y==4||y==10))yield Mark.HAZARD;
    if(wall&&y==8&&Math.floorMod(x,12)==0)yield Mark.RED_LIGHT;
    if(wall&&y>=5&&y<=7)yield Mark.YELLOW_GLASS;
    if(ceiling&&bay==0)yield Mark.DARK_LAMP;
    yield Mark.KEEP;
   }
   default -> Mark.KEEP;
  };
 }
}
