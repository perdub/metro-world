package eu.metroworld.infrastructure;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
/** Visible fixed landing marker. Moving steps use ordinary slabs/full blocks for exact vanilla collision. */
public final class EscalatorBlock extends SimplePolymerBlock {
 public EscalatorBlock(){super(AbstractBlock.Settings.copy(Blocks.YELLOW_CONCRETE),Blocks.YELLOW_CONCRETE);}
}
