package eu.metroworld.infrastructure.world;

/** Botanical side halls. Coordinates are local to the annex floor centre. */
public final class BotanicalAnnex {
 private BotanicalAnnex() {}
 public enum Kind { GREENHOUSE, SEED_BANK, LABORATORY, OVERGROWN_NURSERY }
 public record Spec(Kind kind,int offsetX,int offsetZ,int halfWidth,int halfLength,int height,long salt) {}
 public static Spec spec(long salt){
  long mixed=salt^0x6a09e667f3bcc909L;
  mixed=(mixed^(mixed>>>30))*0xbf58476d1ce4e5b9L;
  mixed=(mixed^(mixed>>>27))*0x94d049bb133111ebL;
  mixed^=mixed>>>31;
  return new Spec(Kind.values()[Math.floorMod(mixed,4)],72,-52,10,12,12,mixed);
 }
 public static boolean contains(Spec spec,int x,int y,int z){
  return Math.abs(x)<=spec.halfWidth()&&Math.abs(z)<=spec.halfLength()&&y>=-3&&y<=spec.height();
 }
 /** Unobstructed route from the south connector to the rear inspection aisle. */
 public static boolean walkway(Spec spec,int x,int y,int z){
  return contains(spec,x,y,z)&&Math.abs(x)<=2&&z>=-7&&y>=1&&y<=3;
 }
 public static boolean chest(int x,int y,int z){return x==6&&y==1&&z==-7;}
 /** 3=bedrock, 2=obsidian, 1=stone, 0=finished interior. */
 public static int shellLayer(Spec spec,int x,int y,int z){
  int inset=Math.min(spec.halfWidth()-Math.abs(x),spec.halfLength()-Math.abs(z));
  inset=Math.min(inset,spec.height()-y);
  if(y<0)inset=Math.min(inset,y+3);
  return inset==0?3:inset==1?2:inset==2?1:0;
 }
}
