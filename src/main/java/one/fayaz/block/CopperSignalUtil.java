package one.fayaz.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CopperSignalUtil {

    private static final Direction[] DIRECTIONS = Direction.values();

    public static int getBestNeighborSignal(BlockGetter level, BlockPos pos) {
        int best = 0;

        for (Direction direction : DIRECTIONS) {
            int signal = getSignal(level, pos.relative(direction), direction);
            if (signal >= 32) return 32;
            if (signal > best) best = signal;
        }

        return best;
    }

    private static int getSignal(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        int signal = state.getSignal(level, pos, direction);
        return state.isRedstoneConductor(level, pos)
                ? Math.max(signal, getDirectSignalTo(level, pos))
                : signal;
    }

    private static int getDirectSignalTo(BlockGetter level, BlockPos pos) {
        int best = 0;
        for (Direction dir : DIRECTIONS) {
            best = Math.max(best, level.getBlockState(pos.relative(dir))
                    .getDirectSignal(level, pos.relative(dir), dir));
            if (best >= 32) return 32;
        }
        return best;
    }
}
