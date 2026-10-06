package eu.metroworld.infrastructure.world;

/** Seeded, deliberately dead-end walking branches. Coordinates use the parent station frame. */
public final class SideBranchDesign {
    public enum Kind { SECRET_STOP, QUARANTINE, ABANDONED_DEPOT, UTILITY }
    public record Spec(Kind kind,long salt,int stationHalfLength) {}
    public static boolean present(long salt){return Math.floorMod(salt,3)==0;}
    private SideBranchDesign() {}
    public static Spec select(long salt,int length) {
        return new Spec(Kind.values()[Math.floorMod((int)(salt >>> 23),4)],salt,length);
    }
    public static int minX(Spec s){return -124;}
    public static int maxX(Spec s){return -s.stationHalfLength()+13;}
    public static int minZ(Spec s){return 7;}
    public static int maxZ(Spec s){return 88;}
    public static boolean room(int x,int z){return Math.abs(x+100)<=18&&Math.abs(z-70)<=14;}
    public static boolean corridor(int x,int z,Spec s,int radius){
        return x>=-100-radius&&x<=-s.stationHalfLength()+6+radius&&Math.abs(z-12)<=radius
            ||Math.abs(x+100)<=radius&&z>=12-radius&&z<=70+radius;
    }
    public static boolean footprint(int x,int z,Spec s){return room(x,z)||corridor(x,z,s,4);}
    public static boolean interior(int x,int y,int z,Spec s){
        boolean hall=Math.abs(x+100)<18&&Math.abs(z-70)<14;
        return y>=3&&y<=(hall?11:6)&&(hall||corridor(x,z,s,2));
    }
    /** Includes a protective shell; the root owns the larger station reservation. */
    public static boolean inside(int x,int y,int z,Spec s){
        if(y< -3||y>17)return false;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)
            if(footprint(x+dx,z+dz,s))return true;
        return false;
    }
    public static boolean entrance(int x,int y,int z,Spec s){
        return x>=-s.stationHalfLength()-5&&x<=-s.stationHalfLength()+6&&Math.abs(z-12)<=2&&y>=3&&y<=6;
    }
    public static boolean container(int x,int y,int z,Spec s){
        return y==3&&x==-114&&z==80;
    }
    public static boolean spawner(int x,int y,int z,Spec s){
        return s.kind()==Kind.QUARANTINE&&y==3&&z==78&&(x==-111||x==-100||x==-89);
    }
    public static boolean brokenRail(int x,int y,int z,Spec s){
        return (s.kind()==Kind.SECRET_STOP||s.kind()==Kind.ABANDONED_DEPOT)&&y==3&&z==70
            &&x>=-114&&x<=-86&&Math.floorMod(x+(int)s.salt(),9)<5;
    }
}
