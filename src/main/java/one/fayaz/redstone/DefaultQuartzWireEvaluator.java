package one.fayaz.redstone;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import one.fayaz.block.QuartzDustBlock;
import org.jspecify.annotations.Nullable;

public class DefaultQuartzWireEvaluator extends QuartzWireEvaluator {
    public DefaultQuartzWireEvaluator(final QuartzDustBlock wireBlock) {
        super(wireBlock);
    }

    @Override
    public void updatePowerStrength(
            final Level level, final BlockPos pos, final BlockState state, @Nullable final Orientation orientation, final boolean skipShapeUpdates
    ) {
        int targetStrength = this.calculateTargetStrength(level, pos);
        if ((Integer)state.getValue(RedStoneWireBlock.POWER) != targetStrength) {
            if (level.getBlockState(pos) == state) {
                level.setBlock(pos, state.setValue(RedStoneWireBlock.POWER, targetStrength), 2);
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

        // ---- fixed-point copper propagation ----
        // scale signals so we can decay by 0.5
        int scaledBlock = blockSignal * 2;
        int scaledWire  = wireSignal  * 2;

        // choose strongest input
        int strongest = Math.max(scaledBlock, scaledWire);

        // copper loses 0.5 per block
        strongest = Math.max(0, strongest - 1);

        // scale back down
        return strongest / 2;
    }

}
