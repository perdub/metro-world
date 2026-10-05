package eu.metroworld.infrastructure.world;

import java.util.ArrayList;
import java.util.List;

/** Seed-independent geometry primitives. Coordinates are sampled before chunk clipping. */
public final class TransitGeometry {
    private TransitGeometry() {}
    public record Point(double x, double y, double z) {}
    public record Sample(double distance, double floorY, double headingX, double headingZ, double along, double signedOffset) {}
    /** Allow a little raster tolerance so rounded curves do not leave holes in either track. */
    public static boolean isTrackLane(Sample sample,double halfWidth){
        return sample.distance()<halfWidth-1&&Math.abs(Math.abs(sample.signedOffset())-2)<0.9;
    }
    public static final class Route {
        private final List<Point> points;
        private final double minX,maxX,minZ,maxZ;
        public Route(List<Point> input) {
            if(input.size()<2) throw new IllegalArgumentException("A route needs two points");
            points=List.copyOf(input);
            minX=points.stream().mapToDouble(Point::x).min().orElseThrow();
            maxX=points.stream().mapToDouble(Point::x).max().orElseThrow();
            minZ=points.stream().mapToDouble(Point::z).min().orElseThrow();
            maxZ=points.stream().mapToDouble(Point::z).max().orElseThrow();
        }
        public List<Point> points(){return points;}
        public double length(){double sum=0;for(int i=1;i<points.size();i++)sum+=Math.hypot(points.get(i).x-points.get(i-1).x,points.get(i).z-points.get(i-1).z);return sum;}
        /** Unit X/Z tangent at a distance measured along the route. */
        public Point headingAt(double distance){
            double travel=0;
            for(int i=1;i<points.size();i++){
                Point a=points.get(i-1),b=points.get(i);double dx=b.x-a.x,dz=b.z-a.z,len=Math.hypot(dx,dz);
                if(len<1e-8)continue;
                if(travel+len>=distance)return new Point(dx/len,0,dz/len);
                travel+=len;
            }
            Point a=points.get(points.size()-2),b=points.getLast();double len=Math.hypot(b.x-a.x,b.z-a.z);
            return new Point((b.x-a.x)/len,0,(b.z-a.z)/len);
        }
        public boolean intersects(int chunkX,int chunkZ,double padding){
            return maxX+padding>=chunkX&&minX-padding<=chunkX+15&&maxZ+padding>=chunkZ&&minZ-padding<=chunkZ+15;
        }
        /** Column query, preserving separate levels of a helix; run once per x/z column. */
        public List<Sample> samplesNear(double x,double z,double halfWidth){
            List<Sample> found=new ArrayList<>();double travel=0;
            for(int i=1;i<points.size();i++){
                Point a=points.get(i-1),b=points.get(i);
                double dx=b.x-a.x,dz=b.z-a.z,len=Math.hypot(dx,dz);
                if(len<1e-8)continue;
                if(x<Math.min(a.x,b.x)-halfWidth||x>Math.max(a.x,b.x)+halfWidth||z<Math.min(a.z,b.z)-halfWidth||z>Math.max(a.z,b.z)+halfWidth){travel+=len;continue;}
                double t=Math.max(0,Math.min(1,((x-a.x)*dx+(z-a.z)*dz)/(len*len)));
                double dist=Math.hypot(x-a.x-t*dx,z-a.z-t*dz),floor=a.y+t*(b.y-a.y);
                if(dist<=halfWidth){
                    Sample sample=new Sample(dist,floor,dx/len,dz/len,travel+t*len,(-(x-a.x-t*dx)*dz+(z-a.z-t*dz)*dx)/len);int match=-1;
                    for(int k=0;k<found.size();k++)if(Math.abs(found.get(k).floorY-floor)<8){match=k;break;}
                    if(match<0)found.add(sample);else if(dist<found.get(match).distance)found.set(match,sample);
                }
                travel+=len;
            }
            return List.copyOf(found);
        }
        /** Use this for tubes without vertical loops. For helices use nearest(x,y,z). */
        public Sample nearest(double x,double z){return nearest(x,Double.NaN,z);}
        public Sample nearest(double x,double y,double z){
            double best=Double.POSITIVE_INFINITY, travel=0; Sample result=null;
            for(int i=1;i<points.size();i++){
                Point a=points.get(i-1),b=points.get(i);
                double dx=b.x-a.x,dz=b.z-a.z,dy=b.y-a.y,len=Math.hypot(dx,dz);
                if(len<1e-8)continue;
                double t=Math.max(0,Math.min(1,((x-a.x)*dx+(z-a.z)*dz)/(len*len)));
                double floor=a.y+t*dy,dist=Math.hypot(x-a.x-t*dx,z-a.z-t*dz);
                // Height disambiguates stacked turns while leaving tube cross sections horizontal.
                double score=dist*dist+(Double.isNaN(y)?0:Math.pow(y-floor,2));
                if(score<best){best=score;result=new Sample(dist,floor,dx/len,dz/len,travel+t*len,(-(x-a.x-t*dx)*dz+(z-a.z-t*dz)*dx)/len);}
                travel+=len;
            }
            return result;
        }
    }
    /** Broad horizontal quarter arcs replace instantaneous right-angle corners. */
    public static Route rounded(List<Point> controls,double radius){
        List<Point> out=new ArrayList<>();out.add(controls.getFirst());
        for(int i=1;i<controls.size()-1;i++){
            Point a=controls.get(i-1),b=controls.get(i),c=controls.get(i+1);
            double l1=Math.hypot(b.x-a.x,b.z-a.z),l2=Math.hypot(c.x-b.x,c.z-b.z);
            if(l1<1e-6||l2<1e-6)continue;
            double r=Math.min(radius,Math.min(l1,l2)*0.42);
            Point entry=mix(b,a,r/l1),exit=mix(b,c,r/l2);
            appendLine(out,entry);
            // Quadratic fillet: tangents match both straight approach segments.
            int steps=Math.max(8,(int)Math.ceil(r*3));
            for(int k=1;k<=steps;k++){
                double t=(double)k/steps,u=1-t;
                out.add(new Point(u*u*entry.x+2*u*t*b.x+t*t*exit.x,u*u*entry.y+2*u*t*b.y+t*t*exit.y,u*u*entry.z+2*u*t*b.z+t*t*exit.z));
            }
        }
        appendLine(out,controls.getLast());return new Route(out);
    }
    private static Point mix(Point a,Point b,double t){return new Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t,a.z+(b.z-a.z)*t);}
    private static void appendLine(List<Point> out,Point end){
        Point start=out.getLast();int n=Math.max(1,(int)Math.ceil(Math.hypot(end.x-start.x,end.z-start.z)/1.5));
        for(int k=1;k<=n;k++)out.add(mix(start,end,(double)k/n));
    }
    /** Full circular ramp, entering and leaving eastbound. Turns separated by >=24 blocks. */
    public static Route helix(Point entry,double targetY,double radius){
        double rise=targetY-entry.y;
        if(Math.abs(rise)<20)throw new IllegalArgumentException("Helix requires at least 20 blocks rise");
        int turns=Math.max(1,(int)Math.floor(Math.abs(rise)/24));
        int n=(int)Math.ceil(turns*Math.PI*2*radius/1.25);
        List<Point> out=new ArrayList<>();
        for(int i=0;i<=n;i++){
            double t=(double)i/n,angle=-Math.PI/2+t*turns*Math.PI*2;
            out.add(new Point(entry.x+radius*Math.cos(angle),entry.y+rise*t,entry.z+radius+radius*Math.sin(angle)));
        }
        return new Route(out);
    }
    /** East/west station throats always start flat before the route bends outside the building. */
    public static Route between(Point a,Point b,int direction,double stationHalfLength,long salt){
        double lead=stationHalfLength+24,r=24;
        Point throat=new Point(a.x+stationHalfLength,a.y,a.z);
        Point transition=new Point(a.x+lead+40,a.y,a.z);
        List<Point> out=new ArrayList<>();out.add(throat);appendLine(out,transition);
        double rise=b.y-a.y;
        if(Math.abs(rise)>=20){
            Point coilEntry=transition;
            // A crossline leaving the same station gets its own spiral bay. Without this
            // offset, the E/W line and the crossline literally start with the same helix.
            if(direction!=0){
                // Leave along the tangent, then put the second coil in a separate bay on X.
                // A lateral Z connector would cut straight through the first spiral's center.
                coilEntry=new Point(transition.x+128,transition.y,transition.z);
                appendLine(out,coilEntry);
            }
            List<Point> coil=helix(coilEntry,b.y,30).points();
            out.addAll(coil.subList(1,coil.size()));
        }else if(Math.abs(rise)>0){
            // Small changes use a long straight walking ramp, never a vertical stair shaft.
            transition=new Point(transition.x+Math.abs(rise)*5,b.y,transition.z);
            appendLine(out,transition);
        }
        Point elevated=out.getLast();
        List<Point> p=new ArrayList<>();p.add(elevated);
        if(direction==0){
            double middle=Math.max(elevated.x+48,(a.x+b.x)/2);
            p.add(new Point(middle,b.y,a.z));p.add(new Point(middle,b.y,b.z));
            p.add(new Point(b.x-lead,b.y,b.z));
        }else{
            double outside=Math.max(elevated.x,b.x+lead)+64+Math.floorMod(salt,24);
            double middle=(a.z+b.z)/2;
            p.add(new Point(outside,b.y,a.z));p.add(new Point(outside,b.y,middle));
            p.add(new Point(b.x-lead,b.y,middle));p.add(new Point(b.x-lead,b.y,b.z));
        }
        p.add(new Point(b.x-stationHalfLength,b.y,b.z));
        List<Point> tail=rounded(p,r).points();out.addAll(tail.subList(1,tail.size()));
        return new Route(out);
    }
}
