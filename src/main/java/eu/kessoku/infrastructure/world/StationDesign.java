package eu.kessoku.infrastructure.world;
import eu.kessoku.infrastructure.InfrastructureBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RailBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.util.math.Direction;

/** Original modern metro architecture. Coordinates are station-local, with the tracks along X.
 * The caller owns the protective shell and must merge connected passage openings afterwards.
 * Walking platforms are at Y=3, tracks at Y=1. Null means outside this room. */
public final class StationDesign {
    public static final int HALF_LENGTH = 40, HALF_WIDTH = 17, HEIGHT = 14;
    private StationDesign() {}

    public static BlockState sample(int x, int y, int z, long salt) {
        return sample(x, y, z, HALF_LENGTH, HALF_WIDTH, HEIGHT, salt);
    }

    public static BlockState sample(int x, int y, int z, int length, int width, int height, long salt) {
        int ax = Math.abs(x), az = Math.abs(z);
        if (ax > length || az > width || y < 0 || y > height) return null;
        BlockState accent = ((salt & 1) == 0 ? Blocks.CYAN_CONCRETE : (salt&4)==0?Blocks.BLUE_CONCRETE:Blocks.LIME_CONCRETE).getDefaultState();
        BlockState glass = ((salt & 1) == 0 ? Blocks.CYAN_STAINED_GLASS : (salt&4)==0?Blocks.LIGHT_BLUE_STAINED_GLASS:Blocks.LIME_STAINED_GLASS).getDefaultState();
        int roof = height - (az > width - 4 ? 2 : az > width - 7 ? 1 : 0);
        if (y >= roof) {
            if (y == roof && (az == 7 || az == width - 3) && Math.floorMod(x, 4) != 0)
                return Blocks.SEA_LANTERN.getDefaultState();
            return (y == roof && Math.floorMod(x, 12) == 0 ? Blocks.SMOOTH_QUARTZ : Blocks.WHITE_CONCRETE).getDefaultState();
        }
        // Track beds, textured platform paving and yellow tactile safety strips.
        if (y == 0) return (az <= 3 ? (Math.floorMod(x, 3) == 0 ? Blocks.POLISHED_DEEPSLATE : Blocks.GRAVEL) : Blocks.SMOOTH_STONE).getDefaultState();
        int bay = Math.floorMod(x + 6, 12);
        // A readable wooden bench: lower slab seats with upright trapdoor backs.
        if (length >= 34 && ax < length - 12 && bay >= 3 && bay <= 6) {
            if (y == 2 && az == width - 3) return Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
            if (y == 2 && az == width - 2) return Blocks.DARK_OAK_TRAPDOOR.getDefaultState()
                    .with(TrapdoorBlock.OPEN, true).with(TrapdoorBlock.HALF, BlockHalf.BOTTOM)
                    .with(TrapdoorBlock.FACING, z > 0 ? Direction.NORTH : Direction.SOUTH);
        }
        if (az >= 5 && y <= 2) {
            if (y == 2 && az == 5) return InfrastructureBlocks.TACTILE_TILE.getDefaultState();
            if (y == 2 && (Math.floorMod(x, 6) == 0 || az == width - 2)) return Blocks.POLISHED_ANDESITE.getDefaultState();
            return Blocks.SMOOTH_QUARTZ.getDefaultState();
        }
        // Two-step access near each platform end, leaving the main platform edge intact.
        if (ax >= length - 10 && ax <= length - 8 && az >= 3 && az <= 4) {
            int stepY=az-2;
            if(y<stepY&&y>0)return Blocks.SMOOTH_QUARTZ.getDefaultState();
            if(y==stepY)return Blocks.SMOOTH_QUARTZ_STAIRS.getDefaultState().with(StairsBlock.FACING,z>0?Direction.SOUTH:Direction.NORTH);
        }
        if (y == 1 && az == 2) return Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.EAST_WEST);
        // Glass service walls, solid lower skirting and a continuous coloured wayfinding ribbon.
        if (az == width) {
            if (y == 4 || y == 10) return accent;
            if (y >= 5 && y <= 8 && ax < length - 5 && Math.floorMod(x, 8) != 0) return glass;
            return Blocks.SMOOTH_QUARTZ.getDefaultState();
        }
        // End portals remain open for track and pedestrian connection merging.
        if (ax == length && !(az <= 5 && y <= 8)) {
            if (y == 8) return accent;
            if (az >= 7 && az <= width - 3 && y >= 5 && y <= 7) return glass;
            return Blocks.SMOOTH_QUARTZ.getDefaultState();
        }
        // Repeated slim structural ribs, with lit capitals and open pedestrian aisles.
        boolean rib = Math.floorMod(x, 12) == 0 && ax < length - 4;
        if (rib && az == width - 4 && y >= 3 && y < roof) {
            if (y == roof - 1) return Blocks.SEA_LANTERN.getDefaultState();
            return InfrastructureBlocks.RIBBED_PANEL.getDefaultState();
        }
        if (rib && y == roof - 1 && az >= 5) return Blocks.SMOOTH_QUARTZ.getDefaultState();
        // A real six-step public stair to a mezzanine on both side platforms.
        // Small station variants simply omit it rather than producing clipped stairs.
        if (length >= 34 && width >= 15 && height >= 13) {
            int stairStart = length - 24, stairEnd = stairStart + 5;
            if (az >= 10 && az <= 12 && x >= stairStart && x <= stairEnd) {
                int stepY = 3 + x - stairStart;
                if (y < stepY) return Blocks.SMOOTH_QUARTZ.getDefaultState();
                if (y == stepY) return Blocks.SMOOTH_QUARTZ_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
                return Blocks.AIR.getDefaultState();
            }
            if (x > stairEnd && ax < length && az >= 8 && az < width) {
                if (y == 8) return Blocks.SMOOTH_QUARTZ.getDefaultState();
                if (az == 8 && (y == 9 || y == 10)) return Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState();
            }
        }
        // Some stations have an upper pedestrian bridge linking both mezzanines.
        if((salt&8)!=0&&x>=length-16&&x<=length-12&&az<width){
            if(y==8)return Blocks.SMOOTH_QUARTZ.getDefaultState();
            if(az<8&&(x==length-16||x==length-12)&&(y==9||y==10))return Blocks.LIME_STAINED_GLASS.getDefaultState();
        }
        // Benches have shaped backs; planters are bounded by low stone curbs.
        if (ax < length - 12 && az >= width - 3 && az <= width - 1 && bay >= 8 && bay <= 10) {
            if (y == 3) {
                if (az == width - 2 && bay == 9) return Blocks.GRASS_BLOCK.getDefaultState();
                return Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM);
            }
            if (y == 4 && az == width - 2 && bay == 9)
                return ((salt & 2) == 0 ? Blocks.PINK_TULIP : Blocks.OXEYE_DAISY).getDefaultState();
        }
        // Raised signs are geometric, vanilla-visible and do not obstruct the platform.
        if (Math.floorMod(x, 24) == 6 && az >= 6 && az <= 9 && y == 8) return accent;
        return Blocks.AIR.getDefaultState();
    }
}
