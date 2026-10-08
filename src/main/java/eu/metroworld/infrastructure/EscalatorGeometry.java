package eu.metroworld.infrastructure;
/** Pure geometry: half-block steps give a walkable rise over twice the horizontal distance. */
public final class EscalatorGeometry {
 private EscalatorGeometry(){}
 public record Plan(int x,int y,int z,int dx,int dz,int rise,boolean descending){
  public Plan {if(rise<1||rise>32||Math.abs(dx)+Math.abs(dz)!=1)throw new IllegalArgumentException("Invalid escalator plan");}
  public int length(){return rise*2;}
  public double along(double px,double pz){return (px-x-.5)*dx+(pz-z-.5)*dz+.5;}
  public double across(double px,double pz){return -(px-x-.5)*dz+(pz-z-.5)*dx;}
  public double floorHeight(double along){if(along<0)return y;if(along>=length())return y+rise;return y+(Math.floor(along)+1)*.5;}
  /** Feet follow the uphill edge of the 0.6-block-wide player, not merely the center cell. */
  public double height(double along){if(along<0)return y;if(along>=length())return y+rise;return y+Math.min(rise,(Math.floor(along+.300000001)+1)*.5);}
  public boolean riding(double px,double py,double pz){double along=along(px,pz);return along>=0&&along<length()&&Math.abs(across(px,pz))<.78&&Math.abs(py-height(along))<.2;}
  public double advance(double along){return along+(descending?-.08:.08);}
 }
}
