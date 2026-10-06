package eu.metroworld.infrastructure.world;

/** Seeded, block-independent geometry shared by planning and dome construction. */
public final class DomeDesign {
 private DomeDesign() {}
 public enum Kind { BIO, AQUA }
 public enum Size { SMALL, MEDIUM, LARGE }
 public record Spec(Kind kind,Size size,int outerRadius,int innerRadius,int offsetX,int offsetZ) {}
 public static Spec spec(long salt){
  long mixed=salt^(salt>>>33);mixed*=0xff51afd7ed558ccdL;mixed^=mixed>>>33;
  Size size=Size.values()[Math.floorMod(mixed,3)];
  int outer=26+8*size.ordinal();
  return new Spec(Math.floorMod(mixed>>>4,4)==0?Kind.AQUA:Kind.BIO,size,outer,outer-11,0,-62);
 }
 public static Spec legacy(){return new Spec(Kind.BIO,Size.MEDIUM,34,23,0,-62);}
 public static boolean dryPath(Spec spec,int x,int z){
  return Math.abs(x)<=2||Math.abs(z)<=2||Math.hypot(x,z)>=spec.innerRadius()-4;
 }
 public static boolean tankWall(Spec spec,int x,int z){
  double r=Math.hypot(x,z);
  return spec.kind()==Kind.AQUA&&Math.abs(x)>2&&Math.abs(z)>2&&r<spec.innerRadius()-3&&
   ((Math.abs(x)==3&&Math.abs(z)>=3)||(Math.abs(z)==3&&Math.abs(x)>=3)||r>=spec.innerRadius()-4);
 }
 public static boolean water(Spec spec,int x,int y,int z){
  return spec.kind()==Kind.AQUA&&y>=1&&y<=4&&!dryPath(spec,x,z)&&!tankWall(spec,x,z);
 }
}
