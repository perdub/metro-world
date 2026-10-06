package eu.metroworld.infrastructure.world;

import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class SideBranchDesignTest {
    @Test void entranceAndTurnRemainWalkableForEveryStationSize(){
        for(int length:new int[]{28,40,54})for(long seed=0;seed<32;seed++){
            var s=SideBranchDesign.select(seed*719249321L,length);
            for(int x=-100;x<=-length+6;x++)for(int z=10;z<=14;z++)for(int y=3;y<=6;y++)
                assertTrue(SideBranchDesign.interior(x,y,z,s));
            for(int z=12;z<=70;z++)for(int x=-102;x<=-98;x++)for(int y=3;y<=6;y++){
                assertTrue(SideBranchDesign.interior(x,y,z,s));
                assertFalse(SideBranchDesign.container(x,y,z,s));
                assertFalse(SideBranchDesign.spawner(x,y,z,s));
            }
        }
    }
    @Test void quarantineSpawnPointsLeaveCentralDoorwayOpen(){
        var s=new SideBranchDesign.Spec(SideBranchDesign.Kind.QUARANTINE,21,40);
        int spawners=0;
        for(int x=-118;x<=-82;x++)for(int z=56;z<=84;z++){
            if(SideBranchDesign.spawner(x,3,z,s)){
                spawners++;
                assertTrue(SideBranchDesign.interior(x,3,z,s));
                assertTrue(z>75);
            }
        }
        assertEquals(3,spawners);
        assertTrue(SideBranchDesign.container(-114,3,80,s));
    }
    @Test void seedsProduceAllFourBranchThemes(){
        var kinds=EnumSet.noneOf(SideBranchDesign.Kind.class);
        for(long seed=0;seed<256;seed++){
            var s=SideBranchDesign.select(seed*719249321L,40);
            assertEquals(s,SideBranchDesign.select(seed*719249321L,40));
            kinds.add(s.kind());
        }
        assertEquals(4,kinds.size());
    }
}
