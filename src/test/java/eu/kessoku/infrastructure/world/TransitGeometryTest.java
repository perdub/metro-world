package eu.kessoku.infrastructure.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransitGeometryTest {
    @Test void stationsHaveFlatEastWestThroatsAndWalkableRises(){
        for(int dy=-360;dy<=360;dy+=9)for(int direction=0;direction<2;direction++){
            var a=new TransitGeometry.Point(0,0,0);
            var b=new TransitGeometry.Point(direction==0?384:35,dy,direction==0?71:384);
            var r=TransitGeometry.between(a,b,direction,40,42);
            var first=r.points().getFirst();var last=r.points().getLast();
            assertEquals(40,first.x());assertEquals(0,first.y());
            assertEquals(b.x()-40,last.x());assertEquals(dy,last.y());assertEquals(b.z(),last.z());
            assertEquals(0,r.points().get(1).y());
            for(int i=1;i<r.points().size();i++){
                var p=r.points().get(i-1);var q=r.points().get(i);
                double run=Math.hypot(q.x()-p.x(),q.z()-p.z());
                assertTrue(Math.abs(q.y()-p.y())<=run*.26+1e-8,"walkable slope");
            }
            assertFalse(r.samplesNear(first.x(),first.z(),10).isEmpty());
        }
    }
    @Test void helixColumnKeepsEverySeparateFloor(){
        var h=TransitGeometry.helix(new TransitGeometry.Point(0,0,0),96,30);
        assertTrue(h.samplesNear(0,0,2).size()>=4);
        assertEquals(96,h.points().getLast().y());
        assertEquals(0,h.points().getLast().x(),1e-8);
        assertEquals(0,h.points().getLast().z(),1e-8);
        assertFalse(h.intersects(1000,1000,10));
    }
    @Test void rasterizedRailsCoverBothSidesOfHelicalTurns(){
        var h=TransitGeometry.helix(new TransitGeometry.Point(0,0,0),96,30);int rails=0,high=0;
        for(int x=-34;x<=34;x++)for(int z=-4;z<=64;z++)for(var s:h.samplesNear(x,z,11))
            if(TransitGeometry.isTrackLane(s,5)){rails++;if(s.floorY()>48)high++;}
        assertTrue(rails>900,"both sides of the spiral should receive rails");
        assertTrue(high>200,"rail lanes must continue on the upper turns too");
    }
    @Test void crosslineSpiralHasASeparateBay(){
        var a=new TransitGeometry.Point(0,0,0);var b=new TransitGeometry.Point(512,96,512);
        var main=TransitGeometry.between(a,b,0,40,8);var cross=TransitGeometry.between(a,b,1,40,8);
        assertTrue(main.points().stream().anyMatch(p->Math.abs(p.x()-104)<1e-8&&Math.abs(p.y())<1e-8&&Math.abs(p.z())<1e-8));
        assertTrue(cross.points().stream().anyMatch(p->Math.abs(p.x()-232)<1e-8&&Math.abs(p.y())<1e-8&&Math.abs(p.z())<1e-8));
    }
}
