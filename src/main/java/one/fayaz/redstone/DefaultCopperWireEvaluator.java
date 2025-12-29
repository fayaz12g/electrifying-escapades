package one.fayaz.redstone;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import one.fayaz.block.CopperDustBlock;
import one.fayaz.block.QuartzDustBlock;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class DefaultCopperWireEvaluator extends CopperWireEvaluator {
    public DefaultCopperWireEvaluator(final CopperDustBlock wireBlock) {
        super(wireBlock);
    }

    @Override
    public void updatePowerStrength(
            final Level level, final BlockPos pos, final BlockState state, @Nullable final Orientation orientation, final boolean skipShapeUpdates
    ) {
        int targetStrength = this.calculateTargetStrength(level, pos);
        if ((Integer)state.getValue(CopperDustBlock.POWER) != targetStrength) {
            if (level.getBlockState(pos) == state) {
                level.setBlock(pos, state.setValue(CopperDustBlock.POWER, targetStrength), 2);
            }

            Set<BlockPos> toUpdate = Sets.<BlockPos>newHashSet();
            toUpdate.add(pos);

            for (Direction direction : Direction.values()) {
                toUpdate.add(pos.relative(direction));
            }

            for (BlockPos blockPos : toUpdate) {
                level.updateNeighborsAt(blockPos, this.wireBlock);
            }
        }
    }

    private int calculateTargetStrength(final Level level, final BlockPos pos) {
        int blockSignal = this.getBlockSignal(level, pos);
        int wireSignal  = this.getIncomingWireSignal(level, pos);

        // ---- SCALE UP vanilla signals from 0-15 to 0-32 ----
        // Vanilla blocks output 0-15, but we want 0-32
        // Scale: 15 -> 32, preserving 0 -> 0
        if (blockSignal > 0 && blockSignal <= 15) {
            blockSignal = (int) Math.ceil((blockSignal * 32.0) / 15.0);
        }
        // wireSignal is already in 0-32 range from other copper dust

        // scale signals so we can decay by 0.5
        int scaledBlock = blockSignal * 2;
        int scaledWire  = wireSignal  * 2;

        // choose strongest input
        int strongest = Math.max(scaledBlock, scaledWire);

        // copper loses 0.5 per block (1 after division by 2)
        strongest = Math.max(0, strongest - 1);

        // scale back down
        return strongest / 2;
    }

}
