package eu.metroworld.infrastructure.train;
/** Separating-axis test for rotated carriage footprints, including diagonal track pairs. */
public final class TrainEnvelope {
 private TrainEnvelope(){}
 public static boolean overlaps(TrainPath.Pose a,double al,double aw,TrainPath.Pose b,double bl,double bw){
  if(Math.abs(a.y()-b.y())>3+Math.abs(a.grade()*al/2)+Math.abs(b.grade()*bl/2))return false;
  double dx=b.x()-a.x(),dz=b.z()-a.z();
  for(double[] axis:new double[][]{{a.hx(),a.hz()},{a.hz(),-a.hx()},{b.hx(),b.hz()},{b.hz(),-b.hx()}}){
   double ar=Math.abs(axis[0]*a.hx()+axis[1]*a.hz())*al/2+Math.abs(axis[0]*a.hz()-axis[1]*a.hx())*aw/2;
   double br=Math.abs(axis[0]*b.hx()+axis[1]*b.hz())*bl/2+Math.abs(axis[0]*b.hz()-axis[1]*b.hx())*bw/2;
   if(Math.abs(dx*axis[0]+dz*axis[1])>=ar+br+.2)return false;
  }return true;
 }
}
