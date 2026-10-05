package eu.metroworld.infrastructure.world;
/** One deterministic architectural family per route; independent of chunk clipping. */
public final class TunnelProfile {
 public enum Style { MODERN, BRICK, INDUSTRIAL, ROCK, CLEAN, SERVICE, COMPACT }
 public record Section(Style style,double halfWidth,int height,boolean compact) {
  public double distance(TransitGeometry.Sample sample){return compact?Math.max(Math.abs(sample.signedOffset()-0.5),sample.distance()-0.5):sample.distance();}
  public int roof(double distance){return compact?3:style==Style.CLEAN||style==Style.INDUSTRIAL?height:height-(distance>=halfWidth-1?2:distance>=halfWidth-2?1:0);}
  public boolean lamp(double along,boolean damaged){
   int period=switch(style){case MODERN,CLEAN->8;case BRICK->20;case INDUSTRIAL->16;case ROCK->24;case SERVICE->32;case COMPACT->40;};
   int phase=Math.floorMod((int)Math.floor(along),period);
   return phase<(style==Style.MODERN||style==Style.CLEAN?4:1)&&(!damaged||Math.floorMod((int)Math.floor(along/period),3)!=1);
  }
 }
 public static Section section(long salt,boolean service,boolean transfer,double along,double length){
  if(transfer)return new Section(Style.MODERN,3,5,false);
  if(service){
   Style style=switch(Math.floorMod((int)(salt>>>9),4)){case 0->Style.MODERN;case 1->Style.BRICK;case 2->Style.SERVICE;default->Style.COMPACT;};
   // Small sections keep generous vestibules at both ends of the service branch.
   boolean compact=style==Style.COMPACT&&length>32&&along>=10&&along<=length-10;
   return new Section(style,compact?1.5:3,compact?3:5,compact);
  }
  Style style=switch(Math.floorMod((int)(salt>>>9),5)){case 0->Style.MODERN;case 1->Style.BRICK;case 2->Style.INDUSTRIAL;case 3->Style.ROCK;default->Style.CLEAN;};
  return new Section(style,style==Style.INDUSTRIAL||style==Style.ROCK?6:5,style==Style.ROCK?9:7,false);
 }
}
