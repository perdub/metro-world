package eu.metroworld.infrastructure.world;
/** Continuous seeded 3D exclusions. An obstacle can end above or below a tunnel. */
public final class ExclusionNoise {
 private ExclusionNoise(){}
 private static final double GX=.78*1.5/1100+.22*1.5/410,GY=.78*1.5/192+.22*1.5/80;
 private static double smooth(double t){return t*t*(3-2*t);}
 private static double value(long seed,int x,int y,int z){return (NetworkPlan.hash(seed^(y*0x632be59bd9b4e019L),x,z,901)>>>11)*0x1.0p-53;}
 private static double mix(double a,double b,double t){return a+(b-a)*t;}
 private static double octave(long seed,double x,double y,double z,double horizontal,double vertical){
  double u=x/horizontal,v=y/vertical,w=z/horizontal;int ix=(int)Math.floor(u),iy=(int)Math.floor(v),iz=(int)Math.floor(w);
  double a=smooth(u-ix),b=smooth(v-iy),c=smooth(w-iz);
  double low=mix(mix(value(seed,ix,iy,iz),value(seed,ix+1,iy,iz),a),mix(value(seed,ix,iy,iz+1),value(seed,ix+1,iy,iz+1),a),c);
  double high=mix(mix(value(seed,ix,iy+1,iz),value(seed,ix+1,iy+1,iz),a),mix(value(seed,ix,iy+1,iz+1),value(seed,ix+1,iy+1,iz+1),a),c);
  return mix(low,high,b);
 }
 public static double sample(long seed,double x,double y,double z){return .78*octave(seed,x,y,z,1100,192)+.22*octave(seed^0x5deece66dL,x,y,z,410,80);}
 public static boolean blocked(long seed,double x,double y,double z){return sample(seed,x,y,z)>.63;}
 private static double[] critical(double center,double half,double scale){
  double lo=center-half,hi=center+half;int first=(int)Math.floor(lo/scale)+1,last=(int)Math.ceil(hi/scale)-1;
  double[] values=new double[Math.max(0,last-first+1)+2];values[0]=lo;values[1]=hi;for(int i=first;i<=last;i++)values[i-first+2]=i*scale;return values;
 }
 private static double upper(long seed,double x,double y,double z,double hx,double hy,double hz,double horizontal,double vertical){
  // Each octave is monotone in each coordinate inside one lattice cell. Its exact box
  // maximum is attained at a box corner or an intervening lattice boundary.
  double max=0;for(double xx:critical(x,hx,horizontal))for(double yy:critical(y,hy,vertical))for(double zz:critical(z,hz,horizontal))max=Math.max(max,octave(seed,xx,yy,zz,horizontal,vertical));return max;
 }
 /** Entire volume clearance, proven with the interpolation derivative bound, not sparse point guesses. */
 public static boolean boxClear(long seed,double x,double y,double z,double hx,double hy,double hz){
  double value=sample(seed,x,y,z),bound=GX*(hx+hz)+GY*hy;
  if(value>.63)return false;if(value+bound<=.63)return true;
  if(.78*upper(seed,x,y,z,hx,hy,hz,1100,192)+.22*upper(seed^0x5deece66dL,x,y,z,hx,hy,hz,410,80)<=.63)return true;
  // At a tiny uncertain boundary reject conservatively; never carve through a forbidden voxel.
  if(bound<.006)return false;
  double dx=GX*hx,dy=GY*hy,dz=GX*hz;
  if(dy>=dx&&dy>=dz)return boxClear(seed,x,y-hy/2,z,hx,hy/2,hz)&&boxClear(seed,x,y+hy/2,z,hx,hy/2,hz);
  if(dx>=dz)return boxClear(seed,x-hx/2,y,z,hx/2,hy,hz)&&boxClear(seed,x+hx/2,y,z,hx/2,hy,hz);
  return boxClear(seed,x,y,z-hz/2,hx,hy,hz/2)&&boxClear(seed,x,y,z+hz/2,hx,hy,hz/2);
 }
 public static boolean stationClear(long seed,double x,double y,double z){return boxClear(seed,x,y+8,z,180,44,180);}
 public static boolean routeClear(long seed,TransitGeometry.Route route,double padding){
  var ps=route.points();for(int i=1;i<ps.size();i++){
   var a=ps.get(i-1);var b=ps.get(i);
   // One conservative box contains the whole segment plus split lanes, roof, floor and shell.
   if(!boxClear(seed,(a.x()+b.x())/2,(a.y()+b.y())/2+5,(a.z()+b.z())/2,
      padding+Math.abs(b.x()-a.x())/2,18+Math.abs(b.y()-a.y())/2,padding+Math.abs(b.z()-a.z())/2))return false;
  }return true;
 }
}
