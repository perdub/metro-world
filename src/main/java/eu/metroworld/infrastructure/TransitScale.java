package eu.metroworld.infrastructure;
/** Reverse Nether ratio: eight metro blocks correspond to one Overworld block. */
public final class TransitScale {
 public static final double METRO_BLOCKS_PER_OVERWORLD_BLOCK=8.0;
 public static double toMetro(double coordinate){return coordinate*METRO_BLOCKS_PER_OVERWORLD_BLOCK;}
 public static double toOverworld(double coordinate){return coordinate/METRO_BLOCKS_PER_OVERWORLD_BLOCK;}
 public static int stationCell(double metroCoordinate){return (int)Math.floor((metroCoordinate+256)/512);}
 private TransitScale(){}
}
